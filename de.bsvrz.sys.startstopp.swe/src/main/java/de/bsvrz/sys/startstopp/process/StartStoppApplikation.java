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
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.process.ProzessManager.StartStoppMode;

public class StartStoppApplikation extends Applikation {

	public enum TaskType {
		DEFAULT, WARTETIMER, INTERVALLTIMER;
	}

	public class SystemProzessListener implements InkarnationsProzessListener {

		@Override
		public void statusChanged(InkarnationsProzessStatus neuerStatus) {

			switch (neuerStatus) {
			case GESTOPPT:
				updateStatus(Applikation.Status.GESTOPPT, "");
				if (process != null) {
					process.removeProzessListener(this);
					process = null;
				}
				break;
			case GESTARTET:
				if (getInkarnation().getInitialize()) {
					updateStatus(Applikation.Status.INITIALISIERT, "");
				} else {
					updateStatus(Applikation.Status.GESTARTET, "");
				}
				break;
			case STARTFEHLER:
				updateStatus(Applikation.Status.GESTOPPT, "Fehler beim Starten");
				if (process != null) {
					process.removeProzessListener(this);
					process = null;
				}
				break;
			default:
				break;
			}
		}
	}

	private static final Debug LOGGER = Debug.getLogger();

	private transient InkarnationsProzessIf process = null;
	private transient List<ManagedApplikationListener> listeners = new ArrayList<>();

	private transient ScheduledFuture<?> warteTask;
	private transient TimerTask intervallTask;
	private transient ProzessManager prozessManager;
	private transient SystemProzessListener systemProzessListener = new SystemProzessListener();

	public StartStoppApplikation(ProzessManager processmanager, StartStoppInkarnation inkarnation) {
		this.prozessManager = processmanager;
		setInkarnation(inkarnation);
		updateStatus(Applikation.Status.INSTALLIERT, "");
		setLetzteStartzeit("noch nie gestartet");
		setLetzteStoppzeit("noch nie gestoppt");
	}

	public void addManagedApplikationListener(ManagedApplikationListener listener) {
		listeners.add(listener);
	}

	public void dispose() {
		if (intervallTask != null) {
			intervallTask.cancel();
		}
		if (process != null) {
			process.removeProzessListener(systemProzessListener);
		}
	}

	private void fireStatusChanged(Applikation.Status oldStatus, Applikation.Status newStatus) {
		List<ManagedApplikationListener> receiver;
		synchronized (listeners) {
			receiver = new ArrayList<>(listeners);
		}

		for (ManagedApplikationListener listener : receiver) {
			listener.applicationStatusChanged(this, oldStatus, newStatus);
		}
	}

	private String getApplikationsArgumente() {
		StringBuilder builder = new StringBuilder(1024);
		for (String argument : getInkarnation().getAufrufParameter()) {
			if (builder.length() > 0) {
				builder.append(' ');
			}
			builder.append(argument);
		}

		if (getInkarnation().getMitInkarnationsName()) {
			builder.append(" -inkarnationsName=");
			builder.append(prozessManager.getInkarnationsPrefix());
			builder.append(getInkarnation().getInkarnationsName());
		}

		return builder.toString();
	}

	public StartBedingung getStartBedingung() {
		return getInkarnation().getStartBedingung();
	}

	public StoppBedingung getStoppBedingung() {
		return getInkarnation().getStoppBedingung();
	}

	public boolean isKernsystem() {
		return getInkarnation().isKernSystem();
	}

	public void removeManagedApplikationListener(ManagedApplikationListener listener) {
		listeners.remove(listener);
	}

	public void starteApplikation() {
		updateStatus(Applikation.Status.GESTARTET, "Start initialisiert");
		process = new InkarnationsProzess();
		process.setInkarnationsName(getInkarnation().getInkarnationsName());
		process.setProgramm(getInkarnation().getApplikation());
		process.setProgrammArgumente(getApplikationsArgumente());
		process.addProzessListener(systemProzessListener);
		process.start();
	}

	public void startSystemProcess() throws StartStoppException {
		switch (getStatus()) {
		case INSTALLIERT:
		case GESTOPPT:
		case STARTENWARTEN:
			starteApplikation();
			break;
		case GESTARTET:
		case INITIALISIERT:
		case STOPPENWARTEN:
			throw new StartStoppException("Applikation kann im Status \"" + getStatus() + "\" nicht gestartet werden");
		default:
			break;
		}
	}

