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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.ApplikationLog;
import de.bsvrz.sys.startstopp.api.jsonschema.KernSystem;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkriptStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus.Status;
import de.bsvrz.sys.startstopp.api.jsonschema.Usv;
import de.bsvrz.sys.startstopp.api.jsonschema.ZugangDav;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.config.StartStoppKonfiguration;
import de.bsvrz.sys.startstopp.process.OnlineApplikation.TaskType;
import de.bsvrz.sys.startstopp.process.dav.DavConnector;
import de.bsvrz.sys.startstopp.process.remote.RechnerClient;
import de.bsvrz.sys.startstopp.process.remote.RechnerManager;
import de.bsvrz.sys.startstopp.startstopp.StartStopp;
import de.bsvrz.sys.startstopp.startstopp.StartStoppOptions;
import de.bsvrz.sys.startstopp.util.NamingThreadFactory;
import de.muspellheim.events.Event;

public final class ProzessManager {

	private static final Debug LOGGER = Debug.getLogger();

	private Map<String, OnlineApplikation> applikationen = new LinkedHashMap<>();
	private StartStoppKonfiguration aktuelleKonfiguration;
	private DavConnector davConnector;
	private RechnerManager rechnerManager = new RechnerManager();
	private StartStopp startStopp;

	private boolean neuStartGeplant;

	public ProzessManager() {
		this(StartStopp.getInstance());
	}

