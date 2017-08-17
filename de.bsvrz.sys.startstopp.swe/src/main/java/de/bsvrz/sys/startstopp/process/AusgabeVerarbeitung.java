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

import com.sun.jna.platform.win32.COM.RunningObjectTable;

/**
 * Klasse zum Einlesen bzw. Auswerten der Standardausgabe bzw.
 * Standardfehlerausgabe einer Inkarnation. Der Thread wird automatisch durch
 * den Konstruktor der Klasse gestartet.
 */
class AusgabeVerarbeitung extends Thread {

	private static final int MAX_LOG_SIZE = 500;
	private static final long MAX_LOG_TIME_IN_MSEC = 30000L;
	private List<String> destination = new ArrayList<>();
	private InputStream stream;
	private Object lock = new Object();
	private boolean running;

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
		super("Ausgabeverarbeitung_" + inkarnation);
		setDaemon(true);
		stream = prozess.getInputStream();
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		running = true;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.defaultCharset()))) {
			String line = null;
			do {
				if (destination.size() > MAX_LOG_SIZE) {
					destination.add("Log-Limit überschritten!");
					running = false;
					continue;
				}
				if ((System.currentTimeMillis() - startTime) > MAX_LOG_TIME_IN_MSEC) {
					destination.add("Log-Zeit-Limit überschritten!");
					running = false;
					continue;
				}

				if (reader.ready()) {
					line = reader.readLine();
					if (line != null) {
						destination.add(line);
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
					destination.add(line);
				}
			}

			stream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getText() {
		return String.join("\n", destination);
	}

	public void stopp() {
		synchronized (lock) {
			running = false;
			lock.notifyAll();
		}
	}
}