	public void stoppeApplikation(boolean force) {
		if (process == null) {
			updateStatus(Applikation.Status.GESTOPPT, "");
			switch (getInkarnation().getStartArt().getOption()) {
			case INTERVALLABSOLUT:
			case INTERVALLRELATIV:
				if (force) {
					updateStatus(Applikation.Status.GESTOPPT, "Zyklische Ausführung angehalten");
				} else {
					updateStatus(Applikation.Status.STARTENWARTEN, "Warte auf nächsten Start");
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

	public void stoppSystemProcess() throws StartStoppException {

		switch (getStatus()) {
		case INSTALLIERT:
		case GESTOPPT:
		case STOPPENWARTEN:
//			throw new StartStoppException("Applikation kann im Status \"" + getStatus() + "\" nicht gestoppt werden");

		case GESTARTET:
		case INITIALISIERT:
		case STARTENWARTEN:
			break;
		default:
			break;
		}

		stoppeApplikation(true);
	}

	public void updateStatus(Applikation.Status status, String message) {
		Applikation.Status oldStatus = getStatus();
		if (oldStatus != status) {
			setStatus(status);
			fireStatusChanged(oldStatus, status);
		}
		setStartMeldung(message);
	}

	public boolean isTransmitter() {
		return getInkarnation().isTransmitter();
	}

	@Override
	public StartStoppInkarnation getInkarnation() {
		return (StartStoppInkarnation) super.getInkarnation();
	}

	public void checkState() {
		checkState(TaskType.DEFAULT);
	}

	private void handleInstalliertState() {

		if (prozessManager.getStatus() != ProzessManager.Status.RUNNING) {
			return;
		}

		switch (getInkarnation().getStartArt().getOption()) {
		case AUTOMATISCH:
		case INTERVALLABSOLUT:
		case INTERVALLRELATIV:
			// TODO Intervallstarts implementieren
			break;
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
				LOGGER.info(getInkarnation().getInkarnationsName() + ": " + davConnectionMsg);
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
			int warteZeitInMsec = convertToWarteZeitInMsec(startBedingung.getWartezeit());
			if (warteZeitInMsec > 0) {
				updateStatus(Applikation.Status.STARTENWARTEN, getStartMeldung());
				setWarteTimer(warteZeitInMsec);
				return;
			}
		}

		starteApplikation();
	}

	private void handleStartenWartenState(TaskType timerType) {

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
				LOGGER.info(getInkarnation().getInkarnationsName() + ": " + davConnectionMsg);
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
			if (timerType != TaskType.WARTETIMER) {
				if (warteTaskIsActive()) {
					updateStatus(Applikation.Status.STARTENWARTEN, getStartMeldung());
					return;
				}
				int warteZeitInMsec = convertToWarteZeitInMsec(startBedingung.getWartezeit());
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
		setStartMeldung("");
	}

	private void handleStoppenWartenState(TaskType timerType) throws StartStoppException {

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
					updateStatus(Applikation.Status.STOPPENWARTEN, getStartMeldung());
					return;
				}
				int warteZeitInMsec = convertToWarteZeitInMsec(stoppBedingung.getWartezeit());
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

		prozessManager.stoppeApplikation(getInkarnation().getInkarnationsName(), StartStoppMode.SKRIPT);
	}

	private int convertToWarteZeitInMsec(String warteZeitStr) {
		// TODO Auto-generated method stub
		return Integer.parseInt(warteZeitStr) * 1000;
	}

	private void setWarteTimer(int warteZeitInMsec) {

		if (warteTask != null) {
			warteTask.cancel(true);
		}

		if (warteZeitInMsec <= 0) {
			return;
		}

		warteTask = Executors.newSingleThreadScheduledExecutor(new NamingThreadFactory("Wartezeit: " + getName())).schedule(() -> checkState(TaskType.WARTETIMER), warteZeitInMsec,
				TimeUnit.MILLISECONDS);
	}

	public void checkState(TaskType timerType) {

		switch (getStatus()) {
		case GESTARTET:
		case GESTOPPT:
		case INITIALISIERT:
			LOGGER.finest(
					getStatus() + ": " + getInkarnation().getInkarnationsName() + " keine Aktualisierung möglich");
			break;
		case INSTALLIERT:
			handleInstalliertState();
			break;
		case STARTENWARTEN:
			handleStartenWartenState(timerType);
			break;
		case STOPPENWARTEN:
			try {
				handleStoppenWartenState(timerType);
			} catch (StartStoppException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	}

	private boolean warteTaskIsActive() {
		return (warteTask != null) && warteTask.getDelay(TimeUnit.MILLISECONDS) > 0;
	}
	
	@Override
	public String toString() {
		return getName();
	}

	private String getName() {
		return getInkarnation().getInkarnationsName();
	}
	
}
