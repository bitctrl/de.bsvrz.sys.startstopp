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
import java.util.ArrayList;
import java.util.List;

import org.jutils.jprocesses.model.ProcessInfo;

import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Systemprozess einer Inkarnation.
 * 
 * @author gysi
 *
 */
public class InkarnationsProzess implements InkarnationsProzessIf {

	private Debug logger = Debug.getLogger();

	private List<InkarnationsProzessListener> prozessListener = new ArrayList<>();

	@Override
	public void addProzessListener(final InkarnationsProzessListener listener) {
		synchronized (prozessListener) {
			prozessListener.add(listener);
		}
	}

	@Override
	public void removeProzessListener(final InkarnationsProzessListener listener) {
		synchronized (prozessListener) {
			prozessListener.remove(listener);
		}
	}

	@Override
	public void setLogger(Debug logger) {
		this.logger = logger;
	}

	public InkarnationsProzess(Debug logger) {
		super();
		this.logger = logger;
	}

	public InkarnationsProzess() {
	}

	private Debug getLogger() {
		return logger;
	}

	ProcessInfo processInfo = null;

	private Process process;

	private String programm;

	private String inkarnation;

	private InkarnationsProzessStatus status = InkarnationsProzessStatus.GESTOPPT;

	private String startFehler;
	private AusgabeVerarbeitung ausgabeUmlenkung;

	private int lastExitCode;

	private String programmArgumente;

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
		synchronized (prozessListener) {
			for (InkarnationsProzessListener l : new ArrayList<>(prozessListener)) {
				l.statusChanged(status);
			}
		}
	}

	/**
	 * Interne Klasse zum Starten einer Inkarnation. Der Startvorgang wird als
	 * eigener Thread ausgeführt um zeitliche Abhängigkeiten (wie sleeps) zu
	 * berücksichtigen.
	 */
	private class InkarnationProcessThread extends Thread {


		/**
		 * Konstruktor der Klasse.
		 */
		InkarnationProcessThread() {
			super();
		}

		@Override
		public void run() {
			starteInkarnation();
		}

		/**
		 * Methode zum Starten einer Inkarnation.
		 */
		private void starteInkarnation() {
			getLogger().info("Starte Inkarnation '" + getInkarnationsName() + "'");
			StringBuilder cmdLine = new StringBuilder();

			try {
				cmdLine.append(getProgramm());
				if (getProgrammArgumente() != null && getProgrammArgumente().length() > 0) {
					cmdLine.append(" ");
					cmdLine.append(getProgrammArgumente());
				}

				getLogger().info("Commandline: '" + cmdLine.toString() + "'");
				process = ProzessStarter.start(cmdLine.toString());
			} catch (IOException ioe) {
				prozessStartFehler(ioe.getMessage());
				return;
			}
			long startTime = System.currentTimeMillis();

			if (process == null) {
				prozessStartFehler("Ursache unklar");
			} else {
				// Umlenken der Standard- und Standardfehlerausgabe
				ausgabeUmlenkung = new AusgabeVerarbeitung(getInkarnationsName(), process);
				ausgabeUmlenkung.start();
				processInfo = Tools.findProcess(cmdLine.toString());
				if (processInfo == null) {
					getLogger().error("Prozessinfo kann nicht bestimmt werden!");
				} else {
					getLogger().fine("Inkarnation '" + getInkarnationsName() + "' gestartet (Pid: "
							+ processInfo.getPid() + ")");
				}

				prozessGestartet();
				ueberwacheProzess(startTime);
			}
		}

		private void ueberwacheProzess(long startTime) {
			getLogger().finer("Prozess der Inkarnation '" + getInkarnationsName() + "' wird überwacht");

			try {
				int exitCode = process.waitFor();
				ausgabeUmlenkung.stopp();
				ausgabeUmlenkung.join();
				getLogger()
						.fine("Prozess der Inkarnation '" + getInkarnationsName() + "' beendet mit Code: " + exitCode);
				long endTime = System.currentTimeMillis();

				if ((endTime - startTime) < (STARTFEHLER_LAUFZEIT_ERKENNUNG_IN_SEC * 1000)) {
					getLogger().fine("Prozess der Inkarnation '" + getInkarnationsName() + "' lief weniger als "
							+ STARTFEHLER_LAUFZEIT_ERKENNUNG_IN_SEC + "s");
					lastExitCode = exitCode;
					prozessStartFehler(getProzessAusgabe());
				} else {
					prozessBeendet(exitCode);
				}

			} catch (InterruptedException e) {
				logger.error("Prozessüberwachung unterbrochen: " + e.getMessage());
			}
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

		InkarnationProcessThread inkarnationProcessThread = new InkarnationProcessThread();
		inkarnationProcessThread.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see testproc.InkarnationsProzessIf#getProzessAusgabe()
	 */
	@Override
	public String getProzessAusgabe() {
		StringBuilder ausgaben = new StringBuilder(10240);

		ausgaben.append("Prozessausgaben: \n");

		String logMeldungen = ausgabeUmlenkung.getText();
		if (!logMeldungen.isEmpty()) {
			ausgaben.append(logMeldungen);
			ausgaben.append('\n');
		}

		return ausgaben.toString();
	}

	private void prozessStartFehler(String string) {
		setStatus(InkarnationsProzessStatus.STARTFEHLER);
		startFehler = string;
		System.out.println("Prozess Startfehler: " + string);
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
				getLogger().warning("Fehler beim Terminieren der Inkarnation '" + getInkarnationsName() + "': "
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
}
