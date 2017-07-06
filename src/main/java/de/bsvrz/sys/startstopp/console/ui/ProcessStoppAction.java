package de.bsvrz.sys.startstopp.console.ui;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.console.StartStoppConsole;

public class ProcessStoppAction implements Runnable {

	private Applikation inkarnation;

	public ProcessStoppAction(Applikation inkarnation) {
		this.inkarnation = inkarnation;
	}

	@Override
	public void run() {
		try {
			StartStoppConsole.getInstance().getClient().stoppeApplikation(inkarnation.getInkarnationsName());
		} catch (StartStoppException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Override
	public String toString() {
		return "Anhalten";
	}
}
