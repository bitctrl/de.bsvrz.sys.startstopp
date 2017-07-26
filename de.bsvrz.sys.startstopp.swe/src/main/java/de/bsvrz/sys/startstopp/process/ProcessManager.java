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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.client.StartStoppClient;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation.Status;
import de.bsvrz.sys.startstopp.api.jsonschema.KernSystem;
import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkriptStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.config.SkriptManagerListener;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.config.StartStoppKonfiguration;
import de.bsvrz.sys.startstopp.startstopp.StartStopp;

public class ProcessManager extends Thread implements SkriptManagerListener, ManagedApplikationListener {

	private static final Debug LOGGER = Debug.getLogger();
	private boolean stopped;
	private Object lock = new Object();
	private ManagerStatus managerStatus = new ManagerStatus();

	private Map<String, StartStoppApplikation> applikationen = new LinkedHashMap<>();
	private final StartStopp startStopp;
	private SkriptStopper stopper;
	private StartStoppKonfiguration currentSkript;
	private ArrayList<String> kernSystem;
	private DavConnector davConnector = new DavConnector(this);
	private String inkarnationsPrefix;

	public ProcessManager() {
		this(StartStopp.getInstance());
	}

	public ProcessManager(StartStopp startStopp) {
		super("ProcessManager");
		this.startStopp = startStopp;
		davConnector.start();
	}

