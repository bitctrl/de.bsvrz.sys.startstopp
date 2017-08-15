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
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.naming.spi.NamingManager;

/**
 * Klasse zum Einlesen bzw. Auswerten der Standardausgabe bzw.
 * Standardfehlerausgabe einer Inkarnation. Der Thread wird automatisch durch
 * den Konstruktor der Klasse gestartet.
 */
class AusgabeVerarbeitung {

	public class ProcessReader implements Runnable {

		private static final int MAX_LOG_SIZE = 500;
		private static final long MAX_LOG_TIME_IN_MSEC = 30000L;
		private List<String> destination;
		private InputStream stream;

		ProcessReader(InputStream stream, List<String> destination) {
			this.stream = stream;
			this.destination = destination;
		}

		@Override
		public void run() {
			long startTime = System.currentTimeMillis();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.defaultCharset()))) {
				ExecutorService readerExecutor = Executors.newSingleThreadExecutor(new NamingThreadFactory("Reader"));
				String line = null;
				boolean running = true;
				do {
					Future<String> future = readerExecutor.submit(() -> {
						return reader.readLine();
					});
					try {
						line = future.get(1000, TimeUnit.MILLISECONDS);
					} catch (@SuppressWarnings("unused") TimeoutException e) {
						running = (System.currentTimeMillis() - startTime) < MAX_LOG_TIME_IN_MSEC;
						System.err.println("Running: " + running);
						continue;
					}
					running = (System.currentTimeMillis() - startTime) < MAX_LOG_TIME_IN_MSEC;
					System.err.println("Running2: " + running);
					if (line != null) {
						destination.add(line);
						if (destination.size() > MAX_LOG_SIZE) {
							destination.add("Log-Limit überschritten!");
							line = null;
						}
					} else {
						running = false;
					}
				} while (running);
				readerExecutor.shutdownNow();
			} catch (IOException | InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

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

		ProcessReader errorReader = new ProcessReader(process.getErrorStream(), processStdError);
		Executors.newSingleThreadExecutor(new NamingThreadFactory("StdErrReader_" + inkarnation)).submit(errorReader);
		
		ProcessReader outputReader = new ProcessReader(process.getInputStream(), processStdOutput);
		Executors.newSingleThreadExecutor(new NamingThreadFactory("StdOutReader_" + inkarnation)).submit(outputReader);
	}

	public String getStdErrText() {
		return String.join("\n", processStdError);
	}

	public String getStdOutText() {
		return String.join("\n", processStdOutput);
	}
}
