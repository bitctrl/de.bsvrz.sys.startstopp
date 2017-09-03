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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.ApplikationLog;
import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartArt;
import de.bsvrz.sys.startstopp.api.jsonschema.StartArt.Option;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StartFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus.Status;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.process.dav.DavApplikationStatus;
import de.bsvrz.sys.startstopp.process.os.OSApplikation;
import de.bsvrz.sys.startstopp.process.os.OSApplikationStatus;
import de.bsvrz.sys.startstopp.process.os.OSTools;
import de.bsvrz.sys.startstopp.startstopp.StartStopp;
import de.muspellheim.events.Event;

public final class OnlineApplikation {

	public enum TaskType {
		DEFAULT, WARTETIMER, INTERVALLTIMER, STOPPFEHLER;
	}

	private static final Debug LOGGER = Debug.getLogger();
	public final Event<ApplikationEvent> onStatusChanged = new Event<>();

	private Consumer<OSApplikationStatus> osApplikationStatusHandler = this::handleOSApplikationStatus;
	private Consumer<StartStoppStatus.Status> prozessManagerStatusHandler = this::prozessMangerStatusChanged;
	private Consumer<DavApplikationStatus> davAppStatusChangedHandler = this::davApplikationStatusChanged;

	private OSApplikation process;

	private ProzessManager prozessManager;

	private OnlineInkarnation inkarnation;
	private Applikation applikation;

	private String inkarnationsPrefix;
	private boolean manuellGestartetOderGestoppt;

	private final List<String> prozessAusgaben = new ArrayList<>();
	private int startFehlerCounter;
	private OnlineApplikationTimer onlineApplikationTimer;

	public OnlineApplikation(ProzessManager processmanager, OnlineInkarnation onlineInkarnation) {
		this(StartStopp.getInstance(), processmanager, onlineInkarnation);
	}

	public OnlineApplikation(StartStopp startStopp, ProzessManager processmanager,
			OnlineInkarnation onlineInkarnation) {
		this.prozessManager = processmanager;
		this.applikation = new Applikation();
		this.inkarnationsPrefix = startStopp.getInkarnationsPrefix();
		applikation.setInkarnation(onlineInkarnation.getInkarnation());
		this.inkarnation = onlineInkarnation;
		applikation.setLetzteStartzeit("noch nie gestartet");
		applikation.setLetzteStoppzeit("noch nie gestoppt");

		onlineApplikationTimer = new OnlineApplikationTimer(this);

		this.prozessManager.onStartStoppStatusChanged.addHandler(prozessManagerStatusHandler);
		this.prozessManager.getDavConnector().onAppStatusChanged.addHandler(davAppStatusChangedHandler);

		updateStatus(Applikation.Status.INSTALLIERT, "");
	}

	private void davApplikationStatusChanged(DavApplikationStatus status) {
		String name = status.name.substring(inkarnationsPrefix.length());
		if (getName().equals(name)) {
			if (status.fertig) {
				if (applikation.getStatus() == Applikation.Status.GESTARTET
						|| applikation.getStatus() == Applikation.Status.INITIALISIERT) {
					updateStatus(Applikation.Status.INITIALISIERT, "");
				}
			} else {
				switch (getApplikation().getInkarnation().getInkarnationsTyp()) {
				case DAV:
				case WRAPPED:
					applikation.setStartMeldung("Keine Fertigmeldung vom Datenverteiler");
					break;
				case EXTERN:
				default:
					break;
				}
			}
		}
	}

	private void prozessMangerStatusChanged(StartStoppStatus.Status status) {

		Applikation.Status appStatus = getStatus();

		switch (status) {
		case STOPPING:
			if ((appStatus != Applikation.Status.GESTOPPT) && (appStatus != Applikation.Status.STOPPENWARTEN)) {
				if ((appStatus == Applikation.Status.GESTARTET) || (appStatus == Applikation.Status.INITIALISIERT)) {
					manuellGestartetOderGestoppt = false;
					updateStatus(Applikation.Status.STOPPENWARTEN, "Startstopp wird angehalten!");
				} else {
					updateStatus(Applikation.Status.GESTOPPT, "Startstopp wird angehalten!");
				}
			}
			break;
		case SHUTDOWN:
			if ((appStatus != Applikation.Status.GESTOPPT) && (appStatus != Applikation.Status.STOPPENWARTEN)) {
				if ((appStatus == Applikation.Status.GESTARTET) || (appStatus == Applikation.Status.INITIALISIERT)) {
					manuellGestartetOderGestoppt = false;
					updateStatus(Applikation.Status.STOPPENWARTEN, "Startstopp wird heruntergefahren!");
				} else {
					updateStatus(Applikation.Status.GESTOPPT, "Startstopp wird heruntergefahren!");
				}
			}
			break;
		case RUNNING:
			startFehlerCounter = 0;
			if (getStartArtOption() != StartArt.Option.MANUELL) {
				if ((appStatus == Applikation.Status.GESTOPPT) || (appStatus == Applikation.Status.STOPPENWARTEN)) {
					updateStatus(Applikation.Status.INSTALLIERT, "Prozessmanager gestartet");
				}
				if (appStatus == Applikation.Status.INSTALLIERT) {
					checkState(TaskType.DEFAULT);
				}
			}
			break;
		case CONFIGERROR:
		case INITIALIZED:
			LOGGER.warning("Unerwarteter Status des Prozessmanagers: " + status);
			break;
		case STOPPED:
		default:
			break;
		}
	}

