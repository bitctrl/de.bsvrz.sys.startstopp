package de.bsvrz.sys.startstopp.data;

import java.util.Date;

public class OnlineInkarnation {

	public enum StartStoppStatus {
		INAKTIV,
		STARTET,
		AKTIV,
		GESTOPPT
	}
	
	private Inkarnation inkarnation;

	public OnlineInkarnation(Inkarnation inkarnation) {
		this.inkarnation = inkarnation;
	}
	
	
	StartStoppStatus status = StartStoppStatus.INAKTIV;
	Date ersteStartZeit = new Date();
	Date letzteStartZeit;
	int startCount;

	public String getApplikationsName() {
		return inkarnation.getInkarnationsName();
	}
	public StartStoppStatus getStatus() {
		return status;
	}
	public Date getErsteStartZeit() {
		return ersteStartZeit;
	}
}
