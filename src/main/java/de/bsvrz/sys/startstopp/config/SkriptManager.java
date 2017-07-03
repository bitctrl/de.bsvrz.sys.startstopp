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

import java.io.File;
import java.io.FileWriter;
import java.io.FilterWriter;
import java.io.StringWriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.bsvrz.sys.startstopp.api.ManagedSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.Startstoppskript;
import de.bsvrz.sys.startstopp.api.jsonschema.Startstoppskriptstatus;
import de.bsvrz.sys.startstopp.api.jsonschema.Statusresponse;
import de.bsvrz.sys.startstopp.startstopp.StartStopp;
import de.bsvrz.sys.startstopp.util.StartStoppXMLParser;

/**
 * 
 * Das Modul zur Verwaltung des von StartStopp auszuführenden Skripts.
 * 
 * Das Skript wird interpretiert und für die Prozessverwaltung bereitgestellt.
 * 
 * @author BitCtrl Systems GmbH, Uwe Peuker
 */
public class SkriptManager {

	private ManagedSkript currentSkript;
	private StartStopp startStopp;

	public SkriptManager() {
		this(StartStopp.getInstance());
	}

	public SkriptManager(StartStopp startStopp) {

		this.startStopp = startStopp;

		String skriptDir = startStopp.getOptions().getSkriptDir();
		try {

			ObjectMapper mapper = new ObjectMapper();
			Startstoppskript skript = null;
			try {
				skript = mapper.readValue(new File(skriptDir + "/startstopp.json"), Startstoppskript.class);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (skript == null) {
				skript = StartStoppXMLParser.getKonfigurationFrom("testkonfigurationen/startStopp01_1.xml");

				mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
				try (FileWriter writer = new FileWriter(skriptDir + "/startstopp.json")) {
					mapper.writeValue(writer, skript);
				}
			}
			currentSkript = new ManagedSkript(skript);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ManagedSkript getCurrentSkript() throws StartStoppException {
		if (currentSkript == null) {
			throw new StartStoppException("Die StartStopp-Applikation hat kein aktuelles Skript geladen");
		}
		return currentSkript;
	}

	public Startstoppskriptstatus getCurrentSkriptStatus() throws StartStoppException {
		return getCurrentSkript().getSkriptStatus();
	}

	public Startstoppskript setNewSkript(Startstoppskript skript) throws StartStoppException {

		ManagedSkript newSkript = new ManagedSkript(skript);
		newSkript.versionieren();
		if (newSkript.getSkriptStatus().getStatus() == Startstoppskriptstatus.Status.INITIALIZED) {
			startStopp.getProcessManager().updateSkript(currentSkript, newSkript);
			return skript;
		}

		Statusresponse status = new Statusresponse();
		status.setCode(-1);
		status.getMessages().addAll(newSkript.getSkriptStatus().getMessages());
		throw new StartStoppStatusException("Skript konnte nicht übernommen und versioniert werden!", status);
	}
}
