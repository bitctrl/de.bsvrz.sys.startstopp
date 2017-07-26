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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;

import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Klasse zum Einlesen bzw. Auswerten der Standardausgabe bzw.
 * Standardfehlerausgabe einer Inkarnation. Der Thread wird automatisch durch
 * den Konstruktor der Klasse gestartet.
 */
public class AusgabeVerarbeitung extends Thread {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Verweis auf Inkarnation f&uuml;r die Ausgaben ausgewertet werden
	 */
	private String inkarnation;

	/**
	 * Stream Standardausgabe
	 */
	// private InputStream standardAusgabeStream;

	/**
	 * Stream Standardfehlerausgabe
	 */
	// private InputStream standardFehlerAusgabeStream;

	private Process process;

	private PrintStream stdErrStream;

	private PrintStream stdOutStream;

	private boolean running;

	private File stdErrFile;

	private File stdOutFile;

	private BufferedReader processStdOutput;

	private BufferedReader processStdError;

	/**
	 * Konstruktor der Klasse, startet automatisch den Thread, der die
	 * Eingangsstr&ouml;me verarbeitet.
	 * 
	 * @param inkarnation
	 *            Name der Inkarnation
	 * @param prozess
	 *            Systemprozess der Inkarnation
	 */
	public AusgabeVerarbeitung(final String inkarnation, final Process prozess) {
		this.inkarnation = inkarnation;
		this.process = prozess;
		processStdError = new BufferedReader(new InputStreamReader(process.getErrorStream(), Charset.defaultCharset()));
		processStdOutput = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()));

		openFiles();
		this.start();
		this.setName("Ausgabeverarbeitung Inkarnation '" + inkarnation + "'");
	}

	public void run() {
		running = true;

		// while (process.isAlive() && running) {
		while (running) {
			String input = null;
			String error = null;

			try {
				if (processStdOutput.ready()) {
					input = processStdOutput.readLine();
					// System.out.println("STDOUT: " + input);
				}

				if (processStdError.ready()) {
					error = processStdError.readLine();
					// System.out.println("STDERR: " + error);
				}
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.error("Fehler beim Lesen der Ausgaben der Inkarnation '" + inkarnation + "': " + e.getMessage());
			}

			if (input != null && stdOutStream != null) {
				stdOutStream.println(input);
			}

			if (error != null && stdErrStream != null) {
				stdErrStream.println(error);
			}

			// Nur wenn keine Daten vorliegen wird ein Sleep ausgeführt.

			if (input == null && error == null) {
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					LOGGER.fine("Lesen der Ausgabeströme der Inkarnation '" + inkarnation + "' unterbrochen");
				}
			}

		} // while

		stdErrStream.close();
		stdOutStream.close();

	}


	private String getOutputDir() {
		// TODO: TEMP-Dir oder 'debug'
		return System.getProperty("java.io.tmpdir");

	}

	private void openFiles() {
		String outputDir = getOutputDir();

		stdErrFile = new File(outputDir, inkarnation + ".stderr.log");
		stdOutFile = new File(outputDir, inkarnation + ".stdout.log");

		if (stdErrFile.exists()) {
			stdErrFile.delete();
		}

		stdErrFile.getParentFile().mkdirs();

		try {
			stdErrStream = new PrintStream(new FileOutputStream(stdErrFile));
		} catch (final IOException ex) {
			LOGGER.error("Datei zum Speichern der Ausgaben der Inkarnation '" + inkarnation + "': "
					+ stdErrFile.getAbsolutePath() + " konnte nicht geöffnet werden: " + ex.getMessage());
		}

		if (stdOutFile.exists()) {
			stdOutFile.delete();
		}

		stdOutFile.getParentFile().mkdirs();

		try {
			stdOutStream = new PrintStream(new FileOutputStream(stdOutFile));
		} catch (final IOException ex) {
			LOGGER.error("Datei zum Speichern der Fehler-Ausgaben der Inkarnation '" + inkarnation + "': "
					+ stdErrFile.getAbsolutePath() + " konnte nicht geöffnet werden: " + ex.getMessage());
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public File getStdErrFile() {
		return stdErrFile;
	}

	public File getStdOutFile() {
		return stdOutFile;
	}
}
