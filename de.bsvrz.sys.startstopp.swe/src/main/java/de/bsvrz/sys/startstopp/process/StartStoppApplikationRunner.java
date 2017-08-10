package de.bsvrz.sys.startstopp.process;

import java.text.DateFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;

public class StartStoppApplikationRunner extends Thread {

	private enum TimerType {
		NOTIMER, WARTETIMER, INTERVALLTIMER;
	}

	private static final Debug LOGGER = Debug.getLogger();
	private StartStoppApplikation applikation;
	private Object lock = new Object();
	private boolean stopped;
	private ProzessManager prozessManager;
	private ScheduledFuture<?> warteTask;

	public StartStoppApplikationRunner(ProzessManager processmanager, StartStoppApplikation applikation) {
		super("Runner_" + applikation.getInkarnation().getInkarnationsName());
		setDaemon(true);
		this.prozessManager = processmanager;
		this.applikation = applikation;
	}

	@Override
	public void run() {
		while (!stopped) {

			checkState(TimerType.NOTIMER);

			synchronized (lock) {
				System.err.println("Run: " + applikation.getInkarnation().getInkarnationsName() + " Status: "
						+ applikation.getStatus() + " PM-Status: " + prozessManager.getStatus());
				try {
					lock.wait(10000);
				} catch (InterruptedException e) {
					LOGGER.warning(e.getLocalizedMessage());
				}
			}
		}
	}

	private void handleInstalliertState() {

		if (prozessManager.getStatus() != ProzessManager.Status.RUNNING) {
			return;
		}

		switch (applikation.getInkarnation().getStartArt().getOption()) {
		case AUTOMATISCH:
		case INTERVALLABSOLUT:
		case INTERVALLRELATIV:
			// TODO Intervallstarts implementieren
			break;
		case MANUELL:
			return;
		}

		Set<String> applikationen = prozessManager.waitForKernsystemStart(applikation);
		if (!applikationen.isEmpty()) {
			applikation.updateStatus(Applikation.Status.STARTENWARTEN);
			applikation.setStartMeldung("Warte auf Kernsystem: " + applikationen.toString());
			return;
		}

		if (!applikation.isKernsystem()) {
			String davConnectionMsg = prozessManager.getDavConnectionMsg();
			if (davConnectionMsg != null) {
				LOGGER.info(applikation.getInkarnation().getInkarnationsName() + ": " + davConnectionMsg);
				applikation.updateStatus(Applikation.Status.STARTENWARTEN);
				applikation.setStartMeldung(davConnectionMsg);
				return;
			}
		}

		StartBedingung startBedingung = applikation.getStartBedingung();
		if (startBedingung != null) {
			applikationen = prozessManager.waitForStartBedingung(applikation);
			if (!applikationen.isEmpty()) {
				applikation.updateStatus(Applikation.Status.STARTENWARTEN);
				applikation.setStartMeldung("Warte auf : " + applikationen);
				return;
			}
			int warteZeitInMsec = convertToWarteZeitInMsec(startBedingung.getWartezeit());
			System.err.println(
					"Wartezeit: " + applikation.getInkarnation().getInkarnationsName() + " " + warteZeitInMsec + " ms");

			if (warteZeitInMsec > 0) {
				applikation.updateStatus(Applikation.Status.STARTENWARTEN);
				setWarteTimer(warteZeitInMsec);
				return;
			}
		}

		applikation.starteApplikation();
	}

