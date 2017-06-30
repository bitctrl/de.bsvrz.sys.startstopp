package de.bsvrz.sys.startstopp;

import java.util.List;

import de.bsvrz.sys.startstopp.api.client.StartStoppClient;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Startstoppskript;
import de.bsvrz.sys.startstopp.api.jsonschema.Startstoppskriptstatus;
import de.bsvrz.sys.startstopp.api.jsonschema.Startstoppstatus;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.config.StartStoppStatusException;

public class AppTest {

	public static void main(String[] args) {

		try {
			StartStoppClient client = new StartStoppClient("localhost", 9998);

			Startstoppstatus startStoppStatus = client.getStartStoppStatus();
			System.err.println(startStoppStatus);
			
			client.stoppStartStopp();
			client.restartStartStopp();
			
			Startstoppskript currentSkript = client.getCurrentSkript();
			System.err.println(currentSkript);
			
			currentSkript = client.setCurrentSkript(currentSkript);
			System.err.println(currentSkript);
			
			Startstoppskriptstatus currentSkriptStatus = client.getCurrentSkriptStatus();
			System.err.println(currentSkriptStatus);

			List<Applikation> applikationen = client.getApplikationen();
			System.err.println(applikationen);

			Applikation applikation = client.getApplikation("Datenverteiler");
			System.err.println(applikation);

			try {
				applikation = client.starteApplikation("Datenverteiler");
				System.err.println(applikation);
			} catch (StartStoppStatusException e) {
				System.err.println("\t" + e.getLocalizedMessage());
				for (String message : e.getMessages()) {
					System.err.println("\t" + message);
				}
			}

			try {
				applikation = client.restarteApplikation("Datenverteiler");
				System.err.println(applikation);
			} catch (StartStoppStatusException e) {
				System.err.println("\t" + e.getLocalizedMessage());
				for (String message : e.getMessages()) {
					System.err.println("\t" + message);
				}
			}

			try {
				applikation = client.stoppeApplikation("Datenverteiler");
				System.err.println(applikation);
			} catch (StartStoppStatusException e) {
				System.err.println("\t" + e.getLocalizedMessage());
				for (String message : e.getMessages()) {
					System.err.println("\t" + message);
				}
			}
			
			
		} catch (StartStoppException e) {
			System.err.println(e.getLocalizedMessage());
		}
	}
}
