package de.bsvrz.sys.startstopp.console.ui;

public class EditorCloseAction implements Runnable {

	private StartStoppEditWindow startStoppEditWindow;

	public EditorCloseAction(StartStoppEditWindow startStoppEditWindow) {
		this.startStoppEditWindow = startStoppEditWindow;
	}

	@Override
	public void run() {
		startStoppEditWindow.close();
	}
	
	@Override
	public String toString() {
		return "Verlassen";
	}

}
