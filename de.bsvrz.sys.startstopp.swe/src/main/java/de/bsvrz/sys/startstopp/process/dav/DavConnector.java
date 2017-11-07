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

package de.bsvrz.sys.startstopp.process.dav;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ClientDavParameters;
import de.bsvrz.dav.daf.main.CommunicationError;
import de.bsvrz.dav.daf.main.ConnectionException;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.InconsistentLoginException;
import de.bsvrz.dav.daf.main.MissingParameterException;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.ConfigurationTaskException;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.management.UserAdministration;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;
import de.bsvrz.sys.funclib.operatingMessage.MessageSender;
import de.bsvrz.sys.funclib.operatingMessage.MessageState;
import de.bsvrz.sys.funclib.operatingMessage.MessageType;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.ZugangDav;
import de.bsvrz.sys.startstopp.process.OnlineApplikation;
import de.bsvrz.sys.startstopp.process.ProzessManager;
import de.bsvrz.sys.startstopp.startstopp.StartStopp;
import de.bsvrz.sys.startstopp.startstopp.StartStoppDavException;
import de.bsvrz.sys.startstopp.startstopp.StartStoppOptions;
import de.bsvrz.sys.startstopp.util.NamingThreadFactory;
import de.muspellheim.events.Event;

public class DavConnector {

	private static final Debug LOGGER = Debug.getLogger();
	public final Event<DavApplikationStatus> onAppStatusChanged = new Event<>();

	private ZugangDav zugangDav;
	private Object lock = new Object();
	private ClientDavConnection connection;
	private ProzessManager prozessManager;
	private final ApplikationStatusHandler appStatusHandler = new ApplikationStatusHandler();
	private final StartStoppKonfigurationProvider configProvider = new StartStoppKonfigurationProvider();
	private UsvHandler usvHandler;

	private String inkarnationsPrefix;
	private MessageSender messageSender;
	private SystemObject lokalerRechner;
	private StartStoppOptions options;

	public DavConnector(ProzessManager prozessManager) {
		this(StartStopp.getInstance(), prozessManager);
	}

	public DavConnector(StartStopp startStopp, ProzessManager prozessManager) {
		this.options = startStopp.getOptions();
		this.prozessManager = prozessManager;
		
		this.inkarnationsPrefix = startStopp.getInkarnationsPrefix();
		appStatusHandler.onStatusChange.addHandler((status) -> appStatusChanged(status));
		usvHandler = new UsvHandler(prozessManager);
		Executors.newSingleThreadScheduledExecutor(new NamingThreadFactory("DavConnector"))
				.scheduleAtFixedRate(() -> connectToDav(), 0, 10, TimeUnit.SECONDS);
	}

