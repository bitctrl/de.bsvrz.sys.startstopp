package de.bsvrz.sys.startstopp.process;

import java.util.ArrayList;
import java.util.List;

import de.bsvrz.sys.funclib.concurrent.PriorizedObject;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation.Status;
import de.bsvrz.sys.startstopp.api.jsonschema.StartArt;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.process.StartStoppApplikation.SystemProzessListener;

public class StartStoppApplikation extends Thread {

	public class SystemProzessListener implements InkarnationsProzessListener {

		@Override
		public void statusChanged(InkarnationsProzessStatus neuerStatus) {

			switch (neuerStatus) {
			case GESTOPPT:
				setStatus(Applikation.Status.GESTOPPT);
				if (process != null) {
					process.removeProzessListener(this);
					process = null;
				}
				break;
			case GESTARTET:
				if (inkarnation.getInitialize()) {
					setStatus(Applikation.Status.INITIALISIERT);
				} else {
					setStatus(Applikation.Status.GESTARTET);
				}
				break;
			case STARTFEHLER:
				setStatus(Applikation.Status.ABGEBROCHEN);
				if (process != null) {
					process.removeProzessListener(this);
					process = null;
				}
				break;
			default:
				break;
			}
		}
	}

	private Applikation applikation;
	private StartStoppInkarnation inkarnation;
	private SystemProcess process = null;
	private final Object lock = new Object();
	private boolean stopped;
	private ProcessManager processManager;

	private List<ManagedApplikationListener> listeners = new ArrayList<>();
	private long plannedStart;
	private long plannedStopp;

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

	public void stoppSystemProcess() throws StartStoppException {

		switch (getStatus()) {
		case INSTALLIERT:
		case ABGEBROCHEN:
		case GESTOPPT:
		case STARTEN:
			throw new StartStoppException("Applikation kann im Status \"" + getStatus() + "\" nicht gestartet werden");

		case KERNSYSTEM:
		case STARTBEDINGUNG:
		case STARTVERZOEGERUNG:
		case GESTARTET:
		case INITIALISIERT:
		case STOPPBEDINGUNG:
		case STOPPEN:
		case STOPPVERZOEGERUNG:
		}

		stoppeApplikation();
	}

	@Override
	public void run() {
		while (!stopped) {

			switch (getStatus()) {
			case ABGEBROCHEN:
				break;
			case GESTARTET:
				break;
			case GESTOPPT:
				break;
			case INITIALISIERT:
				break;
			case INSTALLIERT:
				handleInstalliertState();
				break;
			case KERNSYSTEM:
				handleKernSystemState();
				break;
			case STARTBEDINGUNG:
				handleStartBedingungState();
				break;
			case STARTEN:
				break;
			case STARTVERZOEGERUNG:
				handleStartVerzoegerungState();
				break;
			case STOPPBEDINGUNG:
				break;
			case STOPPEN:
				break;
			case STOPPVERZOEGERUNG:
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

	private void handleInstalliertState() {
		switch (inkarnation.getStartArt().getOption()) {
		case AUTOMATISCH:
		case INTERVALLABSOLUT:
		case INTERVALLRELATIV:
			// TODO Intervallstarts implementieren
			break;
		case MANUELL:
			return;
		}

		if (processManager.waitForKernsystemStart(this)) {
			setStatus(Applikation.Status.KERNSYSTEM);
		} else if (processManager.waitForStartBedingung(this)) {
			setStatus(Applikation.Status.STARTBEDINGUNG);
		} else {
			starteApplikation();
		}
	}

	private void handleKernSystemState() {
		if (processManager.waitForKernsystemStart(this)) {
			return;
		} else if (processManager.waitForStartBedingung(this)) {
			setStatus(Applikation.Status.STARTBEDINGUNG);
		} else {
			starteApplikation();
		}
	}

	private void handleStartBedingungState() {
		if (!processManager.waitForStartBedingung(this)) {
			String warteZeitStr = inkarnation.getStartBedingung().getWartezeit();
			int warteZeitInMsec = convertToWarteZeitInMsec(warteZeitStr);
			// TODO Wartezeit per Timer realisieren
			if (warteZeitInMsec > 0) {
				plannedStart = System.currentTimeMillis() + warteZeitInMsec;
				setStatus(Applikation.Status.STARTVERZOEGERUNG);
			} else {
				starteApplikation();
			}
		}
	}

	private void handleStartVerzoegerungState() {
		if (processManager.waitForStartBedingung(this)) {
			setStatus(Applikation.Status.STARTBEDINGUNG);
		} else if (System.currentTimeMillis() >= plannedStart) {
			starteApplikation();
		}
	}

	private void handleStoppBedingungState() {
		if (!processManager.waitForStoppBedingung(this)) {
			String warteZeitStr = inkarnation.getStoppBedingung().getWartezeit();
			int warteZeitInMsec = convertToWarteZeitInMsec(warteZeitStr);
			// TODO Wartezeit per Timer realisieren
			if (warteZeitInMsec > 0) {
				plannedStopp = System.currentTimeMillis() + warteZeitInMsec;
				setStatus(Applikation.Status.STOPPVERZOEGERUNG);
			} else {
				starteApplikation();
			}
		}
	}

	
	private int convertToWarteZeitInMsec(String warteZeitStr) {
		// TODO Auto-generated method stub
		return 0;
	}

	private void starteApplikation() {
		setStatus(Applikation.Status.STARTEN);
		process = new SystemProcess();
		process.setInkarnationsName(inkarnation.getInkarnationsName());
		process.setProgramm(inkarnation.getApplikation());
		process.setProgrammArgumente(getApplikationsArgumente());
		process.addProzessListener(new SystemProzessListener());
		process.start();
	}

	private void stoppeApplikation() {
		if (process == null) {
			setStatus(Applikation.Status.GESTOPPT);
		} else {
			setStatus(Applikation.Status.STOPPEN);
			process.stopp();
		}
	}

	private String getApplikationsArgumente() {
		StringBuilder builder = new StringBuilder(1024);
		for (String argument : inkarnation.getAufrufParameter()) {
			if (builder.length() > 0) {
				builder.append(' ');
			}
			builder.append(argument);
		}

		// TODO Inkarnationsname ergänzen

		return builder.toString();
	}

	private void setStatus(Status status) {
		Status oldStatus = getStatus();
		if (oldStatus != status) {
			applikation.setStatus(status);
			fireStatusChanged(oldStatus, status);
		}
	}

	public void startSystemProcess() throws StartStoppException {
		switch (getStatus()) {
		case INSTALLIERT:
		case ABGEBROCHEN:
		case GESTOPPT:
		case KERNSYSTEM:
		case STARTBEDINGUNG:
		case STARTVERZOEGERUNG:
			starteApplikation();
			break;
		case GESTARTET:
		case INITIALISIERT:
		case STARTEN:
		case STOPPBEDINGUNG:
		case STOPPEN:
		case STOPPVERZOEGERUNG:
			throw new StartStoppException("Applikation kann im Status \"" + getStatus() + "\" nicht gestartet werden");
		}
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

	public StoppBedingung getStoppBedingung() {
		return inkarnation.getStoppBedingung();
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
