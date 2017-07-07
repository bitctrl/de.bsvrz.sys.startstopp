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
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkriptStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.StatusResponse;
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

	private static final Debug LOGGER = Debug.getLogger();
	private StartStoppKonfiguration currentSkript;
	private StartStopp startStopp;
	private List<SkriptManagerListener> listeners = new ArrayList<>();

	public SkriptManager() {
		this(StartStopp.getInstance());
	}

	public SkriptManager(StartStopp startStopp) {

		this.startStopp = startStopp;

		String skriptDir = startStopp.getOptions().getSkriptDir();
		try {

			ObjectMapper mapper = new ObjectMapper();
			StartStoppSkript skript = null;
			try {
				File src = new File(skriptDir + "/startstopp.json");
				if (src.exists()) {
					skript = mapper.readValue(src, StartStoppSkript.class);
				} else {
					LOGGER.warning("Die Skript-Datei \"" + src.getAbsolutePath() + "\" wurde nicht gefunden!");
				}
			} catch (Exception e) {
				LOGGER.warning("Fehler beim Einlesen des StartStopp-Skripts!", e);
			}
			if (skript == null) {
				LOGGER.warning("Versuche XML-Datei zu konvertieren!");
				skript = StartStoppXMLParser.getKonfigurationFrom("testkonfigurationen/startStopp01_1.xml");

				mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

				try (Writer writer = new OutputStreamWriter(new FileOutputStream(skriptDir + "/startstopp.json"),
						"UTF-8")) {
					mapper.writeValue(writer, skript);
				}
			}
			currentSkript = new StartStoppKonfiguration(skript);
		} catch (Exception e) {
			LOGGER.warning("Fehler beim Einlesen des XML-StartStopp-Skripts!", e);
		}
	}

	public StartStoppKonfiguration getCurrentSkript() throws StartStoppException {
		if (currentSkript == null) {
			throw new StartStoppException("Die StartStopp-Applikation hat kein aktuelles Skript geladen");
		}
		return currentSkript;
	}

	public StartStoppSkriptStatus getCurrentSkriptStatus() throws StartStoppException {
		return getCurrentSkript().getSkriptStatus();
	}

	public StartStoppSkript setNewSkript(String reason, StartStoppSkript skript) throws StartStoppException {

		StartStoppKonfiguration newSkript = new StartStoppKonfiguration(skript);
		if (newSkript.getSkriptStatus().getStatus() == StartStoppSkriptStatus.Status.INITIALIZED) {
			newSkript = newSkript.versionieren(reason);
			sichereSkriptVersion(newSkript);
			fireSkriptChanged(currentSkript, newSkript);
			currentSkript = newSkript;
			return currentSkript.getSkript();
		}

		StatusResponse status = new StatusResponse();
		status.setCode(-1);
		status.getMessages().addAll(newSkript.getSkriptStatus().getMessages());
		throw new StartStoppStatusException("Skript konnte nicht übernommen und versioniert werden!", status);
	}

	private void sichereSkriptVersion(StartStoppKonfiguration newSkript) {
		startStopp.getOptions().getSkriptDir();
		// TODO Auto-generated method stub
	}

	private void fireSkriptChanged(StartStoppKonfiguration oldSkript, StartStoppKonfiguration newSkript) {
		List<SkriptManagerListener> receivers;
		synchronized (listeners) {
			receivers = new ArrayList<>(listeners);
		}

		for (SkriptManagerListener listener : receivers) {
			listener.skriptAktualisiert(oldSkript, newSkript);
		}

	}

	public void addSkriptManagerListener(SkriptManagerListener listener) {
		listeners.add(listener);
	}

	public void removeSkriptManagerListener(SkriptManagerListener listener) {
		listeners.remove(listener);
	}
}
