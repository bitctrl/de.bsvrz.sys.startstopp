package de.bsvrz.sys.startstopp.process;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation.Status;
import de.bsvrz.sys.startstopp.process.OnlineApplikation.TaskType;

public class GestopptStatus extends OnlineApplikationStatus {

	GestopptStatus(OnlineApplikation applikation) {
		super(Applikation.Status.GESTOPPT, applikation);
	}

	@Override
	public OnlineApplikationStatus wechsleStatus(TaskType task) {
		// TODO Auto-generated method stub
		return this;
	}

}
