package de.bsvrz.sys.startstopp.process;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation.Status;
import de.bsvrz.sys.startstopp.process.OnlineApplikation.TaskType;

abstract class OnlineApplikationStatus {
	
	private Status status;
	protected OnlineApplikation applikation;

	OnlineApplikationStatus(Applikation.Status status, OnlineApplikation applikation) {
		this.status = status;
		this.applikation = applikation;
	}
	
	public abstract boolean wechsleStatus(TaskType task, StartStoppStatus.Status startStoppStatus);

	public Status getStatus() {
		return status;
	}

}