	ProzessManager(StartStopp startStopp) {
		davConnector = new DavConnector(this);
		this.startStopp = startStopp;
		startStopp.getSkriptManager().onKonfigurationChanged
				.addHandler((konfiguration) -> aktualisiereSkript(konfiguration));

		try {
			StartStoppKonfiguration skript = startStopp.getSkriptManager().getCurrentSkript();
			if (skript.getSkriptStatus().getStatus() == StartStoppSkriptStatus.Status.INITIALIZED) {
				aktuelleKonfiguration = skript;
				davConnector.reconnect();
				rechnerManager.reconnect(aktuelleKonfiguration.getResolvedRechner());

				for (OnlineInkarnation inkarnation : aktuelleKonfiguration.getInkarnationen()) {
					OnlineApplikation applikation = new OnlineApplikation(this, inkarnation);
					applikation.onStatusChanged.addHandler((status) -> applikationStatusChanged(status));
					applikationen.put(applikation.getName(), applikation);
				}

				startStopp.setStatus(Status.RUNNING);
				for (OnlineApplikation applikation : applikationen.values()) {
					applikation.checkState(TaskType.DEFAULT);
				}
			} else {
				startStopp.setStatus(Status.CONFIGERROR);
			}
		} catch (StartStoppException e) {
			LOGGER.fine(e.getLocalizedMessage());
			aktuelleKonfiguration = null;
		}

		rechnerManager.doRechnerManagerAktualisiert.addHandler(() -> {
			applikationen.values().forEach(app -> app.checkState(TaskType.DEFAULT));
		});

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOGGER.info("Shutdown-Hook aufgerufen");
				if (startStopp.getStatus() != StartStoppStatus.Status.SHUTDOWN) {
					shutdownSkript();
				}
				while (!checkStoppStatus()) {
					LOGGER.info("Warte auf Applikationen");
					try {
						TimeUnit.SECONDS.sleep(2);
					} catch (InterruptedException e) {
						LOGGER.warning(e.getLocalizedMessage());
					}
				}
				LOGGER.info("Alle Applikationen beendet");
			}
		});
	}


	public DavConnector getDavConnector() {
		return davConnector;
	}

	public Collection<OnlineApplikation> getApplikationen() {
		return Collections.unmodifiableCollection(applikationen.values());
	}

	public OnlineApplikation getApplikation(String inkarnationsName) throws StartStoppException {
		OnlineApplikation managedApplikation = applikationen.get(inkarnationsName);
		if (managedApplikation != null) {
			return managedApplikation;
		}

		throw new StartStoppException(
				"Eine Applikation mit dem Inkarnationsname \"" + inkarnationsName + "\" konnte nicht gefunden werden");
	}

	public OnlineApplikation starteApplikation(String inkarnationsName) throws StartStoppException {
		return starteApplikation(inkarnationsName, false);
	}
	
	public OnlineApplikation starteApplikation(String inkarnationsName, boolean manuell) throws StartStoppException {
		checkManualStartModus();
		OnlineApplikation applikation = applikationen.get(inkarnationsName);
		if (applikation == null) {
			throw new StartStoppException("Eine Applikation mit dem Inkarnationsname \"" + inkarnationsName
					+ "\" konnte nicht gefunden werden");
		}

		applikation.starteApplikationManuell();
		return applikation;
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
	public OnlineApplikation restarteApplikation(String inkarnationsName) throws StartStoppException {
		checkManualStartModus();
		OnlineApplikation applikation = applikationen.get(inkarnationsName);
		if (applikation == null) {
			throw new StartStoppException("Eine Applikation mit dem Inkarnationsname \"" + inkarnationsName
					+ "\" konnte nicht gefunden werden");
		}

		CompletableFuture
				.runAsync(new AppStopper(Collections.singleton(applikation), false), Executors
						.newSingleThreadExecutor(new NamingThreadFactory(inkarnationsName + "_StoppForRestart")))
				.thenRun(() -> {
					try {
						applikation.starteApplikationManuell();
					} catch (StartStoppException e) {
						LOGGER.warning(e.getLocalizedMessage());
					}
				});

		return applikation;
	}

	public OnlineApplikation stoppeApplikation(String inkarnationsName) throws StartStoppException {
		OnlineApplikation applikation = applikationen.get(inkarnationsName);
		if (applikation != null) {
			try {
				if (applikation.requestStopp("Beenden über Datenverteilernachricht", true)) {
					davConnector.stoppApplikation(inkarnationsName);
				}
			} catch (StartStoppException e) {
				Debug.getLogger()
						.warning("Die Applikation \"" + inkarnationsName
								+ "\" konnte nicht über die Dav-Terminierungsschnittstelle beendet werden: "
								+ e.getLocalizedMessage());
				applikation.stoppeApplikation();
			}

			return applikation;
		}

		throw new StartStoppException(
				"Eine Applikation mit dem Inkarnationsname \"" + inkarnationsName + "\" konnte nicht gefunden werden");
	}

	public void stoppeSkript() {

		switch (startStopp.getStatus()) {
		case RUNNING_CANCELED:
		case RUNNING:
		case STOPPING_CANCELED:
			davConnector.sendeBetriebsmeldung("StartStopp-Skript wird angehalten");
			if (checkStoppStatus()) {
				startStopp.setStatus(Status.STOPPED);
			} else {
				startStopp.setStatus(Status.STOPPING);
			}
			break;
		case SHUTDOWN:
		case STOPPED:
		case STOPPING:
		case CONFIGERROR:
		case INITIALIZED:
		default:
			break;

		}
	}

	public void restarteSkript() {
		if (startStopp.getStatus() == StartStoppStatus.Status.RUNNING) {
			neuStartGeplant = true;
			startStopp.setStatus(Status.STOPPING);
		} else if (startStopp.getStatus() == StartStoppStatus.Status.STOPPED) {
			try {
				starteSkript();
			} catch (StartStoppException e) {
				LOGGER.warning(e.getLocalizedMessage());
			}
		}
	}

	public void shutdownSkript() {
		switch (startStopp.getStatus()) {
		case RUNNING:
		case RUNNING_CANCELED:
		case STOPPING:
		case STOPPING_CANCELED:
			startStopp.setStatus(Status.SHUTDOWN);
			break;
		case CONFIGERROR:
		case INITIALIZED:
		case STOPPED:
			// System.exit(0);
			break;
		case SHUTDOWN:
			break;
		default:
			LOGGER.warning("Unerwarteter Status: " + startStopp.getStatus());
			break;
		}
	}

	private void aktualisiereSkript(StartStoppKonfiguration neueKonfiguration) {

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
			stoppeSkript();
			CompletableFuture.runAsync(() -> startStopp.waitForStopp())
					.thenRun(() -> aktuellesSkriptAnpassen(neueKonfiguration, entfernt, geandert));
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
					applikation.stoppeApplikation();
					applikation.dispose();
				}
			}

			for (OnlineInkarnation inkarnation : aktuelleKonfiguration.getInkarnationen()) {
				OnlineApplikation applikation = applikationen.get(inkarnation.getName());
				if (applikation == null) {
					applikation = new OnlineApplikation(this, inkarnation);
					applikationen.put(applikation.getName(), applikation);
					applikation.onStatusChanged.addHandler((status) -> applikationStatusChanged(status));
					applikation.requestStart("Applikation angelegt");
					if (startStopp.getStatus() == Status.RUNNING) {
						applikation.checkState(TaskType.DEFAULT);
					}
				} else {
					if (geandert.containsKey(inkarnation.getName())) {
						applikation.reinit(inkarnation.getInkarnation());
					}
				}
			}

			if (startStopp.getStatus() == Status.CONFIGERROR) {
				startStopp.setStatus(Status.RUNNING);
			} else {
				starteSkript();
			}

		} catch (StartStoppException e) {
			LOGGER.error(e.getLocalizedMessage());
			throw new IllegalStateException("Sollte hier nicht auftreten, da nur geprüfte Skripte verwendet werden!",
					e);
		}
	}

	public void applikationStatusChanged(ApplikationEvent status) {
		boolean allStopped = checkStoppStatus();

		for (OnlineApplikation applikation : applikationen.values()) {
			if (!applikation.getName().equals(status.name)) {
				applikation.checkState(TaskType.DEFAULT);
			}
		}

		if ((startStopp.getStatus() == Status.STOPPING) && allStopped) {
			startStopp.setStatus(Status.STOPPED);
			if (neuStartGeplant) {
				try {
					starteSkript();
					neuStartGeplant = false;
				} catch (StartStoppException e) {
					LOGGER.warning(e.getLocalizedMessage());
				}
			}
		}

		// if ((startStoppStatus == Status.SHUTDOWN) && allStopped) {
		// System.exit(0);
		// }
	}

	private boolean checkStoppStatus() {
		for (OnlineApplikation applikation : applikationen.values()) {
			if (applikation.getStatus() != Applikation.Status.GESTOPPT) {
				return false;
			}
		}
		return true;
	}

	public String getDavConnectionMsg() {
		return davConnector.getConnectionMsg();
	}

	public void starteSkript() throws StartStoppException {
		checkSkriptStart();
		startStopp.setStatus(Status.RUNNING, true);
	}

	private void checkManualStartModus() throws StartStoppException {
		switch (startStopp.getStatus()) {
		case RUNNING:
		case RUNNING_CANCELED:
		case STOPPED:
		case STOPPING_CANCELED:
			break;
		case STOPPING:
		case SHUTDOWN:
		case CONFIGERROR:
		case INITIALIZED:
		default:
			throw new StartStoppException(
					"Eine Applikation kann im Status: " + startStopp.getStatus() + " nicht gestartet werden!");
		}
	}

	private void checkSkriptStart() throws StartStoppException {
		switch (startStopp.getStatus()) {
		case RUNNING:
		case STOPPED:
			break;
		case STOPPING:
		case CONFIGERROR:
		case INITIALIZED:
		case SHUTDOWN:
			throw new StartStoppException(
					"Der Prozessmanager kann im Status " + startStopp.getStatus() + " nicht neu gestartet werden");
		default:
			break;

		}
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

	public ApplikationLog getApplikationLog(String inkarnationsName) throws StartStoppException {
		return getApplikation(inkarnationsName).getLog();
	}

	public RechnerClient getRechner(String rechnerName) {
		return rechnerManager.getClient(rechnerName);
	}

	public List<OnlineApplikation> getKernSystemApplikationen() {
		List<OnlineApplikation> result = new ArrayList<>();
		for (KernSystem ks : aktuelleKonfiguration.getKernSysteme()) {
			OnlineApplikation onlineApplikation = applikationen.get(ks.getInkarnationsName());
			if (onlineApplikation == null) {
				LOGGER.warning("Die Kernsystemapplikation \"" + ks.getInkarnationsName() + "\" wurde nicht gefunden!");
			} else {
				result.add(onlineApplikation);
			}
		}
		return result;
	}

}
