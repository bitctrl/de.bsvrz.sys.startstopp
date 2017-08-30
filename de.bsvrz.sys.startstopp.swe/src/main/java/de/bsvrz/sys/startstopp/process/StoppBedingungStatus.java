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

import java.util.LinkedHashSet;
import java.util.Set;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.process.remote.RechnerClient;

public final class StoppBedingungStatus {

	private static final Debug LOGGER = Debug.getLogger();

	private String message;
	private boolean erfuellt;

	public StoppBedingungStatus(OnlineApplikation applikation) {
		erfuellt = pruefeBedingung(applikation);
	}

	private boolean pruefeBedingung(OnlineApplikation applikation) {

		ProzessManager prozessManager = applikation.getProzessManager();
		Set<String> result = new LinkedHashSet<>();

		StoppBedingung bedingung = applikation.getStoppBedingung();
		if (bedingung == null) {
			return true;
		}

		String rechnerName = bedingung.getRechner();
		if (rechnerName != null && !rechnerName.trim().isEmpty()) {
			return remoteStoppBedingungErfuellt(prozessManager, applikation, rechnerName, bedingung);
		}

		for (String nachfolger : bedingung.getNachfolger()) {
			try {
				OnlineApplikation nachfolgerApp = prozessManager.getApplikation(nachfolger);
				if (!referenzApplikationGueltigFuerStopp(nachfolgerApp.getApplikation())) {
					result.add(nachfolger);
				}
			} catch (StartStoppException e) {
				LOGGER.warning("In der Stoppbedingung referenzierte Inkarnation \"" + bedingung.getNachfolger()
						+ "\" existiert nicht!", e);
			}
		}

		if (result.isEmpty()) {
			return true;
		}
		
		message = "Warte auf: " + result;
		return false;

	}

	private boolean remoteStoppBedingungErfuellt(ProzessManager prozessManager, OnlineApplikation applikation,
			String rechnerName, StoppBedingung bedingung) {

		Set<String> result = new LinkedHashSet<>();
		RechnerClient rechnerClient = prozessManager.getRechner(rechnerName);
		if (rechnerClient == null) {
			LOGGER.warning("Rechner " + rechnerName + " ist in der aktuellen Konfiguration nicht definiert!");
			return true;
		}

		for (String nachfolger : bedingung.getNachfolger()) {
			Applikation nachFolgerApplikation = rechnerClient.getApplikation(nachfolger);
			if (nachFolgerApplikation == null) {
				LOGGER.info(applikation.getName() + " kann den Status von " + nachfolger + " auf Rechner \""
						+ rechnerName + "\" nicht ermittlen!");
			} else if (!referenzApplikationGueltigFuerStopp(nachFolgerApplikation)) {
				result.add(nachfolger);
				LOGGER.info(applikation.getName() + " muss auf " + nachfolger + " auf Rechner \"" + rechnerName
						+ "\" warten!");
			}
		}

		if (result.isEmpty()) {
			return true;
		}
		
		message = "Warte auf: " + result;
		return false;
	}

	String getMessage() {
		return Util.nonEmptyString(message);
	}

	boolean isErfuellt() {
		return erfuellt;
	}
	
	boolean referenzApplikationGueltigFuerStopp(Applikation applikation) {
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
	
}
