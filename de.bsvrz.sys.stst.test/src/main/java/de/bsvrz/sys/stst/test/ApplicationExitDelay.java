package de.bsvrz.sys.stst.test;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import de.bsvrz.sys.funclib.debug.Debug;

class ApplicationExitDelay implements Runnable {

	private Object lock = new Object();

	@Override
	public void run() {
		Random random = new Random(System.currentTimeMillis());
		int sekunden = random.nextInt(30) + 20;

		synchronized (lock) {
			try {
				lock.wait();
				Debug.getLogger().warning("Ich sollte beendet werden, warte noch " + sekunden + " Sekunden");
				TimeUnit.SECONDS.sleep(sekunden);
				Debug.getLogger().warning("Applikation beendet");
				System.exit(0);
			} catch (InterruptedException e) {
				Debug.getLogger().error("Interrupted", e);
			}
		}
	}

	void cancelWait() {
		synchronized (lock) {
			lock.notifyAll();
		}
	}
}
