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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation.Status;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.config.StartStoppException;

public class StartStoppApplikation {

	private enum TimerType {
		NOTIMER, WARTETIMER, INTERVALLTIMER;
	}

	private static final Debug LOGGER = Debug.getLogger();

	public class SystemProzessListener implements InkarnationsProzessListener {

		@Override
		public void statusChanged(InkarnationsProzessStatus neuerStatus) {

			switch (neuerStatus) {
			case GESTOPPT:
				setStatus(Applikation.Status.GESTOPPT);
				if (process != null) {
					process.removeProzessListener(this);
					process = null;
				}
				break;
			case GESTARTET:
				if (inkarnation.getInitialize()) {
					setStatus(Applikation.Status.INITIALISIERT);
				} else {
					setStatus(Applikation.Status.GESTARTET);
				}
				break;
			case STARTFEHLER:
				setStatus(Applikation.Status.GESTOPPT);
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

	private Applikation applikation;
	private StartStoppInkarnation inkarnation;
	private SystemProcess process = null;

	public String getReason() {
		return reason;
	}

	private List<ManagedApplikationListener> listeners = new ArrayList<>();
	private String reason;
	private Timer timer;
	private TimerTask warteTask;
	private TimerTask intervallTask;
	private ProcessManager processManager;
	private SystemProzessListener systemProzessListener = new SystemProzessListener();

	public StartStoppApplikation(ProcessManager processmanager, StartStoppInkarnation inkarnation) {

		this.processManager = processmanager;
		this.inkarnation = inkarnation;
		applikation = new Applikation();
		applikation.setInkarnationsName(inkarnation.getInkarnationsName());
		applikation.setStatus(Applikation.Status.INSTALLIERT);
		applikation.setApplikation(inkarnation.getApplikation());
		applikation.getArguments().addAll(inkarnation.getAufrufParameter());
		applikation.setLetzteStartzeit("noch nie gestartet");
		applikation.setLetzteStoppzeit("noch nie gestoppt");
	}

	public Applikation getApplikation() {
		return applikation;
	}

	public String getInkarnationsName() {
		return applikation.getInkarnationsName();
	}

	public boolean isKernsystem() {
		return inkarnation.isKernSystem();
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

		Applikation applikation = processManager.waitForKernsystemStart(this);
		if (applikation != null) {
			setStatus(Applikation.Status.STARTENWARTEN);
			setReason("Warte auf Kernsystem: " + applikation.getInkarnationsName());
			return;
		}

		if (!isKernsystem()) {
			String davConnectionMsg = processManager.getDavConnectionMsg();
			if (davConnectionMsg != null) {
				LOGGER.info(getInkarnationsName() + ": " + davConnectionMsg);
				setStatus(Applikation.Status.STARTENWARTEN);
				setReason(davConnectionMsg);
				return;
			}
		}

		StartBedingung startBedingung = getStartBedingung();
		if (startBedingung != null) {
			applikation = processManager.waitForStartBedingung(this);
			if (applikation != null) {
				setStatus(Applikation.Status.STARTENWARTEN);
				setReason("Warte auf : " + applikation.getInkarnationsName());
				return;
			}
			int warteZeitInMsec = convertToWarteZeitInMsec(startBedingung.getWartezeit());
			if (warteZeitInMsec > 0) {
				setStatus(Applikation.Status.STARTENWARTEN);
				setWarteTimer(warteZeitInMsec);
				return;
			}
		}

		starteApplikation();
	}

	private void handleStartenWartenState(ProcessManager processManager, TimerType timerType) {

		Applikation applikation = processManager.waitForKernsystemStart(this);
		if (applikation != null) {
			setWarteTimer(0);
			setStatus(Applikation.Status.STARTENWARTEN);
			setReason("Warte auf Kernsystem: " + applikation.getInkarnationsName());
			return;
		}

		if (!isKernsystem()) {
			String davConnectionMsg = processManager.getDavConnectionMsg();
			if (davConnectionMsg != null) {
				LOGGER.info(getInkarnationsName() + ": " + davConnectionMsg);
				setStatus(Applikation.Status.STARTENWARTEN);
				setReason(davConnectionMsg);
				return;
			}
		}
		
		StartBedingung startBedingung = getStartBedingung();
		if (startBedingung != null) {
			applikation = processManager.waitForStartBedingung(this);
			if (applikation != null) {
				setWarteTimer(0);
				setStatus(Applikation.Status.STARTENWARTEN);
				setReason("Warte auf : " + applikation.getInkarnationsName());
				return;
			}
			if (timerType != TimerType.WARTETIMER) {
				int warteZeitInMsec = convertToWarteZeitInMsec(startBedingung.getWartezeit());
				if (warteZeitInMsec > 0) {
					setStatus(Applikation.Status.STARTENWARTEN);
					setWarteTimer(warteZeitInMsec);
					setReason("Wartezeit aktiv");
					return;
				}
			}
		}

		starteApplikation();
	}

	private void handleStoppenWartenState(ProcessManager processManager, TimerType timerType) {

		Applikation applikation = processManager.waitForKernsystemStopp(this);
		if (applikation != null) {
			setWarteTimer(0);
			setStatus(Applikation.Status.STOPPENWARTEN);
			setReason("Kernsystem wartet auf: " + applikation.getInkarnationsName());
			return;
		}

		StoppBedingung stoppBedingung = getStoppBedingung();
		if (stoppBedingung != null) {
			applikation = processManager.waitForStoppBedingung(this);
			if (applikation != null) {
				setWarteTimer(0);
				setStatus(Applikation.Status.STOPPENWARTEN);
				setReason("Warte auf : " + applikation.getInkarnationsName());
				return;
			}
			if (timerType != TimerType.WARTETIMER) {
				int warteZeitInMsec = convertToWarteZeitInMsec(stoppBedingung.getWartezeit());
				if (warteZeitInMsec > 0) {
					setStatus(Applikation.Status.STOPPENWARTEN);
					setWarteTimer(warteZeitInMsec);
					setReason("Wartezeit aktiv");
					return;
				}
			}
		}

		stoppeApplikation(false);
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

		warteTask = new TimerTask() {
			@Override
			public void run() {
				updateStatus(TimerType.WARTETIMER);
			}
		};
		timer.schedule(warteTask, warteZeitInMsec);
	}

	private void setReason(String reason) {
		this.reason = reason;
	}

	private int convertToWarteZeitInMsec(String warteZeitStr) {
		// TODO Auto-generated method stub
		return 0;
	}

	private void starteApplikation() {
		setStatus(Applikation.Status.GESTARTET);
		process = new SystemProcess();
		process.setInkarnationsName(inkarnation.getInkarnationsName());
		process.setProgramm(inkarnation.getApplikation());
		process.setProgrammArgumente(getApplikationsArgumente());
		process.addProzessListener(systemProzessListener);
		process.start();
	}

	private void stoppeApplikation(boolean force) {
		if (process == null) {
			setStatus(Applikation.Status.GESTOPPT);
			switch (inkarnation.getStartArt().getOption()) {
			case INTERVALLABSOLUT:
			case INTERVALLRELATIV:
				if (force) {
					setStatus(Applikation.Status.GESTOPPT);
				} else {
					setStatus(Applikation.Status.STARTENWARTEN);
				}
				break;
			case AUTOMATISCH:
			case MANUELL:
				setStatus(Applikation.Status.GESTOPPT);
				break;
			}
		} else {
			setStatus(Applikation.Status.STOPPENWARTEN);
			process.stopp();
			process = null;
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

		// TODO Inkarnationsname ergänzen

		return builder.toString();
	}

	private void setStatus(Status status) {
		Status oldStatus = getStatus();
		if (oldStatus != status) {
			applikation.setStatus(status);
			fireStatusChanged(oldStatus, status);
		}
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

	Applikation.Status getStatus() {
		return applikation.getStatus();
	}

	public StartBedingung getStartBedingung() {
		return inkarnation.getStartBedingung();
	}

	public StoppBedingung getStoppBedingung() {
		return inkarnation.getStoppBedingung();
	}

	public void addManagedApplikationListener(ManagedApplikationListener listener) {
		listeners.add(listener);
	}

	public void removeManagedApplikationListener(ManagedApplikationListener listener) {
		listeners.remove(listener);
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
}
