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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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

import de.bsvrz.dav.daf.communication.protocol.UserLogin;
import de.bsvrz.dav.daf.communication.srpAuthentication.SrpClientAuthentication;
import de.bsvrz.dav.daf.communication.srpAuthentication.SrpCryptoParameter;
import de.bsvrz.dav.daf.communication.srpAuthentication.SrpUtilities;
import de.bsvrz.dav.daf.communication.srpAuthentication.SrpVerifierAndUser;
import de.bsvrz.dav.daf.communication.srpAuthentication.SrpVerifierData;
import de.bsvrz.dav.daf.main.InconsistentLoginException;
import de.bsvrz.dav.daf.main.authentication.ClientCredentials;
import de.bsvrz.dav.daf.main.config.ConfigurationTaskException;
import de.bsvrz.dav.daf.userManagement.UserManagement;
import de.bsvrz.dav.daf.userManagement.UserManagementFileOffline;
import de.bsvrz.dav.daf.userManagement.UserManagementFileOnline;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.MetaDaten;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkriptStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppVersion;
import de.bsvrz.sys.startstopp.api.jsonschema.StatusResponse;
import de.bsvrz.sys.startstopp.api.jsonschema.VersionierungsRequest;
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
	private final StartStopp startStopp;
	private final List<SkriptManagerListener> listeners = new ArrayList<>();
	private final SortedMap<Long, StartStoppVersion> versions = new TreeMap<>();

	private StartStoppKonfiguration currentSkript;

	public SkriptManager() {
		this(StartStopp.getInstance());
	}

	public SkriptManager(StartStopp startStopp) {

		this.startStopp = startStopp;

		String skriptDir = startStopp.getOptions().getSkriptDir();
		initSkriptHistory(skriptDir);

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
				LOGGER.warning("Versuche XML-Datei zu konvertieren!");
				skript = StartStoppXMLParser.getKonfigurationFrom("testkonfigurationen/startStopp01_1.xml");
				mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

				try (FileOutputStream fileStream = new FileOutputStream(getStartStoppSkriptFile());
						Writer writer = new OutputStreamWriter(fileStream, "UTF-8")) {
					mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
					mapper.writeValue(writer, skript);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			currentSkript = new StartStoppKonfiguration(skript);
			currentSkript.setCheckSumme(Long.toString(checkSumme.getValue()));
			if (currentSkript.getSkriptStatus().getStatus() == StartStoppSkriptStatus.Status.INITIALIZED) {
				String version = currentSkript.getSkript().getMetaDaten().getVersionsNummer();
				if (versions.isEmpty() || !versions.get(versions.lastKey()).getVersion().equals(version)) {
					currentSkript.setSkriptStatus(StartStoppSkriptStatus.Status.FAILURE,
							"Die Konfiguration wurde nicht korrekt versioniert!");
				} else {
					if (!versions.get(versions.lastKey()).getPruefsumme().equals(currentSkript.getCheckSumme())) {
						currentSkript.setSkriptStatus(StartStoppSkriptStatus.Status.FAILURE,
								"Checksumme der Konfigurationsdatei ist nicht korrekt!");
					}
				}
			}

		} catch (Exception e) {
			LOGGER.warning("Fehler beim Einlesen des XML-StartStopp-Skripts!", e);
		}
	}

	private File getStartStoppSkriptFile() {
		return new File(startStopp.getOptions().getSkriptDir(), "startstopp.json");
	}

	private File getStartStoppHistoryFile() {

		File versionDir = new File(startStopp.getOptions().getSkriptDir(), "history");
		if (!versionDir.exists()) {
			versionDir.mkdirs();
		}

		return new File(versionDir, "startstopp_history.json");
	}

	private void initSkriptHistory(String skriptDir) {

		File historyFile = getStartStoppHistoryFile();
		ObjectMapper mapper = new ObjectMapper();
		try {
			List<StartStoppVersion> versionsListe = mapper.readValue(historyFile,
					new TypeReference<List<StartStoppVersion>>() {
					});
			for (StartStoppVersion version : versionsListe) {
				versions.put(Long.parseLong(version.getVersion()), version);
			}
		} catch (Exception e) {
			LOGGER.warning("Fehler beim Einlesen der StartStopp-Historie!", e);
		}

		// TODO Auto-generated method stub

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

	public StartStoppSkript setNewSkript(VersionierungsRequest request) throws StartStoppException {

		checkRequest(request);

		StartStoppKonfiguration newSkript = new StartStoppKonfiguration(request.getSkript());
		if (newSkript.getSkriptStatus().getStatus() == StartStoppSkriptStatus.Status.INITIALIZED) {
			StartStoppKonfiguration oldSkript = currentSkript;
			newSkript = versionieren(newSkript, request);
			currentSkript = newSkript;
			fireSkriptChanged(oldSkript, currentSkript);
			return currentSkript.getSkript();
		}

		StatusResponse status = new StatusResponse();
		status.setCode(-1);
		status.getMessages().addAll(newSkript.getSkriptStatus().getMessages());
		throw new StartStoppStatusException("Skript konnte nicht übernommen und versioniert werden!", status);
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

	private void checkAuthentification(String veranlasser, String passwort) throws StartStoppException {

		if( startStopp.getOptions().getMasterHost() != null) {
			// TODO Remote-Prüfung einbauen
		}
		
		try {
			if (startStopp.getProcessManager().getDavConnector().checkAuthentification(veranlasser, passwort)) {
				return;
			}

			throw new StartStoppException("Der Nutzer \"" + veranlasser + "\" ist kein Administrator!");
		} catch (StartStoppException e) {
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
							LOGGER.warning(e.getLocalizedMessage());
						}
						throw new StartStoppException("Der Nutzer \"" + veranlasser + "\" ist kein Administrator!");
					}
					try {
						offlineChecker.close();
					} catch (IOException e) {
						LOGGER.warning(e.getLocalizedMessage());
					}
					return;
				}
			}
		} catch (ParserConfigurationException | ConfigurationTaskException e) {
			LOGGER.fine(e.getLocalizedMessage());
		}

		File passwdFile = startStopp.getOptions().getPasswdFile();
		if( passwdFile != null) {
			try {
				Properties properties = new Properties();
				properties.load(new FileInputStream(passwdFile));
				String passwdValue = properties.getProperty(veranlasser);
				if( passwort.equals(passwdValue)) {
					// XXX Keine Admin-Prüfung
					return;
				}
			} catch (IOException e) {
				LOGGER.fine(e.getLocalizedMessage());
			}
		}
		
		throw new StartStoppException("Der Nutzer \"" + veranlasser + "\" konnte nicht verifiziert werden!");
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

	public StartStoppKonfiguration versionieren(StartStoppKonfiguration skript, VersionierungsRequest request)
			throws StartStoppException {

		long utcNow = Clock.systemUTC().millis();
		Checksum checkSum = new CRC32();
		File tempSkript = saveTempSkript(utcNow, skript, request, checkSum);
		File tempHistoryFile = addNewHistory(utcNow, request, checkSum);

		try {
			Files.copy(tempSkript.toPath(), getStartStoppSkriptFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(tempHistoryFile.toPath(), getStartStoppHistoryFile().toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new StartStoppException(e);
		} finally {
			tempSkript.delete();
			tempHistoryFile.delete();
		}

		return skript;
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

		MetaDaten metaDaten = skript.getSkript().getMetaDaten();
		metaDaten.setAenderungsGrund(request.getAenderungsgrund());
		metaDaten.setErstelltAm(localDate.toString());
		metaDaten.setErstelltDurch(request.getVeranlasser());
		metaDaten.setName(request.getName());
		metaDaten.setVersionsNummer(Long.toString(utcNow));

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
}
