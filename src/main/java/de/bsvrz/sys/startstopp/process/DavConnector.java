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

import de.bsvrz.dav.daf.main.ApplicationCloseActionHandler;
import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientDavParameters;
import de.bsvrz.dav.daf.main.CommunicationError;
import de.bsvrz.dav.daf.main.ConnectionException;
import de.bsvrz.dav.daf.main.DavConnectionListener;
import de.bsvrz.dav.daf.main.InconsistentLoginException;
import de.bsvrz.dav.daf.main.MissingParameterException;
import de.bsvrz.dav.daf.main.config.ConfigurationTaskException;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.management.UserAdministration;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.ZugangDav;
import de.bsvrz.sys.startstopp.config.StartStoppException;

public class DavConnector extends Thread {

	public class ConnectionListener implements DavConnectionListener {

		@Override
		public void connectionClosed(ClientDavInterface connection) {
			// TODO Auto-generated method stub
			System.err.println("ConnectionClosed");
		}
	}

	public class ConnectionCloseHandler implements ApplicationCloseActionHandler {

		@Override
		public void close(String error) {
			System.err.println("Connection closed: " + error);
		}
	}

	private ZugangDav zugangDav;
	private Object lock = new Object();
	private boolean running = true;
	private ClientDavConnection connection;

	public DavConnector(ZugangDav zugangDav) throws StartStoppException {
		super("DavConnector");
		setDaemon(true);
		this.zugangDav = zugangDav;

		try {
			ClientDavParameters parameters = new ClientDavParameters();
			// TODO Inkarnationsname korrekt bilden
			parameters.setApplicationName("StartStopp");
			parameters.setDavCommunicationAddress(zugangDav.getAdresse());
			parameters.setDavCommunicationSubAddress(Integer.parseInt(zugangDav.getPort()));
			connection = new ClientDavConnection(parameters);
			connection.addConnectionListener(new ConnectionListener());
			connection.setCloseHandler(new ConnectionCloseHandler());
		} catch (NumberFormatException | MissingParameterException e) {
			throw new StartStoppException(e);
		}
	}

	@Override
	public void run() {

		try {
			Thread.sleep(40000);
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		
		while (running) {

			try {
				if (!connection.isConnected()) {
					connection.connect();
				}

				if (!connection.isLoggedIn()) {
					Debug.getLogger().info("Anmelden als \"" + zugangDav.getUserName() + "\" Passwort: \"" + zugangDav.getPassWord() + "\"");
					connection.login(zugangDav.getUserName(), zugangDav.getPassWord());
				}
			} catch (CommunicationError | ConnectionException | RuntimeException e) {
				// TODO Auto-generated catch block
				Debug.getLogger().warning(e.getLocalizedMessage());
			} catch (InconsistentLoginException e1) {
				// running = false;
				Debug.getLogger().warning(e1.getLocalizedMessage());
			}

			synchronized (lock) {
				try {
					lock.wait(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public String getConnectionMsg() {
		if( running ) {
			if( connection.isConnected() && connection.isLoggedIn()) {
				return null;
			}
			return "Verbindung zum Datenverteiler konnte noch nicht hergestellt werden!";
		}
		return "Anmeldedaten für den Datenverteiler sind nicht gültig!";
	}

	private boolean isOnline() {
		if ((connection != null) && connection.isConnected() &&  connection.isLoggedIn()) {
			return true;
		}
		
		return false;
	}

	public boolean checkAuthentification(String veranlasser, String passwort) throws StartStoppException {
		
		if(!isOnline()) {
			throw new StartStoppException("Es besteht keine Datenverteilerverbindung!");
		}
		
		DataModel dataModel = connection.getDataModel();
		UserAdministration userAdministration = dataModel.getUserAdministration();
		try {
			Debug.getLogger().warning("Prüfe Passwort: " + veranlasser + ": " + passwort);
			boolean result = userAdministration.isUserAdmin(veranlasser, passwort, veranlasser);
			Debug.getLogger().warning("Geprüft: " + result);
			return result;
		} catch (ConfigurationTaskException e) {
			throw new StartStoppException(e);
		}
	}
}
