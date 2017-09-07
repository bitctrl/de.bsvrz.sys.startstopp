package de.bsvrz.sys.stst.test;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

public class Application01StartAbbruch implements StandardApplication {

	@Override
	public void parseArguments(ArgumentList argumentList) throws Exception {
		throw new IllegalArgumentException("ein Fehler");
	}

	@Override
	public void initialize(ClientDavInterface connection) throws Exception {
	}

	public static void main(String[] args) {
	}

}
