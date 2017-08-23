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

package de.bsvrz.sys.startstopp.process.os;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import de.bsvrz.sys.startstopp.util.NamingThreadFactory;

/**
 * Klasse zum Starten einer OS-Applikation.
 * 
 * Der Start wird in einem eigenen Thred ausgeführt in dem der Prozesszustand im
 * Betriebssystem ausgewertet wird.
 */
class Ueberwacher implements Runnable {

	/**
	 * 
	 */
	private final OSApplikation osApplikation;

	/**
	 * @param osApplikation
	 */
	Ueberwacher(OSApplikation osApplikation) {
		this.osApplikation = osApplikation;
	}

	private long startTime;
	private long endTime;

	private void bereinigeProzess(int exitCode) {
		OSApplikation.LOGGER.fine("Prozess der Inkarnation '" + this.osApplikation.getInkarnationsName()
				+ "' beendet mit Code: " + exitCode);
		if ((endTime - startTime) < (OSApplikation.STARTFEHLER_LAUFZEIT_ERKENNUNG_IN_SEC * 1000)) {
			OSApplikation.LOGGER.fine("Prozess der Inkarnation '" + this.osApplikation.getInkarnationsName()
					+ "' lief weniger als " + OSApplikation.STARTFEHLER_LAUFZEIT_ERKENNUNG_IN_SEC + "s");
			this.osApplikation.lastExitCode = exitCode;
			this.osApplikation.prozessStartFehler(this.osApplikation.getProzessAusgabe());
		} else {
			this.osApplikation.prozessBeendet(exitCode);
		}
	}

	@Override
	public void run() {
		starteInkarnation();
	}

	/**
	 * Methode zum Starten einer Inkarnation.
	 */
	private void starteInkarnation() {
		OSApplikation.LOGGER.info("Starte Inkarnation '" + this.osApplikation.getInkarnationsName() + "'");
		StringBuilder cmdLineBuilder = new StringBuilder();

		try {
			cmdLineBuilder.append(this.osApplikation.getProgramm());
			if (this.osApplikation.getProgrammArgumente() != null
					&& this.osApplikation.getProgrammArgumente().length() > 0) {
				cmdLineBuilder.append(" ");
				cmdLineBuilder.append(this.osApplikation.getProgrammArgumente());
			}

			String cmdLine = cmdLineBuilder.toString();
			String[] cmdArray = cmdLine.split("\\s");
			OSApplikation.LOGGER.info("Commandline: '" + Arrays.toString(cmdArray) + "'");
			ProcessBuilder builder = new ProcessBuilder(cmdArray);
			builder.redirectErrorStream(true);
			if (!OSTools.isWindows()) {
				// TODO prüfen, ob das tatsächlich notwendig ist
				builder.environment().put("LANG", "de_DE@euro");
			}
			this.osApplikation.process = builder.start();
		} catch (IOException ioe) {
			this.osApplikation.prozessStartFehler(ioe.getMessage());
			return;
		}
		startTime = System.currentTimeMillis();

		if (this.osApplikation.process == null) {
			this.osApplikation.prozessStartFehler("Ursache unklar");
		} else {
			// Umlenken der Standard- und Standardfehlerausgabe
			this.osApplikation.ausgabeUmlenkung = new AusgabeVerarbeitung(this.osApplikation,
					this.osApplikation.process);
			Executors
					.newSingleThreadExecutor(
							new NamingThreadFactory(this.osApplikation.getInkarnationsName() + "_Ausgabeumlenkung"))
					.submit(this.osApplikation.ausgabeUmlenkung);
			this.osApplikation.processInfo = OSTools.findProcess(cmdLineBuilder.toString());
			if (this.osApplikation.processInfo == null) {
				OSApplikation.LOGGER.error("Prozessinfo kann nicht bestimmt werden!");
			} else {
				OSApplikation.LOGGER.fine("Inkarnation '" + this.osApplikation.getInkarnationsName()
						+ "' gestartet (Pid: " + this.osApplikation.processInfo.getPid() + ")");
			}

			this.osApplikation.prozessGestartet();
			ueberwacheProzess();
		}
	}

	private void ueberwacheProzess() {
		OSApplikation.LOGGER
				.finer("Prozess der Inkarnation '" + this.osApplikation.getInkarnationsName() + "' wird überwacht");
		try {
			this.osApplikation.process.waitFor();
			endTime = System.currentTimeMillis();
			CompletableFuture.runAsync(() -> this.osApplikation.ausgabeUmlenkung.anhalten())
					.thenRun(() -> bereinigeProzess(this.osApplikation.process.exitValue()));
		} catch (InterruptedException e) {
			throw new IllegalStateException("Kann nicht passieren", e);
		}
	}
}