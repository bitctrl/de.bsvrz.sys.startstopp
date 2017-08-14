package de.bsvrz.sys.stst.test;

import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.bsvrz.dav.daf.main.ApplicationCloseActionHandler;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

public class Application05 implements StandardApplication {

	@Override
	public void parseArguments(ArgumentList argumentList) throws Exception {
		// keine Extra-Argumente erwartet

	}

	@Override
	public void initialize(ClientDavInterface connection) throws Exception {
		connection.setCloseHandler(new ApplicationCloseActionHandler() {

			@Override
			public void close(String error) {
				Random random = new Random(connection.getTime());
				int sekunden = random.nextInt(30);
				Debug logger = Debug.getLogger();
				logger.warning("Ich sollte beendet werden, warte noch " + sekunden + " Sekunden");
				ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
				executor.schedule(() -> {logger.warning("Applikation beendet"); executor.shutdownNow();}, sekunden, TimeUnit.SECONDS);
			}
		});
	}

	public static void main(String[] args) {
		StandardApplicationRunner.run(new Application05(), args);
	}
}
