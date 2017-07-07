package de.bsvrz.sys.startstopp.console.ui;

import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.console.StartStoppConsole;

public class StartStoppExitAction implements Runnable {

	@Override
	public void run() {
		try {
			StartStoppConsole.getInstance().getClient().exitStartStopp();
		} catch (StartStoppException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "StartStopp beenden";
	}
}
