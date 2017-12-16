package de.bsvrz.sys.stst.test;

import de.bsvrz.dav.daf.main.ApplicationCloseActionHandler;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;
import java.util.concurrent.TimeUnit;

public class Application06 implements StandardApplication {

	private class BackGroundThread extends Thread {

		Debug threadLogger;

		BackGroundThread(Debug logger) {
			threadLogger = logger;
		}

		@Override
		public void run() {
			while(true) {
				try {
					threadLogger.warning("Wait 5 seconds");
					TimeUnit.SECONDS.sleep(5);
				} catch (InterruptedException e) {
					threadLogger.warning("Background wait interrupted!");
				}
			}
		}
	}

	private Debug logger;
	private Thread backGroundThread;



	@Override
	public void parseArguments(ArgumentList argumentList) throws Exception {
		logger = Debug.getLogger();
		logger.warning("Create Background Thread");
		backGroundThread = new BackGroundThread(logger);
		backGroundThread.start();
	}

	@Override
	public void initialize(ClientDavInterface connection) throws Exception {
		logger.warning("Verbunden: " + connection.isConnected());
		connection.setCloseHandler(new ApplicationCloseActionHandler() {

			@Override
			public void close(String error) {
					connection.disconnect(false, "Ende");
					logger.warning("Verbindung getrennt, ich sollte beendet werden!");
			}

		});
	}

	public static void main(String[] args) {
		StandardApplicationRunner.run(new Application06(), args);
	}
}
