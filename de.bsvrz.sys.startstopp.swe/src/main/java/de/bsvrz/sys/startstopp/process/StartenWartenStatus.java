package de.bsvrz.sys.startstopp.process;

import java.text.DateFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation.Status;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.process.OnlineApplikation.TaskType;

public class StartenWartenStatus extends OnlineApplikationStatus {

	private static final Debug LOGGER = Debug.getLogger();

	StartenWartenStatus(OnlineApplikation applikation) {
		super(Applikation.Status.STARTENWARTEN, applikation);
	}

	@Override
	public boolean wechsleStatus(TaskType task, StartStoppStatus.Status startStoppStatus) {

		if (task == TaskType.STOPPFEHLER) {
			return false;
		}

		if (!applikation.isManuellGestartetOderGestoppt()
				&& startStoppStatus != StartStoppStatus.Status.RUNNING) {
			applikation.getOnlineApplikationTimer().clear();
			return applikation.updateStatus(Applikation.Status.STARTENWARTEN, "");
		}

		String kernSystemMessage = applikation.kernSystemVerfuegbar();
		if (kernSystemMessage != null) {
			LOGGER.info(applikation.getName() + ": " + kernSystemMessage);
			return applikation.updateStatus(Applikation.Status.STARTENWARTEN, kernSystemMessage);
		}
		
		StartBedingung startBedingung = applikation.getStartBedingung();
		if (startBedingung != null) {
			String message = applikation.startbedingungErfuellt();
			if (message != null) {
				applikation.getOnlineApplikationTimer().clear();
				return applikation.updateStatus(Applikation.Status.STARTENWARTEN, message);
			}
			if (task != TaskType.WARTETIMER) {
				if (applikation.getOnlineApplikationTimer().isWarteTaskAktiv()) {
					return applikation.updateStatus(Applikation.Status.STARTENWARTEN,
							applikation.getApplikation().getStartMeldung());
				}
				long warteZeitInMsec;
				try {
					warteZeitInMsec = Util.convertToWarteZeitInMsec(startBedingung.getWartezeit());
				} catch (StartStoppException e) {
					throw new IllegalStateException(
							"Sollte hier nicht passieren, weil nur geprüfte Skripte ausgeführt werden!", e);
				}
				if (warteZeitInMsec > 0) {
					applikation.getOnlineApplikationTimer().initWarteTask(warteZeitInMsec);
					return applikation.updateStatus(Applikation.Status.STARTENWARTEN, "Wartezeit bis " + DateFormat
							.getDateTimeInstance().format(new Date(System.currentTimeMillis() + warteZeitInMsec)));
				}
			}
		}

		if (applikation.getOnlineApplikationTimer().isWarteTaskAktiv()) {
			return false;
		}

		if (applikation.getOnlineApplikationTimer().isIntervallTaskAktiv()) {
			if (task != TaskType.INTERVALLTIMER) {
				return false;
			}
		}

		if (task != TaskType.INTERVALLTIMER) {
			switch (applikation.getStartArtOption()) {
			case INTERVALLRELATIV:
			case INTERVALLABSOLUT:
				try {
					applikation.getOnlineApplikationTimer().initZyklusTimer();
					return applikation.updateStatus(Applikation.Status.STARTENWARTEN, "Nächster Ausführungszeitpunkt "
							+ DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()
									+ applikation.getOnlineApplikationTimer().getTaskDelay(TimeUnit.MILLISECONDS))));
				} catch (StartStoppException e) {
					LOGGER.warning(e.getLocalizedMessage());
					return false;
				}
			case AUTOMATISCH:
			case MANUELL:
			default:
				break;
			}
		}

		applikation.starteOSApplikation();
		return applikation.updateStatus(Applikation.Status.GESTARTET, "");
	}

}
