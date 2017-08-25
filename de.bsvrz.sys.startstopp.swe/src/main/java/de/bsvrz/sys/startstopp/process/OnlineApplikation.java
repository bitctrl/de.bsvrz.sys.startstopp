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

import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import de.bsvrz.dav.daf.util.cron.CronDefinition;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartArt;
import de.bsvrz.sys.startstopp.api.jsonschema.StartArt.Option;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus.Status;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.process.dav.DavApplikationStatus;
import de.bsvrz.sys.startstopp.process.os.OSApplikation;
import de.bsvrz.sys.startstopp.process.os.OSApplikationStatus;
import de.bsvrz.sys.startstopp.startstopp.StartStopp;
import de.bsvrz.sys.startstopp.util.NamingThreadFactory;
import de.muspellheim.events.Event;

public final class OnlineApplikation {

	public class ApplikationStatus {

		public final Event<ApplikationStatus> event;
		public final String name;
		public final Applikation.Status status;

		private ApplikationStatus(Event<ApplikationStatus> event, String name, Applikation.Status status) {
			super();
			this.event = event;
			this.name = name;
			this.status = status;
		}
	}

	public enum TaskType {
		DEFAULT, WARTETIMER, INTERVALLTIMER, STOPPFEHLER;
	}

	private static final Debug LOGGER = Debug.getLogger();
	public final Event<ApplikationStatus> onStatusChanged = new Event<>();

	private Consumer<OSApplikationStatus> osApplikationStatusHandler = this::handleOSApplikationStatus;
	private Consumer<StartStoppStatus.Status> prozessManagerStatusHandler = this::prozessMangerStatusChanged;
	private Consumer<DavApplikationStatus> davAppStatusChangedHandler = this::davApplikationStatusChanged;

	private OSApplikation process;

	private ScheduledFuture<?> warteTask;
	private ScheduledExecutorService warteZeitExecutor;

	private ScheduledFuture<?> intervallTask;
	private ScheduledExecutorService intervallExecutor;

	private ScheduledFuture<?> stoppFehlerTask;
	private ScheduledExecutorService stoppFehlerExecutor;

	private ProzessManager prozessManager;
	private long zyklischerStart;

	private OnlineInkarnation inkarnationsHandler;
	private Applikation applikation;

	private String inkarnationsPrefix;

	private List<String> prozessAusgaben = new ArrayList<>();

	public OnlineApplikation(ProzessManager processmanager, OnlineInkarnation onlineInkarnation) {
		this(StartStopp.getInstance(), processmanager, onlineInkarnation);
	}

	public OnlineApplikation(StartStopp startStopp, ProzessManager processmanager,
			OnlineInkarnation onlineInkarnation) {
		this.prozessManager = processmanager;
		this.applikation = new Applikation();
		this.inkarnationsPrefix = startStopp.getInkarnationsPrefix();
		applikation.setInkarnation(onlineInkarnation.getInkarnation());
		this.inkarnationsHandler = onlineInkarnation;
		applikation.setLetzteStartzeit("noch nie gestartet");
		applikation.setLetzteStoppzeit("noch nie gestoppt");

		warteZeitExecutor = Executors
				.newSingleThreadScheduledExecutor(new NamingThreadFactory("Wartezeit: " + getName()));
		intervallExecutor = Executors
				.newSingleThreadScheduledExecutor(new NamingThreadFactory("Intervall: " + getName()));
		stoppFehlerExecutor = Executors
				.newSingleThreadScheduledExecutor(new NamingThreadFactory("StoppFehler: " + getName()));

		this.prozessManager.onManagerStatusChanged.addHandler(prozessManagerStatusHandler);
		this.prozessManager.getDavConnector().onAppStatusChanged.addHandler(davAppStatusChangedHandler);

		updateStatus(Applikation.Status.INSTALLIERT, "");
	}

