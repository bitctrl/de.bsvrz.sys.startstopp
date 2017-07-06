package de.bsvrz.sys.startstopp.console.ui;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.console.StartStoppConsole;

public class ProcessStartAction implements Runnable {

	private Applikation inkarnation;

	public ProcessStartAction(Applikation inkarnation) {
		this.inkarnation = inkarnation;
	}

	@Override
	public void run() {
		try {
			StartStoppConsole.getInstance().getClient().starteApplikation(inkarnation.getInkarnationsName());
		} catch (StartStoppException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "Starten";
	}
}
