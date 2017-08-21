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

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ClientDavParameters;
import de.bsvrz.dav.daf.main.CommunicationError;
import de.bsvrz.dav.daf.main.ConnectionException;
import de.bsvrz.dav.daf.main.InconsistentLoginException;
import de.bsvrz.dav.daf.main.MissingParameterException;
import de.bsvrz.dav.daf.main.config.ConfigurationTaskException;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.management.UserAdministration;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.ZugangDav;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.startstopp.StartStoppDavException;

public class DavConnector {

	private static final Debug LOGGER = Debug.getLogger();

	private ZugangDav zugangDav;
	private Object lock = new Object();
	private boolean running = true;
	private ClientDavConnection connection = null;
	private ProzessManager processManager;
	private ApplikationStatusHandler appStatusHandler;

	DavConnector(ProzessManager prozessManager) {
		this.processManager = prozessManager;
		appStatusHandler = new ApplikationStatusHandler(prozessManager);
		Executors.newSingleThreadScheduledExecutor(new NamingThreadFactory("DavConnector"))
				.scheduleAtFixedRate(() -> connectToDav(), 0, 10, TimeUnit.SECONDS);
	}

	public void connectToDav() {

		if (connection == null) {
			connect();
		}

		if (connection != null) {
			try {
				if (!connection.isConnected()) {
					connection.connect();
				}

				if (!connection.isLoggedIn()) {
					LOGGER.info("Anmelden als \"" + zugangDav.getUserName() + "\" Passwort: \""
							+ zugangDav.getPassWord() + "\"");
					connection.login(zugangDav.getUserName(), zugangDav.getPassWord());
					appStatusHandler.reconnect(connection);
					processManager.davConnected();
				}

			} catch (CommunicationError | ConnectionException | RuntimeException e) {
				LOGGER.warning(e.getLocalizedMessage());
			} catch (InconsistentLoginException e1) {
				LOGGER.warning(e1.getLocalizedMessage());
			}
		}
	}

	public String getConnectionMsg() {
		if (connection != null) {
			if (connection.isConnected() && connection.isLoggedIn()) {
				return null;
			}
			return "Verbindung zum Datenverteiler konnte noch nicht hergestellt werden!";
		}
		return "Anmeldedaten für den Datenverteiler sind nicht gültig!";
	}

	private boolean isOnline() {
		if (connection != null) {
			if (connection.isConnected() && connection.isLoggedIn()) {
				return true;
			}
		}

		return false;
	}

	public boolean checkAuthentification(String veranlasser, String passwort) throws StartStoppException {

		if (!isOnline()) {
			throw new StartStoppDavException();
		}

		DataModel dataModel = connection.getDataModel();
		UserAdministration userAdministration = dataModel.getUserAdministration();
		try {
			LOGGER.warning("Prüfe Passwort: " + veranlasser + ": " + passwort);
			boolean result = userAdministration.isUserAdmin(veranlasser, passwort, veranlasser);
			LOGGER.warning("Geprüft: " + result);
			return result;
		} catch (ConfigurationTaskException e) {
			throw new StartStoppException(e);
		}
	}

	public void reconnect(ZugangDav newZugangDav) {
		if (!newZugangDav.equals(zugangDav)) {
			this.zugangDav = newZugangDav;
			if (connection != null && connection.isConnected()) {
				connection.disconnect(false, "");
			}

			connect();
		} else {
			LOGGER.info("Kein Reconnect erforderlich, Zugangsdaten wurden nicht verändert!");
		}
		trigger();
	}

	private void connect() {

		if (zugangDav == null) {
			return;
		}

		try {
			ClientDavParameters parameters = new ClientDavParameters();
			parameters.setApplicationName(processManager.getOptions().getInkarnationsName());
			parameters.setDavCommunicationAddress(zugangDav.getAdresse());
			parameters.setDavCommunicationSubAddress(Integer.parseInt(zugangDav.getPort()));
			connection = new ClientDavConnection(parameters);
			connection.addConnectionListener((conn) -> conn.disconnect(false, ""));
			connection.setCloseHandler((error) -> LOGGER.info("Datenverteilerverbindung beendet: " + error));
		} catch (MissingParameterException e) {
			LOGGER.warning("Datenverteilerverbindung kann nicht hergestellt werden!", e);
		}
	}

	void trigger() {
		synchronized (lock) {
			lock.notify();
		}
	}

	public void stoppApplikation(String name) throws StartStoppException {
		if (isOnline()) {
			appStatusHandler.terminiereAppPerDav(name);
		} else {
			throw new StartStoppException("Es besteht keine Datenverteilerverbindung");
		}
	}
}
