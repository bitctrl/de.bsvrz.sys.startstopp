package de.bsvrz.sys.startstopp.process;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.KernSystem;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.config.StartStoppKonfiguration;

public class StartStoppInkarnation extends Inkarnation {

	private KernSystem kernSystem = null;

	public StartStoppInkarnation(StartStoppKonfiguration skript, Inkarnation inkarnation) throws StartStoppException {
		
		for (KernSystem kernSystem : skript.getSkript().getGlobal().getKernsysteme()) {
			if (kernSystem.getInkarnationsName().equals(inkarnation.getInkarnationsName())) {
				this.kernSystem = kernSystem;
			}
		}
		
		setApplikation(skript.makroResolvedString(inkarnation.getApplikation()));
		for( String aufrufParameter : inkarnation.getAufrufParameter()) {
			getAufrufParameter().add(skript.makroResolvedString(aufrufParameter));
		}
		setInkarnationsName(inkarnation.getInkarnationsName());
		setInkarnationsTyp(inkarnation.getInkarnationsTyp());
		setStartArt(inkarnation.getStartArt());
		setStartBedingung(inkarnation.getStartBedingung());
		setStartFehlerVerhalten(inkarnation.getStartFehlerVerhalten());
		setStoppBedingung(inkarnation.getStoppBedingung());
		setStoppFehlerVerhalten(inkarnation.getStoppFehlerVerhalten());
	}

	public boolean isKernSystem() {
		return kernSystem != null;
	}

	public boolean isSetInitialized() {
		if( kernSystem == null) {
			return false;
		}
		return kernSystem.getInitialize();
	}
}
