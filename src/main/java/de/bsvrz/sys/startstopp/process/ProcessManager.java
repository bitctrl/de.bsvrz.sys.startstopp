package de.bsvrz.sys.startstopp.process;

import de.bsvrz.sys.startstopp.config.ConfigurationManager;
import de.bsvrz.sys.startstopp.startstopp.StartStoppOptions;

public class ProcessManager extends Thread {

	private boolean stopped;
	private Object lock = new Object();

	public ProcessManager(StartStoppOptions options, ConfigurationManager configurationManager) {
		super("ProcessManager");
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void run() {
		while (!stopped) {
			System.err.println("Überwache Inkarnationen");
			try {
				synchronized (lock) {
					lock.wait(30000);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
