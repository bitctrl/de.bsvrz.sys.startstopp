package de.bsvrz.sys.startstopp.process;

import java.io.IOException;

public class ProzessStarter {

	public synchronized static Process start(String cmdarray) throws IOException {
		Process proc = null;

		String[] env = { "LANG=de_DE@euro" };

		if (Tools.isWindows()) {
			proc = Runtime.getRuntime().exec(cmdarray, null, null);
		} else {
			proc = Runtime.getRuntime().exec(cmdarray, env, null);
		}
		
		return proc;
	}

}
