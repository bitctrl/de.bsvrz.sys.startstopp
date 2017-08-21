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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jutils.jprocesses.model.ProcessInfo;

import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Systemprozess einer Inkarnation.
 * 
 * @author gysi
 *
 */
public class InkarnationsProzess implements InkarnationsProzessIf {

	private static final Debug LOGGER = Debug.getLogger();

	private final List<InkarnationsProzessListener> prozessListeners = new CopyOnWriteArrayList<>();

	@Override
	public void addProzessListener(final InkarnationsProzessListener listener) {
		prozessListeners.add(listener);
	}

	@Override
	public void removeProzessListener(final InkarnationsProzessListener listener) {
		prozessListeners.remove(listener);
	}

//	private ExecutorService executor = Executors.newFixedThreadPool(2);
	private ProcessInfo processInfo = null;
	private Process process;
	private String programm;
	private String inkarnation;
	private InkarnationsProzessStatus status = InkarnationsProzessStatus.UNDEFINED;
	private String startFehler;
	private AusgabeVerarbeitung ausgabeUmlenkung;
	private int lastExitCode;
	private String programmArgumente;
	private StringBuilder ausgaben = new StringBuilder(1024);

	@Override
	public int getLastExitCode() {
		return lastExitCode;
	}

	@Override
	public String getStartFehler() {
		return startFehler;
	}

	@Override
	public InkarnationsProzessStatus getStatus() {
		return status;
	}

	private void setStatus(InkarnationsProzessStatus status) {
		this.status = status;
		notifyListener();
	}

	private void notifyListener() {
		prozessListeners.forEach(l -> l.statusChanged(status));
	}

	/**
	 * Interne Klasse zum Starten einer Inkarnation. Der Startvorgang wird als
	 * eigener Thread ausgeführt um zeitliche Abhängigkeiten (wie sleeps) zu
	 * berücksichtigen.
	 */
	private class UeberwachungsRunnable implements Runnable {

		private long startTime;
		private long endTime;

		@Override
		public void run() {
			starteInkarnation();
		}

		/**
		 * Methode zum Starten einer Inkarnation.
		 */
		private void starteInkarnation() {
			LOGGER.info("Starte Inkarnation '" + getInkarnationsName() + "'");
			StringBuilder cmdLineBuilder = new StringBuilder();

			try {
				cmdLineBuilder.append(getProgramm());
				if (getProgrammArgumente() != null && getProgrammArgumente().length() > 0) {
					cmdLineBuilder.append(" ");
					cmdLineBuilder.append(getProgrammArgumente());
				}

				String cmdLine = cmdLineBuilder.toString();
				String[] cmdArray = cmdLine.split("\\s");
				LOGGER.info("Commandline: '" + Arrays.toString(cmdArray) + "'");
				ProcessBuilder builder = new ProcessBuilder(cmdArray);
				builder.redirectErrorStream(true);
				if (!Tools.isWindows()) {
					// TODO prüfen, ob das tatsächlich notwendig ist
					builder.environment().put("LANG", "de_DE@euro");
				}
				process = builder.start();
			} catch (IOException ioe) {
				prozessStartFehler(ioe.getMessage());
				return;
			}
			startTime = System.currentTimeMillis();

			if (process == null) {
				prozessStartFehler("Ursache unklar");
			} else {
				// Umlenken der Standard- und Standardfehlerausgabe
				ausgabeUmlenkung = new AusgabeVerarbeitung(InkarnationsProzess.this, process);
//				executor.execute(ausgabeUmlenkung);
				Executors.newSingleThreadExecutor(new NamingThreadFactory(getInkarnationsName() + "_Ausgabeumlenkung")).submit(ausgabeUmlenkung);
				processInfo = Tools.findProcess(cmdLineBuilder.toString());
				if (processInfo == null) {
					LOGGER.error("Prozessinfo kann nicht bestimmt werden!");
				} else {
					LOGGER.fine("Inkarnation '" + getInkarnationsName() + "' gestartet (Pid: " + processInfo.getPid()
							+ ")");
				}

				prozessGestartet();
				ueberwacheProzess();
			}
		}