	private void davApplikationStatusChanged(DavApplikationStatus status) {
		String name = status.name.substring(inkarnationsPrefix.length());
		if (getName().equals(name)) {
			if (status.fertig) {
				if (applikation.getStatus() == Applikation.Status.GESTARTET) {
					updateStatus(Applikation.Status.INITIALISIERT, "");
				} else {
					LOGGER.warning(
							"INITIALISIERT kann nicht gesetzt werden, " + getName() + " ist im Status " + getStatus());
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
		switch (status) {
		case STOPPING:
			if (kannStoppenWarten()) {
				updateStatus(Applikation.Status.STOPPENWARTEN, "Startstopp wird angehalten!");
			}
			break;
		case SHUTDOWN:
			if (kannStoppenWarten()) {
				updateStatus(Applikation.Status.STOPPENWARTEN, "Startstopp wird heruntergefahren!");
			}
			break;
		case CONFIGERROR:
			// TODO Zustand auswerten
			break;
		case INITIALIZED:
			// TODO Zustand auswerten
			break;
		case RUNNING:
			// TODO Zustand auswerten
			break;
		case STOPPED:
			// TODO Zustand auswerten
			break;
		default:
			// TODO Zustand auswerten
			break;
		}
	}

	private boolean kannStoppenWarten() {
		switch (getStatus()) {
		case GESTOPPT:
		case STOPPENWARTEN:
			return false;
		case GESTARTET:
		case INITIALISIERT:
		case INSTALLIERT:
		case STARTENWARTEN:
		default:
			break;

		}
		return true;
	}

	public void checkState(TaskType taskType) {

		switch (getStatus()) {
		case GESTARTET:
		case GESTOPPT:
		case INITIALISIERT:
			LOGGER.finest(applikation.getStatus() + ": " + applikation.getInkarnation().getInkarnationsName()
					+ " keine Aktualisierung möglich");
			break;
		case INSTALLIERT:
			handleInstalliertState(taskType);
			break;
		case STARTENWARTEN:
			handleStartenWartenState(taskType);
			break;
		case STOPPENWARTEN:
			handleStoppenWartenState(taskType);
			break;
		default:
			break;
		}
	}

	public void dispose() {
		this.prozessManager.getDavConnector().onAppStatusChanged.removeHandler(davAppStatusChangedHandler);
		this.prozessManager.onManagerStatusChanged.removeHandler(prozessManagerStatusHandler);

		if (warteTask != null) {
			warteTask.cancel(true);
		}

		if (warteZeitExecutor != null) {
			warteZeitExecutor.shutdown();
		}

		if (intervallTask != null) {
			intervallTask.cancel(true);
		}

		if (intervallExecutor != null) {
			intervallExecutor.shutdown();
		}

		if (stoppFehlerTask != null) {
			stoppFehlerTask.cancel(true);
		}

		if (stoppFehlerExecutor != null) {
			stoppFehlerExecutor.shutdown();
		}

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

	String getName() {
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

	private void handleInstalliertState(TaskType taskType) {

		if( taskType != TaskType.DEFAULT) {
			return;
		}
		
		if (prozessManager.getStatus() != StartStoppStatus.Status.RUNNING) {
			return;
		}

		switch (applikation.getInkarnation().getStartArt().getOption()) {
		case AUTOMATISCH:
			break;
		case INTERVALLRELATIV:
		case INTERVALLABSOLUT:
			try {
				setZyklusTimer();
				updateStatus(Applikation.Status.STARTENWARTEN,
						"Nächster Ausführungszeitpunkt " + DateFormat.getDateTimeInstance().format(
								new Date(System.currentTimeMillis() + intervallTask.getDelay(TimeUnit.MILLISECONDS))));
			} catch (StartStoppException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return;
		case MANUELL:
		default:
			return;
		}

		Set<String> applikationen = prozessManager.waitForKernsystemStart(this);
		if (!applikationen.isEmpty()) {
			updateStatus(Applikation.Status.STARTENWARTEN, "Warte auf Kernsystem: " + applikationen.toString());
			return;
		}

		if (!isKernsystem()) {
			String davConnectionMsg = prozessManager.getDavConnectionMsg();
			if (davConnectionMsg != null) {
				LOGGER.info(applikation.getInkarnation().getInkarnationsName() + ": " + davConnectionMsg);
				updateStatus(Applikation.Status.STARTENWARTEN, davConnectionMsg);
				return;
			}
		}

		StartBedingung startBedingung = getStartBedingung();
		if (startBedingung != null) {
			applikationen = prozessManager.waitForStartBedingung(this);
			if (!applikationen.isEmpty()) {
				updateStatus(Applikation.Status.STARTENWARTEN, "Warte auf : " + applikationen);
				return;
			}
			long warteZeitInMsec;
			try {
				warteZeitInMsec = Util.convertToWarteZeitInMsec(startBedingung.getWartezeit());
			} catch (StartStoppException e) {
				throw new IllegalStateException(
						"Sollte hier nicht passieren, weil nur geprüfte Skripte ausgeführt werden!", e);
			}
			if (warteZeitInMsec > 0) {
				updateStatus(Applikation.Status.STARTENWARTEN, applikation.getStartMeldung());
				setWarteTimer(warteZeitInMsec);
				return;
			}
		}

		starteApplikation();
	}

	public void handleOSApplikationStatus(OSApplikationStatus neuerStatus) {

		switch (neuerStatus) {
		case GESTOPPT:
			if (process != null) {
				process.onStatusChange.removeHandler(osApplikationStatusHandler);
				setWarteTimer(0);
				deactivateZyklusTimer();
				deactivateStoppFehlerTask();
				process = null;
			}
			switch (applikation.getInkarnation().getStartArt().getOption()) {
			case INTERVALLABSOLUT:
			case INTERVALLRELATIV:
				if (getStatus() == Applikation.Status.STOPPENWARTEN
						|| prozessManager.getStatus() != StartStoppStatus.Status.RUNNING) {
					updateStatus(Applikation.Status.GESTOPPT, "");
				} else {
					updateStatus(Applikation.Status.INSTALLIERT, "");
				}
				break;
			default:
				if ((getStatus() == Applikation.Status.STOPPENWARTEN)
						|| prozessManager.getStatus() != StartStoppStatus.Status.RUNNING) {
					updateStatus(Applikation.Status.GESTOPPT, "");
				} else if (applikation.getInkarnation().getStartArt().getNeuStart()) {
					updateStatus(Applikation.Status.INSTALLIERT, "");
				}
				break;
			}
			break;

		case GESTARTET:
			if (applikation.getInkarnation().getInitialize()) {
				updateStatus(Applikation.Status.INITIALISIERT, "");
			} else if (applikation.getStatus() != Applikation.Status.INITIALISIERT) {
				updateStatus(Applikation.Status.GESTARTET, "");
			}
			break;
		case STARTFEHLER:
			updateStatus(Applikation.Status.GESTOPPT, "Fehler beim Starten");
			if (process != null) {
				prozessAusgaben.addAll(process.getProzessAusgabe());
				if (!prozessAusgaben.isEmpty()) {
					applikation.setStartMeldung(prozessAusgaben.get(0));
				}
				process.onStatusChange.removeHandler(osApplikationStatusHandler);
				setWarteTimer(0);
				deactivateZyklusTimer();
				deactivateStoppFehlerTask();
				
				process = null;
			}
			break;
		default:
			break;
		}
	}

	private void handleStartenWartenState(TaskType taskType) {

		if( taskType == TaskType.STOPPFEHLER) {
			return;
		}
		
		if (prozessManager.getStatus() != StartStoppStatus.Status.RUNNING) {
			setWarteTimer(0);
			updateStatus(Applikation.Status.GESTOPPT, "");
			return;
		}

		if (taskType != TaskType.INTERVALLTIMER && intervallTaskIsActive()) {
			setWarteTimer(0);
			updateStatus(Applikation.Status.STARTENWARTEN,
					"Nächster Ausführungszeitpunkt " + DateFormat.getDateTimeInstance().format(
							new Date(System.currentTimeMillis() + intervallTask.getDelay(TimeUnit.MILLISECONDS))));
			return;
		}

		Set<String> applikationen = prozessManager.waitForKernsystemStart(this);
		if (!applikationen.isEmpty()) {
			setWarteTimer(0);
			updateStatus(Applikation.Status.STARTENWARTEN, "Warte auf Kernsystem: " + applikationen);
			return;
		}

		if (!isKernsystem()) {
			String davConnectionMsg = prozessManager.getDavConnectionMsg();
			if (davConnectionMsg != null) {
				setWarteTimer(0);
				LOGGER.info(applikation.getInkarnation().getInkarnationsName() + ": " + davConnectionMsg);
				updateStatus(Applikation.Status.STARTENWARTEN, davConnectionMsg);
				return;
			}
		}

		StartBedingung startBedingung = getStartBedingung();
		if (startBedingung != null) {
			applikationen = prozessManager.waitForStartBedingung(this);
			if (!applikationen.isEmpty()) {
				setWarteTimer(0);
				updateStatus(Applikation.Status.STARTENWARTEN, "Warte auf : " + applikationen);
				return;
			}
			if (taskType != TaskType.WARTETIMER) {
				if (warteTaskIsActive()) {
					updateStatus(Applikation.Status.STARTENWARTEN, applikation.getStartMeldung());
					return;
				}
				long warteZeitInMsec;
				try {
					warteZeitInMsec = Util.convertToWarteZeitInMsec(startBedingung.getWartezeit());
				} catch (StartStoppException e) {
					throw new IllegalStateException(
							"Sollte hier nicht passieren, weil nur geprüfte Skripte ausgeführt werden!", e);
				}
				if (warteZeitInMsec > 0) {
					updateStatus(Applikation.Status.STARTENWARTEN, "Wartezeit bis " + DateFormat.getDateTimeInstance()
							.format(new Date(System.currentTimeMillis() + warteZeitInMsec)));
					setWarteTimer(warteZeitInMsec);
					return;
				}
			}
		}

		if (warteTaskIsActive()) {
			return;
		}

		starteApplikation();
		applikation.setStartMeldung("");
	}

	private void handleStoppenWartenState(TaskType timerType) {

		if( timerType == TaskType.STOPPFEHLER) {
			// TODO Behandeln
		}
		
		Set<String> applikationen = prozessManager.waitForKernsystemStopp(this);
		if (!applikationen.isEmpty()) {
			setWarteTimer(0);
			updateStatus(Applikation.Status.STOPPENWARTEN, "Kernsystem wartet auf: " + applikationen);
			return;
		}

		StoppBedingung stoppBedingung = getStoppBedingung();
		if (stoppBedingung != null) {
			applikationen = prozessManager.waitForStoppBedingung(this);
			if (!applikationen.isEmpty()) {
				setWarteTimer(0);
				updateStatus(Applikation.Status.STOPPENWARTEN, "Warte auf : " + applikationen);
				return;
			}
			if (timerType != TaskType.WARTETIMER) {
				if (warteTaskIsActive()) {
					updateStatus(Applikation.Status.STOPPENWARTEN, applikation.getStartMeldung());
					return;
				}
				long warteZeitInMsec;
				try {
					warteZeitInMsec = Util.convertToWarteZeitInMsec(stoppBedingung.getWartezeit());
				} catch (StartStoppException e) {
					throw new IllegalStateException(
							"Sollte hier nicht passieren, weil nur geprüfte Skripte ausgeführt werden!", e);
				}
				if (warteZeitInMsec > 0) {
					updateStatus(Applikation.Status.STOPPENWARTEN, "Wartezeit bis " + DateFormat.getDateTimeInstance()
							.format(new Date(System.currentTimeMillis() + warteZeitInMsec)));
					setWarteTimer(warteZeitInMsec);
					return;
				}
			} else {
				setWarteTimer(0);
			}
		}

		if (warteTaskIsActive()) {
			return;
		}

		try {
			prozessManager.stoppeApplikation(applikation.getInkarnation().getInkarnationsName());
			activateStoppFehlerTask();
		} catch (StartStoppException e) {
			throw new IllegalStateException("Sollte hier nicht passieren, weil die Applikation sich selbst beendet!",
					e);
		}
	}

	private void activateStoppFehlerTask() {
		deactivateStoppFehlerTask();
		stoppFehlerTask = stoppFehlerExecutor.schedule(() -> checkState(TaskType.STOPPFEHLER), 30, TimeUnit.SECONDS);
	}
	
	private void deactivateStoppFehlerTask() {
		if (stoppFehlerTask != null) {
			stoppFehlerTask.cancel(true);
			stoppFehlerTask = null;
		}
	}
	
	private boolean intervallTaskIsActive() {
		return (intervallTask != null) && intervallTask.getDelay(TimeUnit.MILLISECONDS) > 0;
	}

	public boolean isKernsystem() {
		return inkarnationsHandler.isKernSystem();
	}

	public boolean isTransmitter() {
		return inkarnationsHandler.isTransmitter();
	}

	private void setWarteTimer(long warteZeitInMsec) {

		if (warteTask != null) {
			warteTask.cancel(true);
		}

		if (warteZeitInMsec <= 0) {
			return;
		}

		warteTask = warteZeitExecutor.schedule(() -> checkState(TaskType.WARTETIMER), warteZeitInMsec,
				TimeUnit.MILLISECONDS);
	}

	private void deactivateZyklusTimer() {
		if (intervallTask != null) {
			intervallTask.cancel(true);
			intervallTask = null;
		}
	}
	
	private void setZyklusTimer() throws StartStoppException {

		zyklischerStart = 0;

		StartArt startArt = applikation.getInkarnation().getStartArt();
		switch (startArt.getOption()) {
		case INTERVALLABSOLUT:
			zyklischerStart = new CronDefinition(startArt.getIntervall()).nextScheduledTime(System.currentTimeMillis());
			break;
		case INTERVALLRELATIV:
			zyklischerStart = ManagementFactory.getRuntimeMXBean().getStartTime();
			long intervalle = (System.currentTimeMillis() - zyklischerStart)
					/ Util.convertToWarteZeitInMsec(startArt.getIntervall());
			zyklischerStart += (intervalle + 1) * Util.convertToWarteZeitInMsec(startArt.getIntervall());
			break;
		default:
			return;
		}

		if (intervallTask != null) {
			intervallTask.cancel(true);
		}

		intervallTask = intervallExecutor.schedule(() -> {
			intervallTask = null;
			checkState(TaskType.INTERVALLTIMER);
		}, zyklischerStart - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	public void starteApplikation() {
		prozessAusgaben.clear();
		updateStatus(Applikation.Status.GESTARTET, "Start initialisiert");
		process = new OSApplikation(getName(), applikation.getInkarnation().getApplikation());
		process.setProgrammArgumente(getApplikationsArgumente());
		process.onStatusChange.addHandler(osApplikationStatusHandler);
		process.start();
	}

	public void startSystemProcess() throws StartStoppException {
		switch (applikation.getStatus()) {
		case INSTALLIERT:
		case GESTOPPT:
		case STARTENWARTEN:
			starteApplikation();
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
		if (process == null) {
			updateStatus(Applikation.Status.GESTOPPT, "");
			switch (applikation.getInkarnation().getStartArt().getOption()) {
			case INTERVALLABSOLUT:
			case INTERVALLRELATIV:
				if (starteNaechstenZyklus(prozessManager.getStatus())) {
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
			process.kill();
		}
	}

	private boolean starteNaechstenZyklus(Status status) {
		return status == StartStoppStatus.Status.RUNNING;
	}

	@Override
	public String toString() {
		return getName();
	}

	public void updateStatus(Applikation.Status status, String message) {

		applikation.setStartMeldung(message);

		Applikation.Status oldStatus = applikation.getStatus();
		if (oldStatus != status) {
			applikation.setStatus(status);
			onStatusChanged.send(new ApplikationStatus(onStatusChanged, this.getName(), status));
		}
	}

	private boolean warteTaskIsActive() {
		return (warteTask != null) && warteTask.getDelay(TimeUnit.MILLISECONDS) > 0;
	}
}