	private void handleStartenWartenState(TimerType timerType) {

		Set<String> applikationen = prozessManager.waitForKernsystemStart(applikation);
		if (!applikationen.isEmpty()) {
			setWarteTimer(0);
			applikation.updateStatus(Applikation.Status.STARTENWARTEN);
			applikation.setStartMeldung("Warte auf Kernsystem: " + applikationen);
			return;
		}

		if (!applikation.isKernsystem()) {
			String davConnectionMsg = prozessManager.getDavConnectionMsg();
			if (davConnectionMsg != null) {
				setWarteTimer(0);
				LOGGER.info(applikation.getInkarnation().getInkarnationsName() + ": " + davConnectionMsg);
				applikation.updateStatus(Applikation.Status.STARTENWARTEN);
				applikation.setStartMeldung(davConnectionMsg);
				return;
			}
		}

		StartBedingung startBedingung = applikation.getStartBedingung();
		if (startBedingung != null) {
			applikationen = prozessManager.waitForStartBedingung(applikation);
			if (!applikationen.isEmpty()) {
				setWarteTimer(0);
				applikation.updateStatus(Applikation.Status.STARTENWARTEN);
				applikation.setStartMeldung("Warte auf : " + applikationen);
				return;
			}
			if (timerType != TimerType.WARTETIMER) {
				if (warteTaskIsActive()) {
					applikation.updateStatus(Applikation.Status.STARTENWARTEN);
					return;
				}
				int warteZeitInMsec = convertToWarteZeitInMsec(startBedingung.getWartezeit());
				if (warteZeitInMsec > 0) {
					applikation.updateStatus(Applikation.Status.STARTENWARTEN);
					setWarteTimer(warteZeitInMsec);
					applikation.setStartMeldung("Wartezeit bis " + DateFormat.getDateTimeInstance()
							.format(new Date(System.currentTimeMillis() + warteZeitInMsec)));
					return;
				}
			}
		}

		if (warteTaskIsActive()) {
			return;
		}

		applikation.starteApplikation();
		applikation.setStartMeldung("");
	}

	
	private void handleStoppenWartenState(TimerType timerType) {

		Set<String> applikationen = prozessManager.waitForKernsystemStopp(applikation);
		if (!applikationen.isEmpty()) {
			setWarteTimer(0);
			applikation.updateStatus(Applikation.Status.STOPPENWARTEN);
			applikation.setStartMeldung("Kernsystem wartet auf: " + applikationen);
			return;
		}

		StoppBedingung stoppBedingung = applikation.getStoppBedingung();
		if (stoppBedingung != null) {
			applikationen = prozessManager.waitForStoppBedingung(applikation);
			if (!applikationen.isEmpty()) {
				setWarteTimer(0);
				applikation.updateStatus(Applikation.Status.STOPPENWARTEN);
				applikation.setStartMeldung("Warte auf : " + applikationen);
				return;
			}
			if (timerType != TimerType.WARTETIMER) {
				if (warteTaskIsActive()) {
					applikation.updateStatus(Applikation.Status.STOPPENWARTEN);
					return;
				}
				int warteZeitInMsec = convertToWarteZeitInMsec(stoppBedingung.getWartezeit());
				if (warteZeitInMsec > 0) {
					applikation.updateStatus(Applikation.Status.STOPPENWARTEN);
					setWarteTimer(warteZeitInMsec);
					applikation.setStartMeldung("Wartezeit bis " + DateFormat.getDateTimeInstance()
							.format(new Date(System.currentTimeMillis() + warteZeitInMsec)));
					return;
				}
			} else {
				setWarteTimer(0);
			}
		}

		if (warteTaskIsActive()) {
			return;
		}

		applikation.stoppeApplikation(false);
	}
	
	private int convertToWarteZeitInMsec(String warteZeitStr) {
		// TODO Auto-generated method stub
		return Integer.parseInt(warteZeitStr) * 1000;
	}

	private void setWarteTimer(int warteZeitInMsec) {

		System.err.println(
				"Setze Wartetimer: " + applikation.getInkarnation().getInkarnationsName() + " auf " + warteZeitInMsec);

		if (warteTask != null) {
			warteTask.cancel(true);
		}

		if (warteZeitInMsec <= 0) {
			return;
		}

		System.err.println("Delay: " + warteZeitInMsec + " " + applikation.getInkarnation().getInkarnationsName());
		warteTask = prozessManager.getExecutor().schedule(() -> checkState(TimerType.WARTETIMER), warteZeitInMsec,
				TimeUnit.MILLISECONDS);
		System.err.println("WarteTask");
	}

	private void checkState(TimerType timerType) {

		switch (applikation.getStatus()) {
		case GESTARTET:
		case GESTOPPT:
		case INITIALISIERT:
			LOGGER.finest(applikation.getStatus() + ": " + applikation.getInkarnation().getInkarnationsName()
					+ " keine Aktualisierung möglich");
			break;
		case INSTALLIERT:
			handleInstalliertState();
			break;
		case STARTENWARTEN:
			handleStartenWartenState(timerType);
			break;
		case STOPPENWARTEN:
			handleStoppenWartenState(timerType);
			break;
		default:
			break;
		}
	}

	private boolean warteTaskIsActive() {
		if (warteTask != null) {
			System.err.println("Delay noch " + warteTask.getDelay(TimeUnit.MILLISECONDS) + " ms");
		}
		return (warteTask != null) && warteTask.getDelay(TimeUnit.MILLISECONDS) > 0;
	}

}
