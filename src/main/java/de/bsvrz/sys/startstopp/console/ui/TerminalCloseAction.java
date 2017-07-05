package de.bsvrz.sys.startstopp.console.ui;

public class TerminalCloseAction implements Runnable {

	private StartStoppOnlineWindow startStoppOnlineWindow;

	public TerminalCloseAction(StartStoppOnlineWindow startStoppOnlineWindow) {
		this.startStoppOnlineWindow = startStoppOnlineWindow;
	}

	@Override
	public void run() {
		startStoppOnlineWindow.close();
	}
	
	@Override
	public String toString() {
		return "Schließen";
	}

}
