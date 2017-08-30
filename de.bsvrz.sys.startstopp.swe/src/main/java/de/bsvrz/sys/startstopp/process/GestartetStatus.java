package de.bsvrz.sys.startstopp.process;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation.Status;
import de.bsvrz.sys.startstopp.process.OnlineApplikation.TaskType;

public class GestartetStatus extends OnlineApplikationStatus {

	GestartetStatus(OnlineApplikation applikation) {
		super(Applikation.Status.GESTARTET, applikation);
	}

	@Override
	public OnlineApplikationStatus wechsleStatus(TaskType task) {
		// TODO Auto-generated method stub
		return this;
	}

}
