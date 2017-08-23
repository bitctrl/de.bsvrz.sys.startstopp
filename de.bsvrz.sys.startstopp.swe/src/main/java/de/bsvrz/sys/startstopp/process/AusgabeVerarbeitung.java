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

import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Klasse zum Einlesen bzw. Auswerten der Standardausgabe bzw.
 * Standardfehlerausgabe einer Inkarnation. Der Thread wird automatisch durch
 * den Konstruktor der Klasse gestartet.
 */
class AusgabeVerarbeitung implements Runnable {

	private static final Debug LOGGER = Debug.getLogger();
	private static final int MAX_LOG_SIZE = 500;
	private static final long MAX_LOG_TIME_IN_MSEC = 30000L;
	private InputStream stream;

	private Object lock = new Object();
	private Object completer = new Object();

	private boolean running;
	private OSApplikation inkarnation;

	/**
	 * Konstruktor der Klasse, startet automatisch den Thread, der die
	 * Eingangsstr&ouml;me verarbeitet.
	 * 
	 * @param inkarnation
	 *            Name der Inkarnation
	 * @param prozess
	 *            Systemprozess der Inkarnation
	 */
	AusgabeVerarbeitung(final OSApplikation inkarnation, final Process prozess) {
		stream = prozess.getInputStream();
		this.inkarnation = inkarnation;
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		running = true;
		int lineCounter = 0;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.defaultCharset()))) {
			String line = null;
			do {
				if (lineCounter > MAX_LOG_SIZE) {
					inkarnation.addProzessAusgabe("Log-Limit überschritten!");
					anhalten();
					continue;
				}
				if ((System.currentTimeMillis() - startTime) > MAX_LOG_TIME_IN_MSEC) {
					inkarnation.addProzessAusgabe("Log-Zeit-Limit überschritten!");
					anhalten();
					continue;
				}

				if (reader.ready()) {
					line = reader.readLine();
					// System.err.println("Line: " + line);
					if (line != null) {
						inkarnation.addProzessAusgabe(line);
						lineCounter++;
					}
				} else {
					try {
						synchronized (lock) {
							lock.wait(300);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} while (running);

			while (reader.ready()) {
				line = reader.readLine();
				if (line != null) {
					inkarnation.addProzessAusgabe(line);
				}
			}

			stream.close();

			synchronized (completer) {
				completer.notifyAll();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void anhalten() {

		if (!running) {
			return;
		}

		synchronized (completer) {
			synchronized (lock) {
				running = false;
				lock.notifyAll();
			}

			try {
				completer.wait(10000);
			} catch (InterruptedException e) {
				LOGGER.warning(e.getLocalizedMessage());
			}
		}
	}
}
