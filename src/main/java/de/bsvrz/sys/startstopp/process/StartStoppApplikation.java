package de.bsvrz.sys.startstopp.process;

import java.util.ArrayList;
import java.util.List;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation.Status;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.config.StartStoppException;

public class StartStoppApplikation extends Thread {

	private Applikation applikation;
	private StartStoppInkarnation inkarnation;
	private SystemProcess process = null;
	private final Object lock = new Object();
	private boolean stopped;
	private ProcessManager processManager;

	private List<ManagedApplikationListener> listeners = new ArrayList<>();

	public StartStoppApplikation(StartStoppInkarnation inkarnation) {
		super(inkarnation.getInkarnationsName());

		this.inkarnation = inkarnation;
		applikation = new Applikation();
		applikation.setInkarnationsName(inkarnation.getInkarnationsName());
		applikation.setStatus(Applikation.Status.INSTALLIERT);
		applikation.setApplikation(inkarnation.getApplikation());
		applikation.getArguments().addAll(inkarnation.getAufrufParameter());
		applikation.setLetzteStartzeit("noch nie gestartet");
		applikation.setLetzteStoppzeit("noch nie gestoppt");
	}

	public Applikation getApplikation() {
		return applikation;
	}

	public String getInkarnationsName() {
		return applikation.getInkarnationsName();
	}

	public boolean isKernsystem() {
		return inkarnation.isKernSystem();
	}

	public void stoppSystemProcess(boolean useRules) throws StartStoppException {
		if (process == null) {
			throw new StartStoppException("Es ist kein Prozess verfügbar, der gestoppt werden kann!");
		}
		process.stopp();
	}

	@Override
	public void run() {
		while (!stopped) {

			System.err.println("Prüfe: " + inkarnation.getInkarnationsName());

			switch (getStatus()) {
			case ABGEBROCHEN:
				break;
			case GESTOPPT:
				break;
			case GESTARTET:
				break;
			case INITIALISIERT:
				break;
			case INSTALLIERT:
				if (processManager.isStartable(this)) {
					if (isSetInitialized()) {
						setStatus(Applikation.Status.INITIALISIERT);
					} else {
						setStatus(Applikation.Status.GESTARTET);
					}
				} else {
					setStatus(Applikation.Status.STARTENWARTEN);
				}
				break;
			case STARTENWARTEN:
				break;
			case WIRDGESTOPPT:
				break;
			}

			synchronized (lock) {
				try {
					lock.wait(30000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private boolean isSetInitialized() {
		return inkarnation.isSetInitialized();
	}

	private void setStatus(Status status) {
		Status oldStatus = getStatus();
		if (oldStatus != status) {
			applikation.setStatus(status);
			fireStatusChanged(oldStatus, status);
		}
	}

	public void startSystemProcess() {
		// TODO Auto-generated method stub

	}

	public void setProcessManager(ProcessManager processManager) {
		this.processManager = processManager;
	}

	Applikation.Status getStatus() {
		return applikation.getStatus();
	}

	public StartBedingung getStartBedingung() {
		return inkarnation.getStartBedingung();
	}

	public void trigger() {
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	public void addManagedApplikationListener(ManagedApplikationListener listener) {
		listeners.add(listener);
	}

	public void removeManagedApplikationListener(ManagedApplikationListener listener) {
		listeners.remove(listener);
	}

	private void fireStatusChanged(Applikation.Status oldStatus, Applikation.Status newStatus) {
		List<ManagedApplikationListener> receiver;
		synchronized (listeners) {
			receiver = new ArrayList<>(listeners);
		}

		for (ManagedApplikationListener listener : receiver) {
			listener.applicationStatusChanged(this, oldStatus, newStatus);
		}
	}
}
