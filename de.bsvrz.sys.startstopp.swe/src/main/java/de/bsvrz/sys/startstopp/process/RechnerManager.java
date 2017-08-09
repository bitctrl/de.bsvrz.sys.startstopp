package de.bsvrz.sys.startstopp.process;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.client.StartStoppClient;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.config.StartStoppException;

public class RechnerManager extends Thread {

	private static final Debug LOGGER = Debug.getLogger();
	private StartStoppClient client;
	private Map<String, Applikation> applikationen = new LinkedHashMap<>();

	private boolean running = true;
	private boolean listeErmittelt = false;
	private Object lock = new Object();

	public RechnerManager(Rechner rechner) {
		super("Rechner-" + rechner.getName());
		setDaemon(true);
		client = new StartStoppClient(rechner.getTcpAdresse(), Integer.parseInt(rechner.getPort()));
	}

	@Override
	public void run() {
		while (running) {
			try {
				List<Applikation> remoteList = client.getApplikationen();
				synchronized (applikationen) {
					applikationen.clear();
					for (Applikation applikation : remoteList) {
						applikationen.put(applikation.getInkarnation().getInkarnationsName(), applikation);
					}
					listeErmittelt = true;
				}
			} catch (StartStoppException e) {
				LOGGER.fine(getName() + ": Liste der Applikationen konnte nicht abgerufen werden!",
						e.getLocalizedMessage());
				synchronized (applikationen) {
					applikationen.clear();
					listeErmittelt = false;
				}
			}

			synchronized (lock) {
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					LOGGER.warning(e.getLocalizedMessage());
				}
			}
		}
	}
	
	boolean checkApplikationsStatus(String inkarnationsName, Applikation.Status status) {
		if( listeErmittelt) {
			Applikation applikation = applikationen.get(inkarnationsName);
			if( applikation != null) {
				return applikation.getStatus() == status;
			}
		}
		return false;
	}

	public Applikation getApplikation(String name) {
		if( listeErmittelt) {
			return applikationen.get(name);
		}
		return null;
	}
}
