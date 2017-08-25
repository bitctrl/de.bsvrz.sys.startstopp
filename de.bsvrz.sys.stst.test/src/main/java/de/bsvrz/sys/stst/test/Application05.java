package de.bsvrz.sys.stst.test;

import java.util.Random;
import java.util.concurrent.Executors;
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
		ApplicationExitDelay task = new ApplicationExitDelay();
		Executors.newSingleThreadExecutor().submit(task);
		connection.setCloseHandler(new ApplicationCloseActionHandler() {

			@Override
			public void close(String error) {
				task.cancelWait();
			}
		});
	}

	public static void main(String[] args) {
		StandardApplicationRunner.run(new Application05(), args);
	}
}
