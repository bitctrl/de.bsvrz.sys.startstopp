package de.bsvrz.sys.startstopp.startstopp;

import de.bsvrz.sys.startstopp.config.StartStoppException;

public class StartStoppDavException extends StartStoppException {

	public StartStoppDavException() {
		super("Es besteht keine Datenverteilerverbindung");
	}
}
