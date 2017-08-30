package de.bsvrz.sys.startstopp.process;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation.Status;
import de.bsvrz.sys.startstopp.process.OnlineApplikation.TaskType;

public class StartenWartenStatus extends OnlineApplikationStatus {

	StartenWartenStatus(OnlineApplikation applikation) {
		super(Applikation.Status.STARTENWARTEN, applikation);
	}

	@Override
	public OnlineApplikationStatus wechsleStatus(TaskType task) {
		// TODO Auto-generated method stub
		return this;
	}

}
