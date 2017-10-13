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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;

import javax.xml.parsers.ParserConfigurationException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.bsvrz.dav.daf.main.authentication.ClientCredentials;
import de.bsvrz.dav.daf.main.config.ConfigurationTaskException;
import de.bsvrz.dav.daf.userManagement.UserManagementFileOffline;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.client.StartStoppStatusException;
import de.bsvrz.sys.startstopp.api.jsonschema.MetaDaten;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkriptStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus.Status;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppVersion;
import de.bsvrz.sys.startstopp.api.jsonschema.StatusResponse;
import de.bsvrz.sys.startstopp.api.jsonschema.VersionierungsRequest;
import de.bsvrz.sys.startstopp.startstopp.StartStopp;
import de.bsvrz.sys.startstopp.startstopp.StartStoppDavException;
import de.bsvrz.sys.startstopp.util.StartStoppXMLParser;
import de.muspellheim.events.Event;

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
	
	public final Event<StartStoppKonfiguration> onKonfigurationChanged = new Event<>();

	private final StartStopp startStopp;
	private final SortedMap<Long, StartStoppVersion> versions = new TreeMap<>();
	private StartStoppKonfiguration aktuelleKonfiguration;

	
	public SkriptManager() {
		this(StartStopp.getInstance());
	}

	public SkriptManager(StartStopp startStopp) {

		this.startStopp = startStopp;

		initSkriptHistory();

		try {

			ObjectMapper mapper = new ObjectMapper();
			StartStoppSkript skript = null;
			Checksum checkSumme = new CRC32();
			File src = getStartStoppSkriptFile();
			if (src.exists()) {
				try (InputStream stream = new CheckedInputStream(new FileInputStream(src), checkSumme)) {
					skript = mapper.readValue(stream, StartStoppSkript.class);
				}
			} else {
				LOGGER.warning("Die Skript-Datei \"" + src.getAbsolutePath() + "\" wurde nicht gefunden!");
			}
			if (skript == null) {
				File localXmlFile = new File(startStopp.getOptions().getSkriptDir(), "startstopp.xml");
				skript = new StartStoppXMLParser().getSkriptFromFile(localXmlFile);
				
				mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
				try (FileOutputStream fileStream = new FileOutputStream(getStartStoppSkriptFile());
						Writer writer = new OutputStreamWriter(fileStream, "UTF-8")) {
					mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
					mapper.writeValue(writer, skript);
				} catch (IOException e) {
					LOGGER.warning("Skript konnte nicht geladen werden: " + e.getLocalizedMessage());
				}
			}

			aktuelleKonfiguration = new StartStoppKonfiguration(skript);
			aktuelleKonfiguration.setCheckSumme(Long.toString(checkSumme.getValue()));
			if (aktuelleKonfiguration.getSkriptStatus().getStatus() == StartStoppSkriptStatus.Status.INITIALIZED) {
				String version = aktuelleKonfiguration.getSkript().getMetaDaten().getVersionsNummer();
				if (versions.isEmpty() || !versions.get(versions.lastKey()).getVersion().equals(version)) {
					aktuelleKonfiguration.setSkriptStatus(StartStoppSkriptStatus.Status.FAILURE,
							"Die Konfiguration wurde nicht korrekt versioniert!");
				} else {
					if (!versions.get(versions.lastKey()).getPruefsumme().equals(aktuelleKonfiguration.getCheckSumme())) {
						aktuelleKonfiguration.setSkriptStatus(StartStoppSkriptStatus.Status.FAILURE,
								"Checksumme der Konfigurationsdatei ist nicht korrekt!");
					}
				}
			}

		} catch (Exception e) {
			startStopp.setStatus(Status.CONFIGERROR);
			LOGGER.warning("Fehler beim Einlesen des XML-StartStopp-Skripts: " + e.getLocalizedMessage());
		}
	}
	
	private File addNewHistory(long utcNow, VersionierungsRequest request, Checksum checkSum)
			throws StartStoppException {

		File tempFile;
		try {
			tempFile = File.createTempFile("STARTSTOPP_HIST", null);
			tempFile.deleteOnExit();
		} catch (IOException e) {
			throw new StartStoppException(e);
		}

		StartStoppVersion version = new StartStoppVersion();
		version.setAenderungsGrund(request.getAenderungsgrund());
		version.setErstelltDurch(request.getVeranlasser());
		version.setName(request.getName());
		version.setPruefsumme(Long.toString(checkSum.getValue()));
		version.setVersion(Long.toString(utcNow));
		versions.put(Long.parseLong(version.getVersion()), version);

		ObjectMapper mapper = new ObjectMapper();
		try (OutputStream stream = new FileOutputStream(tempFile);
				Writer writer = new OutputStreamWriter(stream, "UTF-8")) {
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			mapper.writeValue(writer, versions.values());
		} catch (IOException e) {
			throw new StartStoppException(e);
		}

		return tempFile;
	}

	private void checkAuthentification(String veranlasser, String passwort) throws StartStoppException {

		if (startStopp.getOptions().getMasterHost() != null) {
			// TODO Remote-Prüfung einbauen
		}

		try {
			if (startStopp.getProcessManager().getDavConnector().checkAuthentification(veranlasser, passwort)) {
				return;
			}

			throw new StartStoppException("Der Nutzer \"" + veranlasser + "\" ist kein Administrator!");
		} catch (StartStoppDavException e) {
			LOGGER.fine(e.getLocalizedMessage());
		}

		try {
			File userManagementFile = startStopp.getOptions().getUserManagementFile();
			if (userManagementFile != null) {
				UserManagementFileOffline offlineChecker = new UserManagementFileOffline(userManagementFile);
				if (offlineChecker.validateClientCredentials(veranlasser,
						ClientCredentials.ofPassword(passwort.toCharArray()), -1)) {
					if (!offlineChecker.isUserAdmin(veranlasser)) {
						try {
							offlineChecker.close();
						} catch (IOException e) {
							LOGGER.warning("Authentifizierung fehlgeschlagen: "+ e.getLocalizedMessage());
						}
						throw new StartStoppException("Der Nutzer \"" + veranlasser + "\" ist kein Administrator!");
					}
					try {
						offlineChecker.close();
					} catch (IOException e) {
						LOGGER.warning("Authentifizierung fehlgeschlagen: "+ e.getLocalizedMessage());
					}
					return;
				}
			}
		} catch (ParserConfigurationException | ConfigurationTaskException e) {
			LOGGER.fine(e.getLocalizedMessage());
		}

		File passwdFile = startStopp.getOptions().getPasswdFile();
		if (passwdFile != null) {
			try (InputStream input = new FileInputStream(passwdFile)) {
				Properties properties = new Properties();
				properties.load(input);
				String passwdValue = properties.getProperty(veranlasser);
				if (passwort.equals(passwdValue)) {
					// XXX Keine Admin-Prüfung
					return;
				}
			} catch (IOException e) {
				LOGGER.fine(e.getLocalizedMessage());
			}
		}

		throw new StartStoppException("Der Nutzer \"" + veranlasser + "\" konnte nicht verifiziert werden!");
	}

	private void checkRequest(VersionierungsRequest request) throws StartStoppException {

		if (request == null || request.getSkript() == null) {
			throw new StartStoppException("Es wurde kein Skript übermittelt!");
		}

		String veranlasser = request.getVeranlasser();
		String passwort = request.getPasswort();

		if (veranlasser == null || veranlasser.trim().isEmpty()) {
			throw new StartStoppException("Es muss ein Veranlasser übergeben werden!");
		}

		if (passwort == null || passwort.trim().isEmpty()) {
			throw new StartStoppException("Es muss ein Passwort übergeben werden!");
		}

		checkAuthentification(veranlasser, passwort);

		if (request.getAenderungsgrund() == null || request.getAenderungsgrund().trim().isEmpty()) {
			throw new StartStoppException("Es muss ein Änderungsgrund übergeben werden!");
		}

	}

	public StartStoppKonfiguration getCurrentSkript() throws StartStoppException {
		if (aktuelleKonfiguration == null) {
			throw new StartStoppException("Die StartStopp-Applikation hat kein aktuelles Skript geladen");
		}
		return aktuelleKonfiguration;
	}

	public StartStoppSkriptStatus getCurrentSkriptStatus() throws StartStoppException {
		return getCurrentSkript().getSkriptStatus();
	}

	private File getStartStoppHistoryFile() {

		File versionDir = new File(startStopp.getOptions().getSkriptDir(), "history");
		if (!versionDir.exists()) {
			versionDir.mkdirs();
		}

		return new File(versionDir, "startstopp_history.json");
	}

	private File getStartStoppSkriptFile() {
		return new File(startStopp.getOptions().getSkriptDir(), "startstopp.json");
	}

	private void initSkriptHistory() {

		File historyFile = getStartStoppHistoryFile();
		if( !historyFile.exists()) {
			return;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			List<StartStoppVersion> versionsListe = mapper.readValue(historyFile,
					new TypeReference<List<StartStoppVersion>>() {
						// Kein weiterer Code erforderlich
					});
			for (StartStoppVersion version : versionsListe) {
				versions.put(Long.parseLong(version.getVersion()), version);
			}
		} catch (Exception e) {
			LOGGER.warning("Fehler beim Einlesen der StartStopp-Historie!", e);
		}
	}

	private File saveTempSkript(long utcNow, StartStoppKonfiguration skript, VersionierungsRequest request,
			Checksum checkSum) throws StartStoppException {

		File tempFile;
		try {
			tempFile = File.createTempFile("STARTSTOPP", null);
			tempFile.deleteOnExit();
		} catch (IOException e) {
			throw new StartStoppException(e);
		}

		LocalDateTime localDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(utcNow), ZoneId.systemDefault());

		MetaDaten metaDaten = new MetaDaten();
		metaDaten.setAenderungsGrund(request.getAenderungsgrund());
		metaDaten.setErstelltAm(localDate.toString());
		metaDaten.setErstelltDurch(request.getVeranlasser());
		metaDaten.setName(request.getName());
		metaDaten.setVersionsNummer(Long.toString(utcNow));
		skript.getSkript().setMetaDaten(metaDaten);

		ObjectMapper mapper = new ObjectMapper();
		try (CheckedOutputStream checkedStream = new CheckedOutputStream(new FileOutputStream(tempFile), checkSum);
				Writer writer = new OutputStreamWriter(checkedStream, "UTF-8")) {
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			mapper.writeValue(writer, skript.getSkript());
		} catch (IOException e) {
			tempFile.delete();
			throw new StartStoppException(e);
		}

		return tempFile;
	}

	public StartStoppSkript setNewSkript(VersionierungsRequest request) throws StartStoppException {

		checkRequest(request);

		StartStoppKonfiguration newSkript = new StartStoppKonfiguration(request.getSkript());
		if (newSkript.getSkriptStatus().getStatus() == StartStoppSkriptStatus.Status.INITIALIZED) {
			newSkript = versionieren(newSkript, request);
			aktuelleKonfiguration = newSkript;
			onKonfigurationChanged.send(aktuelleKonfiguration);
			return aktuelleKonfiguration.getSkript();
		}

		StatusResponse status = new StatusResponse();
		status.setCode(-1);
		status.getMessages().addAll(newSkript.getSkriptStatus().getMessages());
		throw new StartStoppStatusException("Skript konnte nicht übernommen und versioniert werden!", status);
	}

	public StartStoppKonfiguration versionieren(StartStoppKonfiguration skript, VersionierungsRequest request)
			throws StartStoppException {

		long utcNow = Clock.systemUTC().millis();
		Checksum checkSum = new CRC32();
		File tempSkript = saveTempSkript(utcNow, skript, request, checkSum);
		File tempHistoryFile = addNewHistory(utcNow, request, checkSum);

		try {
			File versionDir = new File(startStopp.getOptions().getSkriptDir(), "history");
			File archivFile = new File(versionDir, "startstopp.json." + utcNow);
			Files.copy(tempSkript.toPath(), getStartStoppSkriptFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(tempHistoryFile.toPath(), getStartStoppHistoryFile().toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			Files.copy(getStartStoppSkriptFile().toPath(), archivFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new StartStoppException(e);
		} finally {
			tempSkript.delete();
			tempHistoryFile.delete();
		}

		return skript;
	}
}
