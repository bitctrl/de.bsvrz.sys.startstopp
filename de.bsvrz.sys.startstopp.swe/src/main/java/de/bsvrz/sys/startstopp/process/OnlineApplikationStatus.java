package de.bsvrz.sys.startstopp.process;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation.Status;
import de.bsvrz.sys.startstopp.process.OnlineApplikation.TaskType;

abstract class OnlineApplikationStatus {
	
	private Status status;
	private OnlineApplikation applikation;

	OnlineApplikationStatus(Applikation.Status status, OnlineApplikation applikation) {
		this.status = status;
		this.applikation = applikation;
	}
	
	public abstract OnlineApplikationStatus wechsleStatus(TaskType task);
}
