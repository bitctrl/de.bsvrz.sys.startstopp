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

import org.jutils.jprocesses.model.ProcessInfo;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.util.NamingThreadFactory;

/**
 * Klasse zum Starten einer OS-Applikation.
 * 
 * Der Start wird in einem eigenen Thred ausgeführt in dem der Prozesszustand im
 * Betriebssystem ausgewertet wird.
 */
class Ueberwacher implements Runnable {

	private static final Debug LOGGER = Debug.getLogger();
	private final OSApplikation osApplikation;
	private AusgabeVerarbeitung ausgabeUmlenkung;

	private long startTime;

	private long endTime;

	/**
	 * @param osApplikation
	 */
	Ueberwacher(OSApplikation osApplikation) {
		this.osApplikation = osApplikation;
	}

	private void bereinigeProzess(int exitCode) {
		LOGGER.fine(
				"Prozess der Inkarnation '" + osApplikation.getInkarnationsName() + "' beendet mit Code: " + exitCode);
		if ((endTime - startTime) < (OSApplikation.STARTFEHLER_LAUFZEIT_ERKENNUNG_IN_SEC * 1000)) {
			LOGGER.fine("Prozess der Inkarnation '" + osApplikation.getInkarnationsName() + "' lief weniger als "
					+ OSApplikation.STARTFEHLER_LAUFZEIT_ERKENNUNG_IN_SEC + "s");
			osApplikation.prozessStartFehler(exitCode);
		} else {
			osApplikation.prozessBeendet(exitCode);
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
		LOGGER.info("Starte Inkarnation '" + osApplikation.getInkarnationsName() + "'");
		StringBuilder cmdLineBuilder = new StringBuilder();

		Process process;
		try {
			cmdLineBuilder.append(osApplikation.getProgramm());
			if (osApplikation.getProgrammArgumente() != null && osApplikation.getProgrammArgumente().length() > 0) {
				cmdLineBuilder.append(" ");
				cmdLineBuilder.append(osApplikation.getProgrammArgumente());
			}

			String cmdLine = cmdLineBuilder.toString();
			String[] cmdArray = cmdLine.split("\\s");
			LOGGER.info("Commandline: '" + Arrays.toString(cmdArray) + "'");
			ProcessBuilder builder = new ProcessBuilder(cmdArray);
			builder.redirectErrorStream(true);
			if (!OSTools.isWindows()) {
				// TODO prüfen, ob das tatsächlich notwendig ist
				builder.environment().put("LANG", "de_DE@euro");
			}
			process = builder.start();
		} catch (IOException ioe) {
			osApplikation.prozessStartFehler(-1, ioe.getLocalizedMessage());
			return;
		}

		startTime = System.currentTimeMillis();
		if (process == null) {
			osApplikation.prozessStartFehler(-1, "Ursache unklar");
		} else {
			osApplikation.setProcess(process);
			// Umlenken der Standard- und Standardfehlerausgabe
			ausgabeUmlenkung = new AusgabeVerarbeitung(osApplikation, process);
			Executors
					.newSingleThreadExecutor(
							new NamingThreadFactory(osApplikation.getInkarnationsName() + "_Ausgabeumlenkung"))
					.submit(ausgabeUmlenkung);
			ProcessInfo processInfo = OSTools.findProcess(cmdLineBuilder.toString());
			if (processInfo == null) {
				LOGGER.error("Prozessinfo kann nicht bestimmt werden!");
			} else {
				osApplikation.setPid(processInfo.getPid());
			}

			osApplikation.prozessGestartet();
			ueberwacheProzess(process);
		}
	}

	private void ueberwacheProzess(Process process) {
		LOGGER.finer("Prozess der Inkarnation '" + osApplikation.getInkarnationsName() + "' wird überwacht");
		try {
			process.waitFor();
			endTime = System.currentTimeMillis();
//			CompletableFuture.runAsync(() -> ausgabeUmlenkung.anhalten(), Executors.newSingleThreadExecutor(new NamingThreadFactory(osApplikation.getInkarnationsName() + "_Stopp-Ausgabeumlenkung")))
//					.thenRun(() -> bereinigeProzess(process.exitValue()));
			ausgabeUmlenkung.anhalten();
			bereinigeProzess(process.exitValue());
		} catch (InterruptedException e) {
			throw new IllegalStateException("Kann nicht passieren", e);
		}
	}
}