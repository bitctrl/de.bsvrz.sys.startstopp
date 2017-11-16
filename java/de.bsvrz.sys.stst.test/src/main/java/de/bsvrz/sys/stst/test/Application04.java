package de.bsvrz.sys.stst.test;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

public class Application04 implements StandardApplication {

	@Override
	public void parseArguments(ArgumentList argumentList) throws Exception {
		// keine Extra-Argumente erwartet
		
	}

	@Override
	public void initialize(ClientDavInterface connection) throws Exception {
		// die Applikation tut nichts
	}

	public static void main(String[] args) {
		StandardApplicationRunner.run(new Application04(), args);
	}

}
