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

		while (running) {

			try {
				if (!connection.isConnected()) {
					connection.connect();
				}

				if (!connection.isLoggedIn()) {
					connection.login(zugangDav.getUserName(), zugangDav.getPassWord());
				}
			} catch (CommunicationError | ConnectionException | RuntimeException e) {
				// TODO Auto-generated catch block
				Debug.getLogger().warning(e.getLocalizedMessage());
			} catch (InconsistentLoginException e1) {
				running = false;
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
}
