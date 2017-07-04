package de.bsvrz.sys.startstopp.process;

import java.util.List;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingungen;
import de.bsvrz.sys.startstopp.config.StartStoppException;

public class ManagedApplikation extends Thread {

	private Applikation applikation;
	private ManagedInkarnation inkarnation;
	private SystemProcess process = null;
	private final Object lock = new Object();
	private boolean stopped;
	private ProcessManager processManager;
	
	public ManagedApplikation(ManagedInkarnation inkarnation) {
		super(inkarnation.getInkarnation().getInkarnationsName());
		
		this.inkarnation = inkarnation;
		applikation = new Applikation();
		applikation.setInkarnationsName(inkarnation.getInkarnation().getInkarnationsName());
		applikation.setStatus(Applikation.Status.INSTALLIERT);
		try {
			applikation.setApplikation(inkarnation.getResolvedApplikation());
			applikation.getArguments().addAll(inkarnation.getResolvedParameter());
		} catch (StartStoppException e) {
			throw new IllegalStateException(e);
		}
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
		if ( process == null) {
			throw new StartStoppException("Es ist kein Prozess verfügbar, der gestoppt werden kann!");
		}
		process.stopp();
	}
	
	@Override
	public void run() {
		while (!stopped) {

			switch(getStatus()) {
			case ABGEBROCHEN:
			case GESTOPPT:
			case GESTARTET:
			case INITIALISIERT:
			case INSTALLIERT:
			case STARTENWARTEN:
			case WIRDGESTOPPT:
			}
			
			System.err.println(processManager.isStartable(this));
			
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

	public void startSystemProcess() {
		// TODO Auto-generated method stub
		
	}

	public void setProcessManager(ProcessManager processManager) {
		this.processManager = processManager;
	}
	
	Applikation.Status getStatus() {
		return applikation.getStatus();
	}

	public List<StartBedingungen> getStartRegeln() {
		return inkarnation.getInkarnation().getStartBedingungen();
	}
}
