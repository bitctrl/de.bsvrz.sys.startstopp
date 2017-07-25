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
import java.util.Timer;
import java.util.TimerTask;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.config.StartStoppException;

public class StartStoppApplikation extends Applikation {

	public class WarteTask extends TimerTask {
		@Override
		public void run() {
			updateStatus(TimerType.WARTETIMER);
		}

		public boolean isActive() {
			return System.currentTimeMillis() < scheduledExecutionTime();
		}
	}

	public class SystemProzessListener implements InkarnationsProzessListener {

		@Override
		public void statusChanged(InkarnationsProzessStatus neuerStatus) {

			switch (neuerStatus) {
			case GESTOPPT:
				updateStatus(Applikation.Status.GESTOPPT);
				if (process != null) {
					process.removeProzessListener(this);
					process = null;
				}
				break;
			case GESTARTET:
				if (inkarnation.getInitialize()) {
					updateStatus(Applikation.Status.INITIALISIERT);
				} else {
					updateStatus(Applikation.Status.GESTARTET);
				}
				break;
			case STARTFEHLER:
				updateStatus(Applikation.Status.GESTOPPT);
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

	private enum TimerType {
		NOTIMER, WARTETIMER, INTERVALLTIMER;
	}

	private static final Debug LOGGER = Debug.getLogger();

	private StartStoppInkarnation inkarnation;
	private InkarnationsProzessIf process = null;

	private List<ManagedApplikationListener> listeners = new ArrayList<>();

	private String reason;
	private Timer timer;
	private WarteTask warteTask;
	private TimerTask intervallTask;
	private ProcessManager processManager;
	private SystemProzessListener systemProzessListener = new SystemProzessListener();

	public StartStoppApplikation(ProcessManager processmanager, StartStoppInkarnation inkarnation) {

		this.processManager = processmanager;
		this.inkarnation = inkarnation;
		setInkarnationsName(inkarnation.getInkarnationsName());
		updateStatus(Applikation.Status.INSTALLIERT);
		setApplikation(inkarnation.getApplikation());
		getArguments().addAll(inkarnation.getAufrufParameter());
		setLetzteStartzeit("noch nie gestartet");
		setLetzteStoppzeit("noch nie gestoppt");
	}

	public void addManagedApplikationListener(ManagedApplikationListener listener) {
		listeners.add(listener);
	}

	private int convertToWarteZeitInMsec(String warteZeitStr) {
		// TODO Auto-generated method stub
		return Integer.parseInt(warteZeitStr) * 1000;
	}

	public void dispose() {
		if (warteTask != null) {
			warteTask.cancel();
		}
		if (intervallTask != null) {
			intervallTask.cancel();
		}
		if (timer != null) {
			timer.cancel();
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
		for (String argument : inkarnation.getAufrufParameter()) {
			if (builder.length() > 0) {
				builder.append(' ');
			}
			builder.append(argument);
		}

		if (inkarnation.getMitInkarnationsName()) {
			builder.append(" -inkarnationsName=");
			builder.append(processManager.getInkarnationsPrefix());
			builder.append(getInkarnationsName());
		}

		return builder.toString();
	}

	public String getReason() {
		return reason;
	}

	public StartBedingung getStartBedingung() {
		return inkarnation.getStartBedingung();
	}

	public StoppBedingung getStoppBedingung() {
		return inkarnation.getStoppBedingung();
	}

	private void handleInstalliertState(ProcessManager processManager) {
		switch (inkarnation.getStartArt().getOption()) {
		case AUTOMATISCH:
		case INTERVALLABSOLUT:
		case INTERVALLRELATIV:
			// TODO Intervallstarts implementieren
			break;
		case MANUELL:
			return;
		}

		Set<Applikation> applikationen = processManager.waitForKernsystemStart(this);
		if (!applikationen.isEmpty()) {
			updateStatus(Applikation.Status.STARTENWARTEN);
			setReason("Warte auf Kernsystem: " + listApplikationen(applikationen));
			return;
		}

		if (!isKernsystem()) {
			String davConnectionMsg = processManager.getDavConnectionMsg();
			if (davConnectionMsg != null) {
				LOGGER.info(getInkarnationsName() + ": " + davConnectionMsg);
				updateStatus(Applikation.Status.STARTENWARTEN);
				setReason(davConnectionMsg);
				return;
			}
		}

		StartBedingung startBedingung = getStartBedingung();
		if (startBedingung != null) {
			applikationen = processManager.waitForStartBedingung(this);
			if (!applikationen.isEmpty()) {
				updateStatus(Applikation.Status.STARTENWARTEN);
				setReason("Warte auf : " + listApplikationen(applikationen));
				return;
			}
			int warteZeitInMsec = convertToWarteZeitInMsec(startBedingung.getWartezeit());
			if (warteZeitInMsec > 0) {
				updateStatus(Applikation.Status.STARTENWARTEN);
				setWarteTimer(warteZeitInMsec);
				return;
			}
		}

		starteApplikation();
	}

	private void handleStartenWartenState(ProcessManager processManager, TimerType timerType) {

		Set<Applikation> applikationen = processManager.waitForKernsystemStart(this);
		if (!applikationen.isEmpty()) {
			setWarteTimer(0);
			updateStatus(Applikation.Status.STARTENWARTEN);
			setReason("Warte auf Kernsystem: " + listApplikationen(applikationen));
			return;
		}

		if (!isKernsystem()) {
			String davConnectionMsg = processManager.getDavConnectionMsg();
			if (davConnectionMsg != null) {
				setWarteTimer(0);
				LOGGER.info(getInkarnationsName() + ": " + davConnectionMsg);
				updateStatus(Applikation.Status.STARTENWARTEN);
				setReason(davConnectionMsg);
				return;
			}
		}

		StartBedingung startBedingung = getStartBedingung();
		if (startBedingung != null) {
			applikationen = processManager.waitForStartBedingung(this);
			if (!applikationen.isEmpty()) {
				setWarteTimer(0);
				updateStatus(Applikation.Status.STARTENWARTEN);
				setReason("Warte auf : " + listApplikationen(applikationen));
				return;
			}
			if (timerType != TimerType.WARTETIMER) {
				if (warteTaskIsActive()) {
					updateStatus(Applikation.Status.STARTENWARTEN);
					return;
				}
				int warteZeitInMsec = convertToWarteZeitInMsec(startBedingung.getWartezeit());
				if (warteZeitInMsec > 0) {
					updateStatus(Applikation.Status.STARTENWARTEN);
					setWarteTimer(warteZeitInMsec);
					setReason("Wartezeit bis " + DateFormat.getDateTimeInstance()
							.format(new Date(System.currentTimeMillis() + warteZeitInMsec)));
					return;
				}
			}
		}

		if (warteTaskIsActive()) {
			return;
		}

		starteApplikation();
		setReason("");
	}

	private boolean warteTaskIsActive() {
		return (warteTask != null) && warteTask.isActive();
	}

	private void handleStoppenWartenState(ProcessManager processManager, TimerType timerType) {

		Set<Applikation> applikationen = processManager.waitForKernsystemStopp(this);
		if (!applikationen.isEmpty()) {
			setWarteTimer(0);
			updateStatus(Applikation.Status.STOPPENWARTEN);
			setReason("Kernsystem wartet auf: " + listApplikationen(applikationen));
			return;
		}

		StoppBedingung stoppBedingung = getStoppBedingung();
		if (stoppBedingung != null) {
			applikationen = processManager.waitForStoppBedingung(this);
			if (!applikationen.isEmpty()) {
				setWarteTimer(0);
				updateStatus(Applikation.Status.STOPPENWARTEN);
				setReason("Warte auf : " + listApplikationen(applikationen));
				return;
			}
			if (timerType != TimerType.WARTETIMER) {
				if (warteTaskIsActive()) {
					updateStatus(Applikation.Status.STOPPENWARTEN);
					return;
				}
				int warteZeitInMsec = convertToWarteZeitInMsec(stoppBedingung.getWartezeit());
				if (warteZeitInMsec > 0) {
					updateStatus(Applikation.Status.STOPPENWARTEN);
					setWarteTimer(warteZeitInMsec);
					setReason("Wartezeit bis " + DateFormat.getDateTimeInstance()
							.format(new Date(System.currentTimeMillis() + warteZeitInMsec)));
					return;
				}
			}
		}

		if (warteTaskIsActive()) {
			return;
		}

		stoppeApplikation(false);
	}

	public boolean isKernsystem() {
		return inkarnation.isKernSystem();
	}

	private String listApplikationen(Set<Applikation> applikationen) {
		StringBuilder builder = new StringBuilder(200);
		for (Applikation applikation : applikationen) {
			if (builder.length() > 0) {
				builder.append(',');
			}
			builder.append(applikation.getInkarnationsName());
		}
		return builder.toString();
	}

	public void removeManagedApplikationListener(ManagedApplikationListener listener) {
		listeners.remove(listener);
	}

	private void setReason(String reason) {
		this.reason = reason;
	}

	private void setWarteTimer(int warteZeitInMsec) {

		if (timer == null) {
			timer = new Timer();
		}

		if (warteTask != null) {
			warteTask.cancel();
		}

		if (warteZeitInMsec <= 0) {
			return;
		}

		warteTask = new WarteTask();

		timer.schedule(warteTask, warteZeitInMsec);
	}

	private void starteApplikation() {
		updateStatus(Applikation.Status.GESTARTET);
		process = new InkarnationsProzess();
		process.setInkarnationsName(inkarnation.getInkarnationsName());
		process.setProgramm(inkarnation.getApplikation());
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

	private void stoppeApplikation(boolean force) {
		if (process == null) {
			updateStatus(Applikation.Status.GESTOPPT);
			switch (inkarnation.getStartArt().getOption()) {
			case INTERVALLABSOLUT:
			case INTERVALLRELATIV:
				if (force) {
					updateStatus(Applikation.Status.GESTOPPT);
				} else {
					updateStatus(Applikation.Status.STARTENWARTEN);
				}
				break;
			case AUTOMATISCH:
			case MANUELL:
				updateStatus(Applikation.Status.GESTOPPT);
				break;
			}
		} else {
			updateStatus(Applikation.Status.STOPPENWARTEN);
			process.kill();
			// TODO process = null;
		}
	}

	public void stoppSystemProcess() throws StartStoppException {

		switch (getStatus()) {
		case INSTALLIERT:
		case GESTOPPT:
		case STOPPENWARTEN:
			throw new StartStoppException("Applikation kann im Status \"" + getStatus() + "\" nicht gestartet werden");

		case GESTARTET:
		case INITIALISIERT:
		case STARTENWARTEN:
			break;
		default:
			break;
		}

		stoppeApplikation(true);
	}

	public void updateStatus() {
		updateStatus(TimerType.NOTIMER);
	}

	public void updateStatus(Status status) {
		Status oldStatus = getStatus();
		if (oldStatus != status) {
			setStatus(status);
			fireStatusChanged(oldStatus, status);
		}
	}

	private void updateStatus(TimerType timerType) {

		switch (getStatus()) {
		case GESTARTET:
		case GESTOPPT:
		case INITIALISIERT:
			LOGGER.finest(getStatus() + ": " + getInkarnationsName() + " keine Aktualisierung möglich");
			break;
		case INSTALLIERT:
			handleInstalliertState(processManager);
			break;
		case STARTENWARTEN:
			handleStartenWartenState(processManager, timerType);
			break;
		case STOPPENWARTEN:
			handleStoppenWartenState(processManager, timerType);
			break;
		default:
			break;
		}
	}

	public boolean isTransMitter() {
		return inkarnation.isTransMitter();
	}
}