	void checkState(TaskType taskType) {
		getStatusHandler().wechsleStatus(taskType, prozessManager.getStartStoppStatus());
	}

	private OnlineApplikationStatus getStatusHandler() {

		OnlineApplikationStatus handler = null;

		switch (applikation.getStatus()) {
		case GESTARTET:
			handler = new GestartetStatus(this);
			break;
		case GESTOPPT:
			handler = new GestopptStatus(this);
			break;
		case INITIALISIERT:
			handler = new InitialisiertStatus(this);
			break;
		case INSTALLIERT:
			handler = new InstalliertStatus(this);
			break;
		case STARTENWARTEN:
			handler = new StartenWartenStatus(this);
			break;
		case STOPPENWARTEN:
			handler = new StoppenWartenStatus(this);
			break;
		default:
			throw new IllegalStateException("Unbekannter Applikationsstatus: " + applikation.getStatus());

		}
		return handler;
	}

	public void dispose() {
		this.prozessManager.getDavConnector().onAppStatusChanged.removeHandler(davAppStatusChangedHandler);
		this.prozessManager.onStartStoppStatusChanged.removeHandler(prozessManagerStatusHandler);

		onlineApplikationTimer.dispose();

		if (process != null) {
			process.onStatusChange.removeHandler(osApplikationStatusHandler);
		}
	}

	public Applikation getApplikation() {
		return applikation;
	}

	private String getApplikationsArgumente() {
		StringBuilder builder = new StringBuilder(1024);
		for (String argument : applikation.getInkarnation().getAufrufParameter()) {
			if (builder.length() > 0) {
				builder.append(' ');
			}
			builder.append(argument);
		}

		if (applikation.getInkarnation().getMitInkarnationsName()) {
			builder.append(" -inkarnationsName=");
			builder.append(inkarnationsPrefix);
			builder.append(applikation.getInkarnation().getInkarnationsName());
		}

		return builder.toString();
	}

	public String getName() {
		return applikation.getInkarnation().getInkarnationsName();
	}

	public Option getStartArtOption() {
		return applikation.getInkarnation().getStartArt().getOption();
	}

	public StartBedingung getStartBedingung() {
		return applikation.getInkarnation().getStartBedingung();
	}

	public Applikation.Status getStatus() {
		Applikation.Status result = applikation.getStatus();
		if (result == null) {
			result = Applikation.Status.INSTALLIERT;
		}
		return result;
	}

	public StoppBedingung getStoppBedingung() {
		return applikation.getInkarnation().getStoppBedingung();
	}

	public void handleOSApplikationStatus(OSApplikationStatus neuerStatus) {

		switch (neuerStatus) {
		case GESTOPPT:
			handleOsApplikationStopped();
			break;

		case GESTARTET:
			handleOsApplikationStarted();
			break;
		case STARTFEHLER:
			handleOsApplikationStartFehler();
			break;
		default:
			break;
		}
	}

	private void handleOsApplikationStartFehler() {
		updateStatus(Applikation.Status.GESTOPPT, "Fehler beim Starten");
		if (process != null) {
			updateProzessAusgaben();
			if (!prozessAusgaben.isEmpty()) {
				applikation.setStartMeldung(prozessAusgaben.get(0));
			}
			process.onStatusChange.removeHandler(osApplikationStatusHandler);
			onlineApplikationTimer.clear();
			process = null;
		}

		if (!manuellGestartetOderGestoppt) {

			StartFehlerVerhalten fehlerVerhalten = applikation.getInkarnation().getStartFehlerVerhalten();
			if (fehlerVerhalten != null) {
				String wiederholungenStr = fehlerVerhalten.getWiederholungen();
				if (wiederholungenStr != null) {
					if (startFehlerCounter++ <= Integer.parseInt(wiederholungenStr)) {
						if (prozessManager.getStartStoppStatus() == Status.RUNNING) {
							updateStatus(Applikation.Status.INSTALLIERT, "Wiederholung nach Startfehler");
						} else {
							updateStatus(Applikation.Status.GESTOPPT, "");
						}
						return;
					}
				}
				switch (fehlerVerhalten.getOption()) {
				case ABBRUCH:
					CompletableFuture.runAsync(
							() -> prozessManager.setStartStoppStatus(StartStoppStatus.Status.RUNNING_CANCELED));
					break;
				case BEENDEN:
					CompletableFuture.runAsync(() -> prozessManager.stoppeSkript());
					break;
				case IGNORIEREN:
				default:
					break;
				}
			}
		}
	}

