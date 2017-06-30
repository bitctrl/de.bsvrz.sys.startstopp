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

package de.bsvrz.sys.startstopp.config;

import de.bsvrz.sys.startstopp.api.ManagedSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.Startstoppskript;
import de.bsvrz.sys.startstopp.api.jsonschema.Startstoppskriptstatus;
import de.bsvrz.sys.startstopp.api.jsonschema.Statusresponse;
import de.bsvrz.sys.startstopp.startstopp.StartStopp;
import de.bsvrz.sys.startstopp.util.StartStoppXMLParser;

public class SkriptManager {

	private ManagedSkript currentSkript;

	public SkriptManager() {
		this(StartStopp.getInstance());
	}

	public SkriptManager(StartStopp startStopp) {

		startStopp.getOptions().getSkriptDir();

		try {
			Startstoppskript skript = StartStoppXMLParser
					.getKonfigurationFrom("testkonfigurationen/startStopp01_1.xml");
			currentSkript = new ManagedSkript(skript);
		} catch (Exception e) {

		}
	}

	public ManagedSkript getCurrentSkript() throws StartStoppException {
		if (currentSkript == null) {
			throw new StartStoppException("Die StartStopp-Applikation hat kein aktuelles Skript geladen");
		}
		return currentSkript;
	}

	public Startstoppskriptstatus checkStatus(Startstoppskript konfiguration) {
		// TODO Auto-generated method stub
		return currentSkript.getStatus();
	}

	public Startstoppskript setNewSkript(Startstoppskript skript) throws StartStoppStatusException {

		ManagedSkript newSkript = new ManagedSkript(skript);
		if (newSkript.getStatus().getStatus() == Startstoppskriptstatus.Status.INITIALIZED) {

			// TODO Auto-generated method stub

			return skript;
		}

		Statusresponse status = new Statusresponse();
		status.setCode(-1);
		status.getMessages().addAll(newSkript.getStatus().getMessages());
		throw new StartStoppStatusException("Skript konnte nicht übernommen und versioniert werden!", status);
	}
}
