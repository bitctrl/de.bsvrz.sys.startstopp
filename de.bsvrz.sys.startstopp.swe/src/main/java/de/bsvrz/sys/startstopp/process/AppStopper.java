package de.bsvrz.sys.startstopp.process;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation.Status;

public class AppStopper implements Runnable, StartStoppApplikationListener {

	private static final Debug LOGGER = Debug.getLogger();
	private Map<String, StartStoppApplikation> applikations = new LinkedHashMap<>();
	private Object lock = new Object();
	private boolean waitOnly;

	AppStopper(Collection<StartStoppApplikation> applikations, boolean waitOnly) {
		this.waitOnly = waitOnly;
		for (StartStoppApplikation applikation : applikations) {
			if (applikation.getStatus() != Applikation.Status.GESTOPPT) {
				this.applikations.put(applikation.getInkarnation().getInkarnationsName(), applikation);
				applikation.addManagedApplikationListener(this);
			}
		}
	}

	@Override
	public void run() {
		if (applikations.isEmpty()) {
			return;
		}
		if (!waitOnly) {
			for (StartStoppApplikation applikation : applikations.values()) {
				CompletableFuture.runAsync(
						() -> applikation.updateStatus(Applikation.Status.STOPPENWARTEN, "Skript wird angehalten"));
			}
		}
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				LOGGER.warning(e.getLocalizedMessage());
			}
		}
	}

	@Override
	public void applicationStatusChanged(StartStoppApplikation applikation, Status oldValue, Status newValue) {
		if (newValue != Applikation.Status.STOPPENWARTEN) {
			applikations.remove(applikation.getInkarnation().getInkarnationsName());
			applikation.removeManagedApplikationListener(this);
			if (applikations.isEmpty()) {
				synchronized (lock) {
					lock.notifyAll();
				}
			} 
		}
	}
}