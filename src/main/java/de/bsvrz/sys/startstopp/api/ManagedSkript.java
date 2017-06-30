package de.bsvrz.sys.startstopp.api;

import java.util.ArrayList;
import java.util.Collection;

import de.bsvrz.sys.startstopp.api.jsonschema.Startstoppskript;
import de.bsvrz.sys.startstopp.api.jsonschema.Startstoppskriptstatus;

public class ManagedSkript {

	private Startstoppskript skript;
	private Startstoppskriptstatus status = new Startstoppskriptstatus();
	
	public ManagedSkript(Startstoppskript skript) {
		this.skript = skript;
		status.getMessages().addAll(pruefeVollstaendigkeit());
		status.getMessages().addAll(pruefeZirkularitaet());

		if( status.getMessages().isEmpty()) {
			status.setStatus(Startstoppskriptstatus.Status.INITIALIZED);
		} else {
			status.setStatus(Startstoppskriptstatus.Status.FAILURE);
		}
	}

	private Collection<String> pruefeZirkularitaet() {
		Collection<String> result = new ArrayList<>();
		result.add("Zirkularitätsprüfung noch nicht implementiert");
		return result;
	}

	private Collection<String> pruefeVollstaendigkeit() {
		Collection<String> result = new ArrayList<>();
		result.add("Vollständigkeitsprüfung noch nicht implementiert");
		return result;
	}

	public Startstoppskript getSkript() {
		return skript;
	}

	public Startstoppskriptstatus getStatus() {
		return status;
	}
}
