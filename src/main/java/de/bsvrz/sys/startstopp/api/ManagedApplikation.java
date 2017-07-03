package de.bsvrz.sys.startstopp.api;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.process.SystemProcess;

public class ManagedApplikation {

	Applikation applikation;
	private ManagedInkarnation inkarnation;
	
	SystemProcess process = new SystemProcess();
	
	public ManagedApplikation(ManagedInkarnation inkarnation) {
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
}
