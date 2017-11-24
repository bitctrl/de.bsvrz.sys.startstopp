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
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.util.Util;
import de.bsvrz.sys.startstopp.process.remote.RechnerClient;

public final class StartBedingungStatus {

	private static final Debug LOGGER = Debug.getLogger();

	private String message;
	private boolean erfuellt;
	
	public StartBedingungStatus(OnlineApplikation applikation) {
		erfuellt = pruefeBedingung(applikation);
	}

	private boolean pruefeBedingung(OnlineApplikation applikation) {

		ProzessManager prozessManager = applikation.getProzessManager();

		Set<String> result = new LinkedHashSet<>();
		StartBedingung bedingung = applikation.getStartBedingung();
		if (bedingung == null) {
			return true;
		}

		String rechnerName = bedingung.getRechner();
		if ((rechnerName != null) && !rechnerName.trim().isEmpty()) {
			return remoteStartBedingungErfuellt(prozessManager, rechnerName, bedingung);
		}

		for (String vorgaenger : bedingung.getVorgaenger()) {
			try {
				OnlineApplikation vorgaengerApplikation = prozessManager.getApplikation(vorgaenger);
				if (!referenzApplikationGueltigFuerStart(vorgaengerApplikation.getApplikation(),
						bedingung)) {
					result.add(vorgaengerApplikation.getName());
				}
			} catch (StartStoppException e) {
				LOGGER.warning("In der Startbedingung referenzierte Inkarnation \"" + bedingung.getVorgaenger()
						+ "\" existiert nicht!", e);
			}
		}

		if( result.isEmpty()) {
			return true;
		}
		
		message = "Warte auf " + result;
		return false;
	}

	private boolean remoteStartBedingungErfuellt(ProzessManager prozessManager, String rechnerName,
			StartBedingung bedingung) {
		
		RechnerClient rechnerClient = prozessManager.getRechner(rechnerName);
		if (rechnerClient == null) {
			LOGGER.warning("Rechner " + rechnerName + " ist in der Konfiguration nicht definiert!");
			return true;
		}
		
		Set<String> result = new LinkedHashSet<>();
		for (String vorgaenger : bedingung.getVorgaenger()) {
			Applikation vorgaengerApplikation = rechnerClient.getApplikation(vorgaenger);
			if (vorgaengerApplikation == null) {
				result.add(vorgaenger);
			} else if (!referenzApplikationGueltigFuerStart(vorgaengerApplikation, bedingung)) {
				result.add(vorgaenger);
			}
		}

		if( result.isEmpty()) {
			return true;
		}
		
		message = "Warte auf " + result;
		return false;
	}

	String getMessage() {
		return Util.nonEmptyString(message);
	}

	boolean isErfuellt() {
		return erfuellt;
	}
	
	boolean referenzApplikationGueltigFuerStart(Applikation applikation, StartBedingung bedingung) {
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

	
}
