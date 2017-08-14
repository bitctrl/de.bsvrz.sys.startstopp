/*
 * Segment 10 System (Sys), SWE 10.1 StartStopp
 * Copyright (C) 2007-2017 BitCtrl Systems GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Contact Information:<br>
 * BitCtrl Systems GmbH<br>
 * Weißenfelser Straße 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.sys.startstopp.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Klasse zum Einlesen bzw. Auswerten der Standardausgabe bzw.
 * Standardfehlerausgabe einer Inkarnation. Der Thread wird automatisch durch
 * den Konstruktor der Klasse gestartet.
 */
class AusgabeVerarbeitung {

	public class ProcessReader implements Runnable {

		private static final int MAX_LOG_SIZE = 500;
		private List<String> destination;
		private InputStream stream;

		public ProcessReader(InputStream stream, List<String> destination) {
			this.stream = stream;
			this.destination = destination;
		}

		@Override
		public void run() {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.defaultCharset()))) {
				String line = null;
				do {
					if (reader.ready()) {
						line = reader.readLine();
						destination.add(line);
						if (destination.size() > MAX_LOG_SIZE) {
							destination.add("Log-Limit überschritten!");
							line = null;
						}
					} else {
						TimeUnit.MILLISECONDS.sleep(50);
					}
				} while (true);
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

			System.err.println("Stopped");
		}
	}

	private static final Debug LOGGER = Debug.getLogger();

	private final ScheduledThreadPoolExecutor executor;

	private Process process;
	private List<String> processStdOutput = new ArrayList<>();
	private List<String> processStdError = new ArrayList<>();

	/**
	 * Konstruktor der Klasse, startet automatisch den Thread, der die
	 * Eingangsstr&ouml;me verarbeitet.
	 * 
	 * @param inkarnation
	 *            Name der Inkarnation
	 * @param prozess
	 *            Systemprozess der Inkarnation
	 */
	AusgabeVerarbeitung(final String inkarnation, final Process prozess) {
		this.process = prozess;

		executor = new ScheduledThreadPoolExecutor(3);

		ProcessReader errorReader = new ProcessReader(process.getErrorStream(), processStdError);
		ScheduledFuture<?> errorFuture = executor.schedule(errorReader, 0, TimeUnit.MILLISECONDS);

//		ProcessReader outputReader = new ProcessReader(process.getInputStream(), processStdOutput);
//		ScheduledFuture<?> outputFuture = executor.schedule(outputReader, 0, TimeUnit.MILLISECONDS);
		executor.schedule(() -> {
			errorFuture.cancel(true);
		//	outputFuture.cancel(true);
			executor.shutdownNow();
		}, 30, TimeUnit.SECONDS);
	}

	public String getStdErrText() {
		return String.join("\n", processStdError);
	}

	public String getStdOutText() {
		return String.join("\n", processStdOutput);
	}
}
