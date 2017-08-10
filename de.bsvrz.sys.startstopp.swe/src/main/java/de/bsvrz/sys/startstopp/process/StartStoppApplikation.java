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
import java.util.TimerTask;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.config.StartStoppException;

public class StartStoppApplikation extends Applikation {

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
				if (getInkarnation().getInitialize()) {
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

	private InkarnationsProzessIf process = null;
	private List<ManagedApplikationListener> listeners = new ArrayList<>();

	private TimerTask intervallTask;
	private ProzessManager processManager;
	private SystemProzessListener systemProzessListener = new SystemProzessListener();

	public StartStoppApplikation(ProzessManager processmanager, StartStoppInkarnation inkarnation) {
		this.processManager = processmanager;
		setInkarnation(inkarnation);
		updateStatus(Applikation.Status.INSTALLIERT);
		setLetzteStartzeit("noch nie gestartet");
		setLetzteStoppzeit("noch nie gestoppt");
		new StartStoppApplikationRunner(processmanager, this).start();
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
			builder.append(processManager.getInkarnationsPrefix());
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
		updateStatus(Applikation.Status.GESTARTET);
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
			updateStatus(Applikation.Status.GESTOPPT);
			switch (getInkarnation().getStartArt().getOption()) {
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
			throw new StartStoppException("Applikation kann im Status \"" + getStatus() + "\" nicht gestoppt werden");

		case GESTARTET:
		case INITIALISIERT:
		case STARTENWARTEN:
			break;
		default:
			break;
		}

		stoppeApplikation(true);
	}

	public void updateStatus(Applikation.Status status) {
		Applikation.Status oldStatus = getStatus();
		if (oldStatus != status) {
			setStatus(status);
			fireStatusChanged(oldStatus, status);
		}
	}

	public boolean isTransmitter() {
		return getInkarnation().isTransmitter();
	}
	
	@Override
	public StartStoppInkarnation getInkarnation() {
		return (StartStoppInkarnation) super.getInkarnation();
	}
}
