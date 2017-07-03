package de.bsvrz.sys.startstopp.process;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.bsvrz.sys.startstopp.api.ManagedApplikation;

public class SkriptStopper extends Thread {

	private final Map<String, ManagedApplikation> applikationen = new LinkedHashMap<>();
	private final Map<String, ManagedApplikation> kernsystem = new LinkedHashMap<>();

	public SkriptStopper(Map<String, ManagedApplikation> applikationen) {
		for (ManagedApplikation applikation : applikationen.values()) {
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
