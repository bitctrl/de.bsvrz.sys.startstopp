package de.bsvrz.sys.startstopp;

import java.util.List;

import de.bsvrz.sys.startstopp.api.client.StartStoppClient;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkriptStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.config.StartStoppStatusException;

public class AppTest {

	public static void main(String[] args) {

		try {
			StartStoppClient client = new StartStoppClient("localhost", 9998);

			System.err.println("STARTSTOPPSTATUS");
			StartStoppStatus startStoppStatus = client.getStartStoppStatus();
			System.err.println(startStoppStatus);
			System.err.println();

			System.err.println("STARTSTOPP_STOPP");
			client.stoppStartStopp();
			System.err.println();

			System.err.println("STARTSTOPP_RESTART");
			client.restartStartStopp();
			System.err.println();

			StartStoppSkript currentSkript = null;

			System.err.println("SKRIPT_CURRENT");
			try {
				currentSkript = client.getCurrentSkript();
				System.err.println(currentSkript);
				System.err.println();
			} catch (StartStoppStatusException e) {
				System.err.println(e.getLocalizedMessage());
			}

			if (currentSkript != null) {
				System.err.println("SET_SKRIPT_CURRENT");
				try {
					currentSkript = client.setCurrentSkript("Alles neu", currentSkript);
					System.err.println(currentSkript);
				} catch (StartStoppStatusException e) {
					System.err.println(e.getLocalizedMessage());
					for (String message : e.getMessages()) {
						System.err.println("\t" + message);
					}
				}
				System.err.println();
			}

			System.err.println("SKRIPT_CURRENT_STATUS");
			StartStoppSkriptStatus currentSkriptStatus = client.getCurrentSkriptStatus();
			System.err.println(currentSkriptStatus);
			System.err.println();

			System.err.println("APPLIKATIONEN");
			List<Applikation> applikationen = client.getApplikationen();
			System.err.println(applikationen);
			System.err.println();

			System.err.println("APPLIKATION_DATENVERTEILER");
			Applikation applikation = client.getApplikation("Datenverteiler");
			System.err.println(applikation);
			System.err.println();

			System.err.println("APPLIKATION_DATENVERTEILER_START");
			try {
				applikation = client.starteApplikation("Datenverteiler");
				System.err.println(applikation);
			} catch (StartStoppStatusException e) {
				System.err.println("\t" + e.getLocalizedMessage());
				for (String message : e.getMessages()) {
					System.err.println("\t" + message);
				}
			}
			System.err.println();

			System.err.println("APPLIKATION_DATENVERTEILER_RESTART");
			try {
				applikation = client.restarteApplikation("Datenverteiler");
				System.err.println(applikation);
			} catch (StartStoppStatusException e) {
				System.err.println("\t" + e.getLocalizedMessage());
				for (String message : e.getMessages()) {
					System.err.println("\t" + message);
				}
			}
			System.err.println();

			System.err.println("APPLIKATION_DATENVERTEILER_STOPP");
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
