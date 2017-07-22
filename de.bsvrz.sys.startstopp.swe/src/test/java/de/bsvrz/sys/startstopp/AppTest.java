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

package de.bsvrz.sys.startstopp;

import java.util.List;

import de.bsvrz.sys.startstopp.api.client.StartStoppClient;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkriptStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.config.StartStoppStatusException;

public class AppTest {

	public static void main(String[] args) {

		try {
			StartStoppClient client = new StartStoppClient("localhost", 3000);

			System.err.println("STARTSTOPPSTATUS");
			StartStoppStatus startStoppStatus = client.getStartStoppStatus();
			System.err.println(startStoppStatus);
			System.err.println();

			System.err.println("STARTSTOPP_STOPP");
			client.stoppStartStopp();
			System.err.println();

			System.err.println("STARTSTOPP_RESTART");
			client.restartStartStopp();
			System.err.println();

			StartStoppSkript currentSkript = null;

			System.err.println("SKRIPT_CURRENT");
			try {
				currentSkript = client.getCurrentSkript();
				System.err.println(currentSkript);
				System.err.println();
			} catch (StartStoppStatusException e) {
				System.err.println(e.getLocalizedMessage());
			}

			if (currentSkript != null) {
				System.err.println("SET_SKRIPT_CURRENT");
				try {
					currentSkript = client.setCurrentSkript("Alles neu", currentSkript);
					System.err.println(currentSkript);
				} catch (StartStoppStatusException e) {
					System.err.println(e.getLocalizedMessage());
					for (String message : e.getMessages()) {
						System.err.println("\t" + message);
					}
				}
				System.err.println();
			}

			System.err.println("SKRIPT_CURRENT_STATUS");
			StartStoppSkriptStatus currentSkriptStatus = client.getCurrentSkriptStatus();
			System.err.println(currentSkriptStatus);
			System.err.println();

			System.err.println("APPLIKATIONEN");
			List<Applikation> applikationen = client.getApplikationen();
			System.err.println(applikationen);
			System.err.println();

			System.err.println("APPLIKATION_DATENVERTEILER");
			Applikation applikation = client.getApplikation("Datenverteiler");
			System.err.println(applikation);
			System.err.println();

			System.err.println("APPLIKATION_DATENVERTEILER_START");
			try {
				applikation = client.starteApplikation("Datenverteiler");
				System.err.println(applikation);
			} catch (StartStoppStatusException e) {
				System.err.println("\t" + e.getLocalizedMessage());
				for (String message : e.getMessages()) {
					System.err.println("\t" + message);
				}
			}
			System.err.println();

			System.err.println("APPLIKATION_DATENVERTEILER_RESTART");
			try {
				applikation = client.restarteApplikation("Datenverteiler");
				System.err.println(applikation);
			} catch (StartStoppStatusException e) {
				System.err.println("\t" + e.getLocalizedMessage());
				for (String message : e.getMessages()) {
					System.err.println("\t" + message);
				}
			}
			System.err.println();

			System.err.println("APPLIKATION_DATENVERTEILER_STOPP");
			try {
				applikation = client.stoppeApplikation("Datenverteiler");
				System.err.println(applikation);
			} catch (StartStoppStatusException e) {
				System.err.println("\t" + e.getLocalizedMessage());
				for (String message : e.getMessages()) {
					System.err.println("\t" + message);
				}
			}

		} catch (StartStoppException e) {
			System.err.println(e.getLocalizedMessage());
		}
	}
}
