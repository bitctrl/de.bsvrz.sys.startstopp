package de.bsvrz.sys.stst.test;

import java.util.concurrent.Executors;

import de.bsvrz.dav.daf.main.ApplicationCloseActionHandler;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

public class Application01 implements StandardApplication {

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
		StandardApplicationRunner.run(new Application01(), args);
	}

}
