package de.bsvrz.sys.stst.test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

public class Application02 implements StandardApplication {

	@Override
	public void parseArguments(ArgumentList argumentList) throws Exception {
		// keine Extra-Argumente erwartet
		
	}

	@Override
	public void initialize(ClientDavInterface connection) throws Exception {
		// die Applikation beendet sich nach 30 Sekunden
		Executors.newSingleThreadScheduledExecutor().schedule(()->System.exit(0), 30, TimeUnit.SECONDS);
	}
	
	public static void main(String[] args) {
		StandardApplicationRunner.run(new Application02(), args);
	}

}
