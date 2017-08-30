package de.bsvrz.sys.startstopp.process;

import java.text.DateFormat;
import java.util.Date;
import java.util.Set;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation.Status;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.process.OnlineApplikation.TaskType;

public class StoppenWartenStatus extends OnlineApplikationStatus {

	StoppenWartenStatus(OnlineApplikation applikation) {
		super(Applikation.Status.STOPPENWARTEN, applikation);
	}

	@Override
	public boolean wechsleStatus(TaskType task, StartStoppStatus.Status startStoppStatus) {
		if (task != TaskType.STOPPFEHLER) {
			if (applikation.getOnlineApplikationTimer().isStoppFehlerTaskAktiv()) {
				return false;
			}
		}

		String message = applikation.kernSystemGestoppt();
		if (message != null) {
			applikation.getOnlineApplikationTimer().clear();
			return applikation.updateStatus(Applikation.Status.STOPPENWARTEN, message);
		}

		StoppBedingung stoppBedingung = applikation.getStoppBedingung();
		if (stoppBedingung != null) {
			message = applikation.stoppBedingungErfuellt();
			if (message != null) {
				applikation.getOnlineApplikationTimer().clear();
				return applikation.updateStatus(Applikation.Status.STOPPENWARTEN, message);
			}
			if (task != TaskType.WARTETIMER) {
				if (applikation.getOnlineApplikationTimer().isWarteTaskAktiv()) {
					return applikation.updateStatus(Applikation.Status.STOPPENWARTEN,
							applikation.getApplikation().getStartMeldung());
				}
				long warteZeitInMsec;
				try {
					warteZeitInMsec = Util.convertToWarteZeitInMsec(stoppBedingung.getWartezeit());
				} catch (StartStoppException e) {
					throw new IllegalStateException(
							"Sollte hier nicht passieren, weil nur geprüfte Skripte ausgeführt werden!", e);
				}
				if (warteZeitInMsec > 0) {
					applikation.getOnlineApplikationTimer().initWarteTask(warteZeitInMsec);
					return applikation.updateStatus(Applikation.Status.STOPPENWARTEN, "Wartezeit bis " + DateFormat
							.getDateTimeInstance().format(new Date(System.currentTimeMillis() + warteZeitInMsec)));
				}
			} else {
				applikation.getOnlineApplikationTimer().clear();
			}
		}

		if (applikation.getOnlineApplikationTimer().isWarteTaskAktiv()) {
			return false;
		}

		if (applikation.getOnlineApplikationTimer().isStoppFehlerTaskAktiv()) {
			return false;
		}
		
		applikation.getOnlineApplikationTimer().initStoppFehlerTask();
		applikation.requestStopp("");
		return true;
	}

}