	private void appStatusChanged(DavApplikationStatus status) {
		onAppStatusChanged.send(status);
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
					
					connection.login(zugangDav.getUserName(), zugangDav.getPassWord());

					messageSender = MessageSender.getInstance();
					messageSender.init(connection, "Start/Stopp", inkarnationsPrefix + "_MsgSender");
					lokalerRechner = ermittleLokalenRechner(connection.getDataModel());
					
					appStatusHandler.reconnect(connection);
					configProvider.reconnect(prozessManager, connection, lokalerRechner);
					configProvider.update(prozessManager.getApplikationen());
					usvHandler.reconnect(connection);
					prozessManager.davConnected();
				}

			} catch (CommunicationError | ConnectionException | InconsistentLoginException | RuntimeException e) {
				LOGGER.warning("Datenverteilerverbindung kann nicht hergestellt werden: " + e.getLocalizedMessage());
			} 
		}
	}

	public String getConnectionMsg() {

		String result = null;

		if (connection != null) {
			if (!(connection.isConnected() && connection.isLoggedIn())) {
				result = "Verbindung zum Datenverteiler konnte noch nicht hergestellt werden!";
			}
		} else {
			result = "Anmeldedaten für den Datenverteiler sind nicht gültig!";
		}

		return result;
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
			boolean result = userAdministration.isUserAdmin(veranlasser, passwort, veranlasser);
			return result;
		} catch (ConfigurationTaskException e) {
			throw new StartStoppException(e);
		}
	}

	public void reconnect() {
		ZugangDav newZugangDav = prozessManager.getZugangDav();

		if (!newZugangDav.equals(zugangDav)) {
			this.zugangDav = newZugangDav;
			if (connection != null && connection.isConnected()) {
				connection.disconnect(false, "");
			}

			connect();
		} else {
			usvHandler.reconnect(connection);
		}

		trigger();
	}

	private void connect() {

		if (zugangDav == null) {
			return;
		}

		try {
			ClientDavParameters parameters = new ClientDavParameters();
			parameters.setApplicationName(prozessManager.getOptions().getInkarnationsName());
			parameters.setDavCommunicationAddress(zugangDav.getAdresse());
			parameters.setDavCommunicationSubAddress(Integer.parseInt(zugangDav.getPort()));
			connection = new ClientDavConnection(parameters);
			connection.addConnectionListener((conn) -> conn.disconnect(false, ""));
			connection.setCloseHandler((error) -> handleDisconnect(error));

		} catch (MissingParameterException e) {
			LOGGER.warning("Datenverteilerverbindung kann nicht hergestellt werden!", e);
		}
	}

	private void handleDisconnect(String error) {
		LOGGER.info("Datenverteilerverbindung beendet: " + error);
		appStatusHandler.disconnect();
	}

	void trigger() {
		synchronized (lock) {
			lock.notify();
		}
	}

	public void stoppApplikation(String name) throws StartStoppException {
		if (isOnline()) {
			appStatusHandler.terminiereAppPerDav(inkarnationsPrefix + name);
		} else {
			throw new StartStoppException("Es besteht keine Datenverteilerverbindung");
		}
	}

	public void sendeBetriebsmeldung(String meldung) {
		if (isOnline() && options.isBetriebsMeldungVersenden()) {
			messageSender.sendMessage(MessageType.SYSTEM_DOMAIN, MessageGrade.INFORMATION, meldung);
		}
	}

	public void sendeStatusBetriebsMeldung(OnlineApplikation onlineApplikation) {
		if (!isOnline()) {
			return;
		}

		configProvider.update(prozessManager.getApplikationen());
		
		if( !options.isBetriebsMeldungVersenden()) {
			return;
		}

		String meldung = null;
		String meldungsZusatz = null;

		switch (onlineApplikation.getStatus()) {
		case GESTARTET:
			meldung = "Applikation " + onlineApplikation.getName() + " gestartet. [Sys-StSt-St01]";
			meldungsZusatz = "[Sys-StSt-St01]";
			break;
		case GESTOPPT:
			meldung = "Applikation " + onlineApplikation.getName() + " gestopped. [Sys-StSt-St02]";
			meldungsZusatz = "[Sys-StSt-St02]";
			break;
		default:
			break;
		}

		if (meldung != null) {
			messageSender.sendMessage("AOE", MessageType.SYSTEM_DOMAIN, meldungsZusatz, MessageGrade.INFORMATION,
					getLokalerRechner(), MessageState.NEW_MESSAGE, null, meldung);
		}
	}

	private SystemObject getLokalerRechner() {
		if (isOnline()) {
			return lokalerRechner;
		}
		return null;
	}

	private SystemObject ermittleLokalenRechner(DataModel dataModel) {
		
		if( options.getRechnerPid() != null) {
			return dataModel.getObject(options.getRechnerPid());
		}
		
		String hostName = null;
		String adresse = null;
		try {
			adresse = InetAddress.getLocalHost().getHostAddress();
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			LOGGER.warning("Hostname des lokalen Rechners kann nicht bestimmt werden: " + e.getLocalizedMessage());
			return null;
		}

		AttributeGroup rechnerAtg = dataModel.getAttributeGroup("atg.rechnerInformation");
		if( rechnerAtg == null) {
			LOGGER.warning("Die Attributgruppe \" atg.rechnerInformation \" ist in der aktuellen Konfiguration nicht verfügbar");
			return null;
		}
		
		for( SystemObject object : dataModel.getType("typ.rechner").getElements()) {
			Data data = object.getConfigurationData(rechnerAtg);
			if( data != null) {
				String tcpText = data.getTextValue("TCPIP").getText();
				if( tcpText.equals(hostName) || tcpText.equals(adresse)) {
					return object;
				}
			}
		}
		return null;
	}

	public boolean getAppStatus(OnlineApplikation applikation) {
		return appStatusHandler.getAppStatus(applikation);
	}
}
