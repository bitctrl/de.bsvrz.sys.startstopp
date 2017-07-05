package de.bsvrz.sys.startstopp.process;

import java.util.LinkedHashMap;
import java.util.Map;

public class SkriptStopper extends Thread {

	private final Map<String, StartStoppApplikation> applikationen = new LinkedHashMap<>();
	private final Map<String, StartStoppApplikation> kernsystem = new LinkedHashMap<>();

	public SkriptStopper(ProcessManager processManager) {
		for (StartStoppApplikation applikation : processManager.getManagedApplikationen()) {
			if (applikation.isKernsystem()) {
				kernsystem.put(applikation.getInkarnationsName(), applikation);
			} else {
				this.applikationen.put(applikation.getInkarnationsName(), applikation);
			}
		}
	}

	@Override
	public void run() {
		// TODO Herunterfahren implementieren
		for( String name : applikationen.keySet()) {
			System.err.println("Beende App: " + name);
		}
		for( String name : kernsystem.keySet()) {
			System.err.println("Beende Kernsystem: " + name);
		}
	}
}