	private void handleOsApplikationStarted() {
		if (applikation.getInkarnation().getInitialize()) {
			updateStatus(Applikation.Status.INITIALISIERT, "");
		} else if (applikation.getStatus() != Applikation.Status.INITIALISIERT) {
			updateStatus(Applikation.Status.GESTARTET, "");
		}
	}

	private void handleOsApplikationStopped() {

		if (getStatus() == Applikation.Status.GESTARTET) {
			handleOsApplikationStartFehler();
			return;
		}

		if (process != null) {
			process.onStatusChange.removeHandler(osApplikationStatusHandler);
			onlineApplikationTimer.clear();
			process = null;
		}

		switch (applikation.getInkarnation().getStartArt().getOption()) {
		case INTERVALLABSOLUT:
		case INTERVALLRELATIV:
			if (getStatus() == Applikation.Status.STOPPENWARTEN) {
				updateStatus(Applikation.Status.GESTOPPT, "");
			} else {
				updateStatus(Applikation.Status.INSTALLIERT, "");
			}
			break;
		default:
			if ((prozessManager.getStartStoppStatus() == StartStoppStatus.Status.RUNNING)
					&& applikation.getInkarnation().getStartArt().getNeuStart()) {
				updateStatus(Applikation.Status.INSTALLIERT, "");
			} else {
				updateStatus(Applikation.Status.GESTOPPT, "");
			}
			break;
		}
	}

	private void updateProzessAusgaben() {
		if (process != null) {
			prozessAusgaben.clear();
			prozessAusgaben.addAll(process.getProzessAusgabe());
		}
	}

	public boolean isKernsystem() {
		return inkarnation.isKernSystem();
	}

	public boolean isTransmitter() {
		return inkarnation.isTransmitter();
	}

	public void starteOSApplikation() {
		prozessAusgaben.clear();
		process = new OSApplikation(getName(), applikation.getInkarnation().getApplikation());
		process.setProgrammArgumente(getApplikationsArgumente());
		process.onStatusChange.addHandler(osApplikationStatusHandler);
		process.start();
		applikation.setLetzteStartzeit(DateFormat.getDateTimeInstance().format(new Date()));
	}

	public void starteApplikationManuell() throws StartStoppException {

		switch (applikation.getStatus()) {
		case INSTALLIERT:
		case GESTOPPT:
		case STARTENWARTEN:
			manuellGestartetOderGestoppt = true;
			switch (getApplikation().getInkarnation().getStartArt().getOption()) {
			case AUTOMATISCH:
			case INTERVALLABSOLUT:
			case INTERVALLRELATIV:
				updateStatus(Applikation.Status.INSTALLIERT, "Manueller Start");
				break;
			case MANUELL:
				starteOSApplikation();
				break;
			default:
				break;

			}
			break;
		case GESTARTET:
		case INITIALISIERT:
		case STOPPENWARTEN:
			throw new StartStoppException(
					"Applikation kann im Status \"" + applikation.getStatus() + "\" nicht gestartet werden");
		default:
			break;
		}
	}

	public void stoppeApplikation() {
		applikation.setLetzteStoppzeit(DateFormat.getDateTimeInstance().format(new Date()));
		if (process == null) {
			updateStatus(Applikation.Status.GESTOPPT, "");
			switch (applikation.getInkarnation().getStartArt().getOption()) {
			case INTERVALLABSOLUT:
			case INTERVALLRELATIV:
				if (starteNaechstenZyklus(prozessManager.getStartStoppStatus())) {
					updateStatus(Applikation.Status.STARTENWARTEN, "Warte auf nächsten Start");
				} else {
					updateStatus(Applikation.Status.GESTOPPT, "Zyklische Ausführung angehalten");
				}
				break;
			case AUTOMATISCH:
			case MANUELL:
				updateStatus(Applikation.Status.GESTOPPT, "");
				break;
			default:
				break;
			}
		} else {
			try {
				prozessManager.getDavConnector().stoppApplikation(getName());
			} catch (StartStoppException e) {
				Debug.getLogger()
						.warning("Die Applikation \"" + getName()
								+ "\" konnte nicht über die Dav-Terminierungsschnittstelle beendet werden: "
								+ e.getLocalizedMessage());
				if (process.terminateSupported()) {
					process.terminate();
				} else {
					process.kill();
				}
			}
		}
	}