	@Override
	public void run() {

		startStopp.getSkriptManager().addSkriptManagerListener(this);

		while (!stopped) {

			if (currentSkript == null) {
				try {
					StartStoppKonfiguration skript = startStopp.getSkriptManager().getCurrentSkript();
					if (skript.getSkriptStatus().getStatus() == StartStoppSkriptStatus.Status.INITIALIZED) {
						currentSkript = skript;
						davConnector.reconnect(currentSkript.getResolvedZugangDav());
						kernSystem = new ArrayList<>();
						for (KernSystem ks : currentSkript.getSkript().getGlobal().getKernsysteme()) {
							kernSystem.add(ks.getInkarnationsName());
						}
						for (StartStoppInkarnation inkarnation : currentSkript.getInkarnationen()) {
							StartStoppApplikation applikation = new StartStoppApplikation(this, inkarnation);
							applikationen.put(applikation.getInkarnationsName(), applikation);
							applikation.addManagedApplikationListener(this);
						}
					}
				} catch (StartStoppException e) {
					currentSkript = null;
				}
			}
			for (StartStoppApplikation applikation : applikationen.values()) {
				applikation.updateStatus();
			}

			try {
				synchronized (lock) {
					lock.wait(30000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		this.startStopp.getSkriptManager().removeSkriptManagerListener(this);
	}

	public DavConnector getDavConnector() {
		return davConnector;
	}

	public List<Applikation> getApplikationen() {

		List<Applikation> result = new ArrayList<>();
		for (StartStoppApplikation applikation : applikationen.values()) {
			result.add(applikation);
		}

		return result;
	}

	public Collection<StartStoppApplikation> getManagedApplikationen() {
		return Collections.unmodifiableCollection(applikationen.values());
	}

	public Applikation getApplikation(String inkarnationsName) throws StartStoppException {
		StartStoppApplikation managedApplikation = applikationen.get(inkarnationsName);
		if (managedApplikation != null) {
			return managedApplikation;
		}

		throw new StartStoppException(
				"Eine Applikation mit dem Inkarnationsname \"" + inkarnationsName + "\" konnte nicht gefunden werden");
	}

	public Applikation starteApplikationOhnePruefung(String inkarnationsName) throws StartStoppException {
		StartStoppApplikation applikation = applikationen.get(inkarnationsName);
		if (applikation == null) {
			throw new StartStoppException("Eine Applikation mit dem Inkarnationsname \"" + inkarnationsName
					+ "\" konnte nicht gefunden werden");
		}

		applikation.startSystemProcess();
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
	public Applikation restarteApplikation(String inkarnationsName) throws StartStoppException {
		StartStoppApplikation applikation = applikationen.get(inkarnationsName);
		if (applikation == null) {
			throw new StartStoppException("Eine Applikation mit dem Inkarnationsname \"" + inkarnationsName
					+ "\" konnte nicht gefunden werden");
		}

		applikation.stoppSystemProcess();
		applikation.startSystemProcess();

		return applikation;
	}

	public Applikation stoppeApplikationOhnePruefung(String inkarnationsName) throws StartStoppException {
		StartStoppApplikation applikation = applikationen.get(inkarnationsName);
		if (applikation != null) {
			applikation.stoppSystemProcess();
			return applikation;
		}

		throw new StartStoppException(
				"Eine Applikation mit dem Inkarnationsname \"" + inkarnationsName + "\" konnte nicht gefunden werden");
	}

	public void stopp() {
		stopped = true;
		synchronized (lock) {
			lock.notify();
		}
	}

	public boolean isSkriptRunning() {
		return managerStatus.getState() == ManagerStatus.State.RUNNING;
	}

	public boolean isSkriptStopped() {
		return managerStatus.getState() == ManagerStatus.State.STOPPED;
	}

	public Thread stoppeSkript(boolean restart) {

		if (managerStatus.getState() == ManagerStatus.State.STOPPING) {
			return null;
		}
		managerStatus.setState(ManagerStatus.State.STOPPING);

		stopper = new SkriptStopper(this);
		stopper.start();
		return stopper;
	}

	@Override
	public void skriptAktualisiert(StartStoppKonfiguration oldValue, StartStoppKonfiguration newValue) {
		if (currentSkript == null) {
			synchronized (lock) {
				lock.notify();
			}
		}

		if (currentSkript != null) {
			try {
				davConnector.reconnect(currentSkript.getResolvedZugangDav());
			} catch (StartStoppException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// TODO Änderungen berechnen und Applikationen aktualisieren
	}

	public Set<Applikation> waitForStartBedingung(StartStoppApplikation managedApplikation) {

		Set<Applikation> result = new LinkedHashSet<>();

		StartBedingung bedingung = managedApplikation.getStartBedingung();
		if (bedingung == null) {
			return result;
		}

		String rechnerName = bedingung.getRechner();
		if ((rechnerName != null) && !rechnerName.trim().isEmpty()) {
			return waitForRemoteStartBedingung(managedApplikation, rechnerName, bedingung);
		}

		for (String vorgaenger : bedingung.getVorgaenger()) {
			StartStoppApplikation applikation = applikationen.get(vorgaenger);
			if (applikation == null) {
				LOGGER.warning("In der Startbedingung referenzierte Inkarnation \"" + bedingung.getVorgaenger()
						+ "\" existiert nicht!");
			}

			if (!canBeStartet(applikation, bedingung)) {
				result.add(applikation);
				LOGGER.info(managedApplikation.getInkarnationsName() + " muss auf " + applikation.getInkarnationsName()
						+ " warten!");
			}
		}
		return result;
	}

	private boolean canBeStartet(Applikation applikation, StartBedingung bedingung) {
		switch (bedingung.getWarteart()) {
		case BEGINN:
			if ((applikation.getStatus() != Status.GESTARTET) && (applikation.getStatus() != Status.INITIALISIERT)) {
				return false;
			}
			break;
		case ENDE:
			if (applikation.getStatus() != Status.INITIALISIERT) {
				return false;
			}
			break;
		}

		return true;
	}

	public Set<Applikation> waitForStoppBedingung(StartStoppApplikation managedApplikation) {

		Set<Applikation> result = new LinkedHashSet<>();

		StoppBedingung bedingung = managedApplikation.getStoppBedingung();
		if (bedingung == null) {
			return result;
		}

		String rechnerName = bedingung.getRechner();
		if ((rechnerName != null) && !rechnerName.trim().isEmpty()) {
			return waitForRemoteStoppBedingung(managedApplikation, rechnerName, bedingung);
		}

		for (String nachfolger : bedingung.getNachfolger()) {
			StartStoppApplikation applikation = applikationen.get(nachfolger);
			if (applikation == null) {
				LOGGER.warning("In der Stoppbedingung referenzierte Inkarnation \"" + bedingung.getNachfolger()
						+ "\" existiert nicht!");
				continue;
			}

			if (!canBeStopped(applikation)) {
				result.add(applikation);
			}
		}

		return result;
	}

	private boolean canBeStopped(Applikation applikation) {
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

	private Set<Applikation> waitForRemoteStoppBedingung(StartStoppApplikation managedApplikation, String rechnerName,
			StoppBedingung bedingung) {
		Set<Applikation> result = new LinkedHashSet<>();
		try {
			Rechner rechner = currentSkript.getResolvedRechner(rechnerName);
			StartStoppClient client = new StartStoppClient(rechner.getTcpAdresse(),
					Integer.parseInt(rechner.getPort()));
			for (String nachfolger : bedingung.getNachfolger()) {
				Applikation applikation = client.getApplikation(nachfolger);
				if (!canBeStopped(applikation)) {
					result.add(applikation);
					LOGGER.info(managedApplikation.getInkarnationsName() + " muss auf "
							+ applikation.getInkarnationsName() + " auf Rechner \"" + rechnerName + "\" warten!");
				}
			}

		} catch (NumberFormatException | StartStoppException e) {
			LOGGER.warning(e.getLocalizedMessage());
		}

		return result;
	}

	private Set<Applikation> waitForRemoteStartBedingung(StartStoppApplikation managedApplikation, String rechnerName,
			StartBedingung bedingung) {

		Set<Applikation> result = new LinkedHashSet<>();
		try {
			Rechner rechner = currentSkript.getResolvedRechner(rechnerName);
			StartStoppClient client = new StartStoppClient(rechner.getTcpAdresse(),
					Integer.parseInt(rechner.getPort()));

			for (String vorgaenger : bedingung.getVorgaenger()) {

				Applikation applikation = client.getApplikation(vorgaenger);

				if (!canBeStartet(applikation, bedingung)) {
					result.add(applikation);
					LOGGER.info(managedApplikation.getInkarnationsName() + " muss auf "
							+ applikation.getInkarnationsName() + " auf Rechner \"" + rechnerName + "\" warten!");
				}
			}
		} catch (NumberFormatException | StartStoppException e) {
			LOGGER.warning(e.getLocalizedMessage());
			Applikation dummyApplikation = new Applikation();
			dummyApplikation.setInkarnationsName(rechnerName);
			result.add(dummyApplikation);
		}

		return result;
	}

	public Set<Applikation> waitForKernsystemStart(StartStoppApplikation managedApplikation) {
		
		Set<Applikation> result = new LinkedHashSet<>();
		
		for (String name : kernSystem) {
			if (name.equals(managedApplikation.getInkarnationsName())) {
				return result;
			}
			StartStoppApplikation app = applikationen.get(name);
			switch (app.getStatus()) {
			case GESTARTET:
			case INITIALISIERT:
				break;
			default:
				result.add(managedApplikation);
			}
		}

		return result;
	}

	public Set<Applikation> waitForKernsystemStopp(StartStoppApplikation startStoppApplikation) {
		
		Set<Applikation> result = new LinkedHashSet<>();
		boolean foundKernsoftwareApplikation = false;

		for (String name : kernSystem) {
			if (!foundKernsoftwareApplikation) {
				if (name.equals(startStoppApplikation.getInkarnationsName())) {
					foundKernsoftwareApplikation = true;
				}
			} else {
				StartStoppApplikation app = applikationen.get(name);
				switch (app.getStatus()) {
				case GESTARTET:
				case INITIALISIERT:
				case STOPPENWARTEN:
					result.add(app);
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

	@Override
	public void applicationStatusChanged(StartStoppApplikation managedApplikation, Status oldValue, Status newValue) {
		synchronized (lock) {
			lock.notify();
		}
	}

	public String getDavConnectionMsg() {
		return davConnector.getConnectionMsg();
	}

	public String getInkarnationsPrefix() {

		if (inkarnationsPrefix == null) {

			StringBuilder builder = new StringBuilder(200);
			builder.append("StartStopp_");
			String hostName;
			try {
				hostName = InetAddress.getLocalHost().getHostName();
				builder.append(hostName);
			} catch (UnknownHostException e) {
				builder.append("unknown_host");
			}
			builder.append('_');
			inkarnationsPrefix = builder.toString();
		}

		return inkarnationsPrefix;
	}

	public void updateFromDav(String inkarnationsName, boolean fertig) {
		if (fertig) {
			StartStoppApplikation applikation = applikationen.get(inkarnationsName);
			if (applikation.getStatus() == StartStoppApplikation.Status.GESTARTET) {
				applikation.updateStatus(StartStoppApplikation.Status.INITIALISIERT);
			} else {
				LOGGER.warning(applikation.getInkarnationsName() + " ist im Status " + applikation.getStatus());
			}
		}
	}

	public void stopperFinished() {
		// TODO Auto-generated method stub
		
	}
}
