package de.bsvrz.sys.startstopp.console;

public class StartStoppConsoleOptions {

	private String host = "localhost";
	private int port = 3000;

	public StartStoppConsoleOptions(String[] args) {
		// TODO Kommandozeilenparameter einlesen
	}

	public String getHost() {
		return host ;
	}

	public int getPort() {
		return port ;
	}

}
