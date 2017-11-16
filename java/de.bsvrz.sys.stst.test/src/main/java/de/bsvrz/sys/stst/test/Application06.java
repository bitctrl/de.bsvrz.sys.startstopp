package de.bsvrz.sys.stst.test;

import de.bsvrz.dav.daf.main.ApplicationCloseActionHandler;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

public class Application06 implements StandardApplication {

	private Debug logger;

	@Override
	public void parseArguments(ArgumentList argumentList) throws Exception {
		logger = Debug.getLogger();
	}

	@Override
	public void initialize(ClientDavInterface connection) throws Exception {
		logger.warning("Verbunden: " + connection.isConnected());
		connection.setCloseHandler(new ApplicationCloseActionHandler() {
			
			@Override
			public void close(String error) {
				logger.warning("Ich sollte beendet werden!");
			}
		});
		new Thread(()->{while(true) {Thread.yield();}}).start();
		
	}
	
	public static void main(String[] args) {
		StandardApplicationRunner.run(new Application06(), args);
	}
}