	private boolean starteNaechstenZyklus(Status status) {
		return status == StartStoppStatus.Status.RUNNING;
	}

	@Override
	public String toString() {
		return getName();
	}

	boolean updateStatus(Applikation.Status status, String message) {

		applikation.setStartMeldung(message);
		Applikation.Status oldStatus = applikation.getStatus();
		if (oldStatus != status) {
			applikation.setStatus(status);
			LOGGER.info("Statuswechsel " + getName() + ": " + oldStatus  + " --> " + getStatus());
			prozessManager.getDavConnector().sendeStatusBetriebsMeldung(this);
			onStatusChanged.send(new ApplikationEvent(onStatusChanged, this.getName(), status));
			checkState(TaskType.DEFAULT);
			return true;
		}

		return false;
	}

	public ApplikationLog getLog() {
		ApplikationLog log = new ApplikationLog().withInkarnation(getName());
		updateProzessAusgaben();
		if (prozessAusgaben.isEmpty()) {
			String startMeldung = applikation.getStartMeldung();
			if ((startMeldung != null) && !startMeldung.trim().isEmpty()) {
				log.getMessages().add(startMeldung.trim());
			}
		} else {
			log.getMessages().addAll(prozessAusgaben);
		}
		return log;
	}

	public void reinit(Inkarnation newInkarnation) throws StartStoppException {
		applikation.setInkarnation(newInkarnation);
		startFehlerCounter = 0;
		if (prozessManager.getStartStoppStatus() == Status.RUNNING) {
			prozessManager.restarteApplikation(getName());
		}
	}

	public boolean requestStopp(String message) {
		boolean result = false;
		switch (getStatus()) {
		case GESTARTET:
		case INITIALISIERT:
		case STOPPENWARTEN:
			updateStatus(Applikation.Status.STOPPENWARTEN, message);
			result = true;
			break;
		case GESTOPPT:
		case INSTALLIERT:
		case STARTENWARTEN:
			updateStatus(Applikation.Status.GESTOPPT, message);
			break;
		default:
			break;
		}
		return result;
	}

	public void requestStart(String message) {
		updateStatus(Applikation.Status.INSTALLIERT, message);
	}

	boolean isManuellGestartetOderGestoppt() {
		return manuellGestartetOderGestoppt;
	}

	OnlineApplikationTimer getOnlineApplikationTimer() {
		return onlineApplikationTimer;
	}

	public String kernSystemVerfuegbar() {

		Set<String> applikationen = new LinkedHashSet<>();
		for (OnlineApplikation ksApp : prozessManager.getKernSystemApplikationen()) {
			if (ksApp.getName().equals(getName())) {
				break;
			}
			switch (ksApp.getStatus()) {
			case GESTARTET:
			case INITIALISIERT:
				break;
			default:
				applikationen.add(ksApp.getName());
			}
		}

		if (!applikationen.isEmpty()) {
			return "Warte auf Kernsystem: " + applikationen.toString();
		}

		String message = null;
		if (!isKernsystem()) {
			message = prozessManager.getDavConnectionMsg();
		}

		return message;
	}

	public String kernSystemKannGestopptWerden() {

		Set<String> applikationen = new LinkedHashSet<>();
		OnlineApplikation transmitter = null;

		if (isKernsystem()) {
			for (OnlineApplikation onlineApplikation : prozessManager.getApplikationen()) {
				if (this.equals(onlineApplikation)) {
					continue;
				}
				if (onlineApplikation.isKernsystem()) {
					if (onlineApplikation.isTransmitter()) {
						transmitter = onlineApplikation;
					}
				} else if (onlineApplikation.getStatus() != Applikation.Status.GESTOPPT) {
					applikationen.add(onlineApplikation.getName());
				}
			}

			if (OSTools.isWindows()) {
				if (transmitter != null) {
					applikationen.add(transmitter.getName());
				}
			} else {
				boolean found = false;
				for (OnlineApplikation onlineApplikation : prozessManager.getKernSystemApplikationen()) {
					if (found) {
						if (onlineApplikation.getStatus() != Applikation.Status.GESTOPPT) {
							applikationen.add(onlineApplikation.getName());
						}
					} else if (onlineApplikation.getName().equals(getName())) {
						found = true;
					}
				}
			}
		}
		if (!applikationen.isEmpty()) {
			return "Kernsystem wartet auf: " + applikationen;
		}
		return null;
	}

	public StoppBedingungStatus getStoppBedingungStatus() {
		return new StoppBedingungStatus(this);
	}

	public StartBedingungStatus getStartbedingungStatus() {
		return new StartBedingungStatus(this);
	}

	ProzessManager getProzessManager() {
		return prozessManager;
	}

}