		private void ueberwacheProzess() {
			LOGGER.finer("Prozess der Inkarnation '" + getInkarnationsName() + "' wird überwacht");
			try {
				process.waitFor();
				endTime = System.currentTimeMillis();
				CompletableFuture.runAsync(() -> ausgabeUmlenkung.anhalten())
						.thenRun(() -> bereinigeProzess(process.exitValue()));
			} catch (InterruptedException e) {
				throw new IllegalStateException("Kann nicht passieren", e);
			}
		}

		private void bereinigeProzess(int exitCode) {
			LOGGER.fine("Prozess der Inkarnation '" + getInkarnationsName() + "' beendet mit Code: " + exitCode);
			if ((endTime - startTime) < (STARTFEHLER_LAUFZEIT_ERKENNUNG_IN_SEC * 1000)) {
				LOGGER.fine("Prozess der Inkarnation '" + getInkarnationsName() + "' lief weniger als "
						+ STARTFEHLER_LAUFZEIT_ERKENNUNG_IN_SEC + "s");
				lastExitCode = exitCode;
				prozessStartFehler(getProzessAusgabe());
			} else {
				prozessBeendet(exitCode);
			}
//			executor.shutdown();
		}
	}

	private void prozessGestartet() {
		setStatus(InkarnationsProzessStatus.GESTARTET);
	}

	private void prozessBeendet(int exitCode) {
		lastExitCode = exitCode;
		setStatus(InkarnationsProzessStatus.GESTOPPT);
	}

	@Override
	public void start() {
		if (getProgramm() == null || getProgramm().length() == 0) {
			throw new IllegalStateException("Kein Programm versorgt");
		}

		if (getInkarnationsName() == null || getInkarnationsName().length() == 0) {
			throw new IllegalStateException("Kein Inkarnationsname versorgt");
		}

		UeberwachungsRunnable processThread = new UeberwachungsRunnable();
		Executors.newSingleThreadExecutor(new NamingThreadFactory(getInkarnationsName())).submit(processThread);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see testproc.InkarnationsProzessIf#getProzessAusgabe()
	 */
	@Override
	public String getProzessAusgabe() {
		return ausgaben.toString();
	}

	private void prozessStartFehler(String string) {
		setStatus(InkarnationsProzessStatus.STARTFEHLER);
		startFehler = string;
	}

	@Override
	public String getProgramm() {
		return programm;
	}

	@Override
	public void setProgramm(String command) {
		this.programm = command;
	}

	@Override
	public String getProgrammArgumente() {
		return programmArgumente;
	}

	@Override
	public void setProgrammArgumente(String args) {
		this.programmArgumente = args;

	}

	@Override
	public String getInkarnationsName() {
		return inkarnation;
	}

	@Override
	public void setInkarnationsName(String command) {
		inkarnation = command;
	}

	@Override
	public Integer getPid() {
		if (processInfo != null) {
			return Integer.parseInt(processInfo.getPid());
		}
		return null;
	}

	@Override
	public void terminate() {
		if (Tools.isWindows()) {
			int terminateWindowsProzess = Tools.terminateWindowsProzess(getPid());
			if (terminateWindowsProzess != 0) {
				LOGGER.warning("Fehler beim Terminieren der Inkarnation '" + getInkarnationsName() + "': "
						+ terminateWindowsProzess);
			}
		} else {
			process.destroy();
		}
	}

	@Override
	public void kill() {
		process.destroyForcibly();
	}

	@Override
	public boolean terminateSupported() {
		// TODO Eventuell unter Java 9 anpassen
		return !Tools.isWindows();
	}

	@Override
	public void addProzessAusgabe(String meldung) {
		if (ausgaben.length() > 0) {
			ausgaben.append('\n');
		}
		ausgaben.append(meldung);
	}
}
