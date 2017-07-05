package de.bsvrz.sys.startstopp.process;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation.Status;

public interface ManagedApplikationListener {
	void applicationStatusChanged(StartStoppApplikation managedApplikation, Status oldValue, Status newValue);
}
