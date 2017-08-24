/*
 * Segment 10 System (Sys), SWE 10.1 StartStopp
 * Copyright (C) 2007-2017 BitCtrl Systems GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Contact Information:<br>
 * BitCtrl Systems GmbH<br>
 * Weißenfelser Straße 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.sys.startstopp.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.KernSystem;
import de.bsvrz.sys.startstopp.api.jsonschema.StartArt;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkriptStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus.Status;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.Usv;
import de.bsvrz.sys.startstopp.api.jsonschema.ZugangDav;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.config.StartStoppKonfiguration;
import de.bsvrz.sys.startstopp.process.OnlineApplikation.ApplikationStatus;
import de.bsvrz.sys.startstopp.process.OnlineApplikation.TaskType;
import de.bsvrz.sys.startstopp.process.dav.DavConnector;
import de.bsvrz.sys.startstopp.process.remote.RechnerClient;
import de.bsvrz.sys.startstopp.process.remote.RechnerManager;
import de.bsvrz.sys.startstopp.startstopp.StartStopp;
import de.bsvrz.sys.startstopp.startstopp.StartStoppOptions;
import de.bsvrz.sys.startstopp.util.NamingThreadFactory;

public final class ProzessManager {

	public enum StartStoppMode {
		SKRIPT, MANUELL;
	}

	private static final Debug LOGGER = Debug.getLogger();
	private StartStoppStatus.Status managerStatus = StartStoppStatus.Status.INITIALIZED;

	private Map<String, OnlineApplikation> applikationen = new LinkedHashMap<>();
	private StartStoppKonfiguration aktuelleKonfiguration;
	private DavConnector davConnector;
	private RechnerManager rechnerManager = new RechnerManager();
	private StartStopp startStopp;

	public ProzessManager() {
		this(StartStopp.getInstance());
	}

	ProzessManager(StartStopp startStopp) {
		davConnector = new DavConnector(this);
		this.startStopp = startStopp;
		startStopp.getSkriptManager().onKonfigurationChanged
				.addHandler((konfiguration) -> skriptAktualisiert(konfiguration));

		try {
			StartStoppKonfiguration skript = startStopp.getSkriptManager().getCurrentSkript();
			if (skript.getSkriptStatus().getStatus() == StartStoppSkriptStatus.Status.INITIALIZED) {
				aktuelleKonfiguration = skript;
				davConnector.reconnect();
				rechnerManager.reconnect(aktuelleKonfiguration.getResolvedRechner());

				for (OnlineInkarnation inkarnation : aktuelleKonfiguration.getInkarnationen()) {
					OnlineApplikation applikation = new OnlineApplikation(this, inkarnation);
					applikationen.put(applikation.getName(), applikation);
					applikation.onStatusChanged.addHandler((status) -> applikationStatusChanged(status));
				}

				managerStatus = Status.RUNNING;
				for (OnlineApplikation applikation : applikationen.values()) {
					applikation.checkState();
				}
			} else {
				managerStatus = Status.CONFIGERROR;
			}
		} catch (StartStoppException e) {
			LOGGER.fine(e.getLocalizedMessage());
			aktuelleKonfiguration = null;
		}

	}

	public DavConnector getDavConnector() {
		return davConnector;
	}

	public Collection<OnlineApplikation> getApplikationen() {
		return Collections.unmodifiableCollection(applikationen.values());
	}

	public Applikation getApplikation(String inkarnationsName) throws StartStoppException {
		OnlineApplikation managedApplikation = applikationen.get(inkarnationsName);
		if (managedApplikation != null) {
			return managedApplikation.getApplikation();
		}

		throw new StartStoppException(
				"Eine Applikation mit dem Inkarnationsname \"" + inkarnationsName + "\" konnte nicht gefunden werden");
	}

	public OnlineApplikation starteApplikation(String inkarnationsName, StartStoppMode modus)
			throws StartStoppException {
		checkStartModus(modus);
		OnlineApplikation applikation = applikationen.get(inkarnationsName);
		if (applikation == null) {
			throw new StartStoppException("Eine Applikation mit dem Inkarnationsname \"" + inkarnationsName
					+ "\" konnte nicht gefunden werden");
		}

		applikation.startSystemProcess(modus);
		return applikation;
	}

	private void checkStartModus(StartStoppMode modus) throws StartStoppException {
		if ((modus == StartStoppMode.MANUELL) && (managerStatus != StartStoppStatus.Status.RUNNING)) {
			throw new StartStoppException(
					"Eine Applikation kann im Status: " + managerStatus + " nicht gestartet werden!");
		}
	}

	/**
	 * Die Funktion startet die mit dem Inkarnationsname beschriebene Applikation
	 * neu.
	 * 
	 * Beim Neustart einer Applikation werden die Start-Stopp-Regeln nicht
	 * angewendet!
	 * 
	 * @param inkarnationsName
	 *            der Inkarnationsname der Applikation
	 * @return die Informationen zur Applikation
	 * @throws StartStoppException
	 *             der Neustart ist fehlgeschlagen
	 */
	public OnlineApplikation restarteApplikation(String inkarnationsName, StartStoppMode modus)
			throws StartStoppException {
		checkStartModus(modus);
		OnlineApplikation applikation = applikationen.get(inkarnationsName);
		if (applikation == null) {
			throw new StartStoppException("Eine Applikation mit dem Inkarnationsname \"" + inkarnationsName
					+ "\" konnte nicht gefunden werden");
		}

		CompletableFuture
				.runAsync(new AppStopper(Collections.singleton(applikation), modus, false), Executors
						.newSingleThreadExecutor(new NamingThreadFactory(inkarnationsName + "_StoppForRestart")))
				.thenRun(() -> {
					try {
						applikation.startSystemProcess(modus);
					} catch (StartStoppException e) {
						LOGGER.warning(e.getLocalizedMessage());
					}
				});

		return applikation;
	}

	public OnlineApplikation stoppeApplikation(String inkarnationsName, StartStoppMode modus)
			throws StartStoppException {
		OnlineApplikation applikation = applikationen.get(inkarnationsName);
		if (applikation != null) {
			try {
				applikation.updateStatus(Applikation.Status.STOPPENWARTEN, modus,
						"Beenden über Datenverteilernachricht");
				davConnector.stoppApplikation(inkarnationsName);
			} catch (StartStoppException e) {
				Debug.getLogger()
						.warning("Die Applikation \"" + inkarnationsName
								+ "\" konnte nicht über die Dav-Terminierungsschnittstelle beendet werden: "
								+ e.getLocalizedMessage());
				applikation.stoppeApplikation(modus);
			}

			return applikation;
		}

		throw new StartStoppException(
				"Eine Applikation mit dem Inkarnationsname \"" + inkarnationsName + "\" konnte nicht gefunden werden");
	}

	public CompletableFuture<Void> stoppeSkript(StartStoppMode modus) {

		if (managerStatus != StartStoppStatus.Status.RUNNING) {
			return new CompletableFuture<>();
		}
		managerStatus = StartStoppStatus.Status.STOPPING;
		ExecutorService skriptStopperExecutor = Executors.newFixedThreadPool(1,
				new NamingThreadFactory("SkriptStopper"));
		return CompletableFuture.runAsync(new SkriptStopper(this, modus), skriptStopperExecutor).thenRun(() -> {
			managerStatus = Status.STOPPED;
			skriptStopperExecutor.shutdown();
		});
	}

	public CompletableFuture<Void> shutdownSkript(StartStoppMode modus) {

		if (managerStatus == StartStoppStatus.Status.SHUTDOWN) {
			return new CompletableFuture<>();
		}
		managerStatus = StartStoppStatus.Status.SHUTDOWN;
		return CompletableFuture.runAsync(new SkriptStopper(this, modus),
				Executors.newSingleThreadExecutor(new NamingThreadFactory("SkriptShutdown")));
	}

	private void skriptAktualisiert(StartStoppKonfiguration neueKonfiguration) {

		boolean kernsystemGeandert = false;
		List<String> entfernt = new ArrayList<>();
		Map<String, InkarnationsAenderung> geandert = new LinkedHashMap<>();

		if (aktuelleKonfiguration != null) {
			try {
				KonfigurationsVergleicher konfigurationsVergleicher = new KonfigurationsVergleicher();
				konfigurationsVergleicher.vergleiche(aktuelleKonfiguration, neueKonfiguration);
				kernsystemGeandert = konfigurationsVergleicher.isKernsystemGeandert();
				entfernt.addAll(konfigurationsVergleicher.getEntfernteInkarnationen());
				geandert.putAll(konfigurationsVergleicher.getGeanderteInkarnationen());
			} catch (StartStoppException e) {
				LOGGER.warning(e.getLocalizedMessage());
				return;
			}
		}

		if (kernsystemGeandert) {
			stoppeSkript(StartStoppMode.SKRIPT).thenRun(() -> {
				aktuellesSkriptAnpassen(neueKonfiguration, entfernt, geandert);
			});
		} else {
			aktuellesSkriptAnpassen(neueKonfiguration, entfernt, geandert);
		}
	}

	private void aktuellesSkriptAnpassen(StartStoppKonfiguration neueKonfiguration, List<String> entfernt,
			Map<String, InkarnationsAenderung> geandert) {
		try {
			aktuelleKonfiguration = neueKonfiguration;
			davConnector.reconnect();
			rechnerManager.reconnect(aktuelleKonfiguration.getResolvedRechner());

			for (String name : entfernt) {
				OnlineApplikation applikation = applikationen.remove(name);
				if (applikation != null) {
					applikation.stoppeApplikation(StartStoppMode.SKRIPT);
					applikation.dispose();
				}
			}

			for (OnlineInkarnation inkarnation : aktuelleKonfiguration.getInkarnationen()) {
				OnlineApplikation applikation = applikationen.get(inkarnation.getName());
				if (applikation == null) {
					applikation = new OnlineApplikation(this, inkarnation);
					applikationen.put(applikation.getName(), applikation);
					applikation.onStatusChanged.addHandler((status) -> applikationStatusChanged(status));
					applikation.updateStatus(Applikation.Status.INSTALLIERT, StartStoppMode.SKRIPT,
							"Applikation angelegt");
					if (managerStatus == Status.RUNNING) {
						applikation.checkState();
					}
				} else {
					applikation.getApplikation().setInkarnation(inkarnation.getInkarnation());
					if ((managerStatus == Status.RUNNING) && geandert.containsKey(inkarnation.getName())) {
						// TODO Änderungen genauer auswerten
						restarteApplikation(inkarnation.getName(), StartStoppMode.SKRIPT);
					}
				}
			}

			starteSkript(StartStoppMode.SKRIPT);

		} catch (StartStoppException e) {
			LOGGER.error(e.getLocalizedMessage());
			throw new IllegalStateException("Sollte hier nicht auftreten, da nur geprüfte Skripte verwendet werden!",
					e);
		}
	}

	public Set<String> waitForStartBedingung(OnlineApplikation managedApplikation) {

		Set<String> result = new LinkedHashSet<>();

		StartBedingung bedingung = managedApplikation.getStartBedingung();
		if (bedingung == null) {
			return result;
		}

		String rechnerName = bedingung.getRechner();
		if ((rechnerName != null) && !rechnerName.trim().isEmpty()) {
			return waitForRemoteStartBedingung(managedApplikation, rechnerName, bedingung);
		}

		for (String vorgaenger : bedingung.getVorgaenger()) {
			OnlineApplikation applikation = applikationen.get(vorgaenger);
			if (applikation == null) {
				LOGGER.warning("In der Startbedingung referenzierte Inkarnation \"" + bedingung.getVorgaenger()
						+ "\" existiert nicht!");
				continue;
			}

			if (!referenzApplikationGueltigFuerStart(applikation.getApplikation(), bedingung)) {
				result.add(applikation.getName());
				LOGGER.info(managedApplikation.getName() + " muss auf " + applikation.getName() + " warten!");
			}
		}
		return result;
	}

	private boolean referenzApplikationGueltigFuerStart(Applikation applikation, StartBedingung bedingung) {
		switch (bedingung.getWarteart()) {
		case BEGINN:
			if ((applikation.getStatus() != Applikation.Status.GESTARTET)
					&& (applikation.getStatus() != Applikation.Status.INITIALISIERT)) {
				return false;
			}
			break;
		case ENDE:
			if (applikation.getStatus() != Applikation.Status.INITIALISIERT) {
				return false;
			}
			break;
		default:
			break;
		}

		return true;
	}

	public Set<String> waitForStoppBedingung(OnlineApplikation managedApplikation) {

		Set<String> result = new LinkedHashSet<>();

		StoppBedingung bedingung = managedApplikation.getStoppBedingung();
		if (bedingung == null) {
			return result;
		}

		String rechnerName = bedingung.getRechner();
		if (rechnerName != null && !rechnerName.trim().isEmpty()) {
			return waitForRemoteStoppBedingung(managedApplikation, rechnerName, bedingung);
		}

		for (String nachfolger : bedingung.getNachfolger()) {
			OnlineApplikation applikation = applikationen.get(nachfolger);
			if (applikation == null) {
				LOGGER.warning("In der Stoppbedingung referenzierte Inkarnation \"" + bedingung.getNachfolger()
						+ "\" existiert nicht!");
				continue;
			}

			if (!referenzApplikationGueltigFuerStopp(applikation.getApplikation())) {
				result.add(nachfolger);
			}
		}

		return result;
	}

	private boolean referenzApplikationGueltigFuerStopp(Applikation applikation) {
		switch (applikation.getStatus()) {
		case GESTOPPT:
		case INSTALLIERT:
		case STARTENWARTEN:
			break;
		case GESTARTET:
		case INITIALISIERT:
		case STOPPENWARTEN:
			return false;
		default:
			break;
		}

		return true;
	}

	private Set<String> waitForRemoteStoppBedingung(OnlineApplikation managedApplikation, String rechnerName,
			StoppBedingung bedingung) {
		Set<String> result = new LinkedHashSet<>();
		RechnerClient rechnerClient = this.rechnerManager.getClient(rechnerName);
		if (rechnerClient == null) {
			throw new IllegalStateException(
					"Rechner " + rechnerName + " ist in der aktuellen Konfiguration nicht definiert!");
		}

		for (String nachfolger : bedingung.getNachfolger()) {
			Applikation applikation = rechnerClient.getApplikation(nachfolger);
			if (applikation == null) {
				LOGGER.info(managedApplikation.getName() + " kann den Status von " + nachfolger + " auf Rechner \""
						+ rechnerName + "\" nicht ermittlen!");
			} else if (!referenzApplikationGueltigFuerStopp(applikation)) {
				result.add(nachfolger);
				LOGGER.info(
						managedApplikation.getName() + " muss auf " + applikation.getInkarnation().getInkarnationsName()
								+ " auf Rechner \"" + rechnerName + "\" warten!");
			}
		}

		return result;
	}

	private Set<String> waitForRemoteStartBedingung(OnlineApplikation managedApplikation, String rechnerName,
			StartBedingung bedingung) {

		Set<String> result = new LinkedHashSet<>();
		RechnerClient rechnerClient = rechnerManager.getClient(rechnerName);
		if (rechnerClient == null) {
			throw new IllegalStateException("Rechner " + rechnerName + " ist in der Konfiguration nicht definiert!");
		}
		for (String vorgaenger : bedingung.getVorgaenger()) {
			Applikation applikation = rechnerClient.getApplikation(vorgaenger);
			if (applikation == null) {
				result.add(vorgaenger);
				LOGGER.info(managedApplikation.getName() + " muss auf " + vorgaenger + " auf Rechner \"" + rechnerName
						+ "\" warten!");
			} else if (!referenzApplikationGueltigFuerStart(applikation, bedingung)) {
				result.add(vorgaenger);
				LOGGER.info(
						managedApplikation.getName() + " muss auf " + applikation.getInkarnation().getInkarnationsName()
								+ " auf Rechner \"" + rechnerName + "\" warten!");
			}
		}

		return result;
	}

	public Set<String> waitForKernsystemStart(OnlineApplikation applikation) {

		Set<String> result = new LinkedHashSet<>();

		for (KernSystem ks : aktuelleKonfiguration.getKernSysteme()) {
			if (ks.getInkarnationsName().equals(applikation.getName())) {
				return result;
			}
			OnlineApplikation app = applikationen.get(ks.getInkarnationsName());
			switch (app.getStatus()) {
			case GESTARTET:
			case INITIALISIERT:
				break;
			default:
				result.add(applikation.getName());
			}
		}

		return result;
	}

	public Set<String> waitForKernsystemStopp(OnlineApplikation applikation) {

		Set<String> result = new LinkedHashSet<>();
		boolean foundKernsoftwareApplikation = false;

		for (KernSystem ks : aktuelleKonfiguration.getKernSysteme()) {
			if (!foundKernsoftwareApplikation) {
				if (ks.getInkarnationsName().equals(applikation.getName())) {
					foundKernsoftwareApplikation = true;
				}
			} else {
				OnlineApplikation app = applikationen.get(ks.getInkarnationsName());
				switch (app.getStatus()) {
				case GESTARTET:
				case INITIALISIERT:
				case STOPPENWARTEN:
					result.add(app.getName());
					break;
				case GESTOPPT:
				case INSTALLIERT:
				case STARTENWARTEN:
					break;
				default:
					break;
				}
			}
		}

		return result;
	}

	public void applikationStatusChanged(ApplikationStatus status) {
		for (OnlineApplikation applikation : applikationen.values()) {
			if (!applikation.getName().equals(status.applikation.getName()))
				applikation.checkState();
		}
	}

	public String getDavConnectionMsg() {
		return davConnector.getConnectionMsg();
	}

	public void updateFromDav(String inkarnationsName, boolean fertig) {
		OnlineApplikation applikation = applikationen.get(inkarnationsName);
		if (fertig) {
			if (applikation.getStatus() == Applikation.Status.GESTARTET) {
				applikation.updateStatus(Applikation.Status.INITIALISIERT, "");
			} else {
				LOGGER.warning(applikation.getName() + " ist im Status " + applikation.getStatus());
			}
		} else {
			switch (applikation.getApplikation().getInkarnation().getInkarnationsTyp()) {
			case DAV:
			case WRAPPED:
				applikation.updateStatus(applikation.getStatus(), "Keine Fertigmeldung vom Datenverteiler");
				break;
			case EXTERN:
			default:
				break;
			}
		}
	}

	public void starteSkript(StartStoppMode modus) throws StartStoppException {

		checkSkriptStart(modus);
		managerStatus = Status.RUNNING;
		for (OnlineApplikation applikation : applikationen.values()) {
			if (applikation.getStartArtOption() != StartArt.Option.MANUELL) {
				if (applikation.getStatus() == Applikation.Status.GESTOPPT) {
					applikation.updateStatus(Applikation.Status.INSTALLIERT, modus, "");
				} else if (applikation.getStatus() == Applikation.Status.INSTALLIERT) {
					applikation.checkState();
				}
			}
		}
	}

	private void checkSkriptStart(StartStoppMode modus) throws StartStoppException {
		if (modus == StartStoppMode.MANUELL) {
			switch (managerStatus) {
			case RUNNING:
			case STOPPED:
				break;
			case STOPPING:
				if (checkStopStatus() != StartStoppStatus.Status.STOPPED) {
					throw new StartStoppException(
							"Der Prozessmanager kann im Status " + managerStatus + " nicht neu gestartet werden");
				}
				break;
			case CONFIGERROR:
			case INITIALIZED:
			case SHUTDOWN:
				throw new StartStoppException(
						"Der Prozessmanager kann im Status " + managerStatus + " nicht neu gestartet werden");
			default:
				break;

			}
		}
		// TODO Auto-generated method stub

	}

	private Status checkStopStatus() {

		if (managerStatus != StartStoppStatus.Status.STOPPED) {
			for (OnlineApplikation applikation : applikationen.values()) {
				if (applikation.getStatus() != Applikation.Status.GESTOPPT) {
					return managerStatus;
				}
			}
		}
		managerStatus = StartStoppStatus.Status.STOPPED;
		return managerStatus;
	}

	public Status getStatus() {
		return managerStatus;
	}

	public void davConnected() {
		for (OnlineApplikation applikation : applikationen.values()) {
			applikation.checkState(TaskType.DEFAULT);
		}
	}

	public StartStoppOptions getOptions() {
		return startStopp.getOptions();
	}

	public ZugangDav getZugangDav() {
		try {
			return aktuelleKonfiguration.getResolvedZugangDav();
		} catch (StartStoppException e) {
			LOGGER.warning(e.getLocalizedMessage());
		}

		return new ZugangDav();
	}

	public Usv getUsv() {
		try {
			if (aktuelleKonfiguration != null) {
				return aktuelleKonfiguration.getResolvedUsv();
			}
		} catch (StartStoppException e) {
			LOGGER.warning(e.getLocalizedMessage());
		}

		return null;
	}

}
