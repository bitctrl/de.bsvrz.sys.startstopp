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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see testproc.InkarnationsProzessIf#addProzessListener(testproc.
	 * InkarnationsProzessListener)
	 */
	@Override
	public void addProzessListener(final InkarnationsProzessListener listener) {
		synchronized (prozessListener) {
			prozessListener.add(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see testproc.InkarnationsProzessIf#removeProzessListener(testproc.
	 * InkarnationsProzessListener)
	 */
	@Override
	public void removeProzessListener(final InkarnationsProzessListener listener) {
		synchronized (prozessListener) {
			prozessListener.remove(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see testproc.InkarnationsProzessIf#setLogger(de.bsvrz.sys.funclib.debug.
	 * Debug)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see testproc.InkarnationsProzessIf#getLastExitCode()
	 */
	@Override
	public int getLastExitCode() {
		return lastExitCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see testproc.InkarnationsProzessIf#getStartFehler()
	 */
	@Override
	public String getStartFehler() {
		return startFehler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see testproc.InkarnationsProzessIf#getStatus()
	 */
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
			for (InkarnationsProzessListener l : (new ArrayList<>(prozessListener))) {
				l.statusChanged(status);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see testproc.InkarnationsProzessIf#stopAusgabeUmlenkung()
	 */
	@Override
	public void stopAusgabeUmlenkung() {
		if (ausgabeUmlenkung != null) {
			ausgabeUmlenkung.setRunning(false);
		}
	}

	/**
	 * Interne Klasse zum Starten einer Inkarnation. Der Startvorgang wird als
	 * eigener Thread ausgeführt um zeitliche Abhängigkeiten (wie sleeps) zu
	 * berücksichtigen.
	 */
	private class InkarnationProcessThread extends Thread {

		private static final int STARTFEHLER_LAUFZEIT_ERKENNUNG = 5;

		/**
		 * Konstruktor der Klasse.
		 */
		public InkarnationProcessThread() {
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
			getLogger().fine("Starte Inkarnation '" + getInkarnationsName() + "'");
			StringBuilder cmdLine = new StringBuilder();

			try {
				cmdLine.append(getProgramm());
				if (getProgrammArgumente() != null && getProgrammArgumente().length() > 0) {
					cmdLine.append(" ");
					cmdLine.append(getProgrammArgumente());
				}

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

				processInfo = Tools.findProcess(cmdLine.toString());
				if (processInfo == null) {
					getLogger().error("Prozessinfo kann nicht bestimmt werden!");
				} else {
					getLogger().fine("Inkarnation '" + getInkarnationsName() + "' gestartet (Pid: "
							+ processInfo.getPid() + ")");
				}

				prozessGestartet(process, processInfo);

				ueberwacheProzess(startTime);
			}
		}

		private void ueberwacheProzess(long startTime) {
			getLogger().finer("Prozess der Inkarnation '" + getInkarnationsName() + "' wird überwacht");

			// Hier etwas warten, sonst sind die Startfehler nicht in der
			// Ausgabeumlenkung!
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				int exitCode = process.waitFor();
				stoppAusgabeUmlenkung();
				getLogger()
						.fine("Prozess der Inkarnation '" + getInkarnationsName() + "' beendet mit Code: " + exitCode);
				long endTime = System.currentTimeMillis();

				if ((endTime - startTime) < (STARTFEHLER_LAUFZEIT_ERKENNUNG * 1000)) {
					getLogger().fine("Prozess der Inkarnation '" + getInkarnationsName() + "' lief weniger als "
							+ STARTFEHLER_LAUFZEIT_ERKENNUNG + "s");
					lastExitCode = exitCode;
					prozessStartFehler(getProzessAusgabe());
				} else {
					prozessBeendet(exitCode);
				}

			} catch (InterruptedException e) {
				logger.error("Prozessüberwachung unterbrochen: " + e.getMessage());
			}
		}

		public void stoppAusgabeUmlenkung() {
			ausgabeUmlenkung.setRunning(false);
			ausgabeUmlenkung.interrupt();
		}
	}

	/**
	 * Interne Klasse zum Stoppen einer Inkarnation. Der Stoppvorgang wird als
	 * eigener Thread ausgeführt um zeitliche Abhängigkeiten (wie sleeps) zu
	 * berücksichtigen.
	 */
	private class InkarnationProcessStopperThread extends Thread {

		/**
		 * Konstruktor der Klasse
		 */
		public InkarnationProcessStopperThread() {
			super();
		}

		@Override
		public void run() {
			stoppeInkarnation();
		}

		/**
		 * Methode zum Stoppen einer Inkarnation.
		 */
		private void stoppeInkarnation() {
			getLogger().fine("Stoppe Inkarnation '" + getInkarnationsName() + "'");

			// erster Versuch
			if (process != null && process.isAlive()) {
				process.destroy();
			}

			if (waitStop(2)) {
				return;
			}

			// zweiter Versuch
			if (process != null && process.isAlive()) {
				process.destroyForcibly();
			}

			if (waitStop(2)) {
				return;
			}

			// TODO: 3 Versuch mit kill??

			return;
		}

		private boolean waitStop(int seconds) {
			int i = (seconds * 1000) / 100;
			while (getStatus() != InkarnationsProzessStatus.GESTOPPT && (i-- > 0)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return getStatus() == InkarnationsProzessStatus.GESTOPPT;

		}
	}

	private void prozessGestartet(Process process, ProcessInfo processInfo) {
		setStatus(InkarnationsProzessStatus.GESTARTET);
	}

	private void prozessBeendet(int exitCode) {
		lastExitCode = exitCode;
		setStatus(InkarnationsProzessStatus.GESTOPPT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see testproc.InkarnationsProzessIf#start()
	 */
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
		StringBuffer ausgaben = new StringBuffer();

		ausgaben.append("Prozessausgaben: ");

		Path path = Paths.get(ausgabeUmlenkung.getStdOutFile().toURI());
		try {
			String text = new String(Files.readAllBytes(path), Charset.defaultCharset());
			if (text.length() > 0) {
				ausgaben.append("Standardausgabe: ");
				ausgaben.append(text);
			}
		} catch (IOException e) {
			getLogger().config("Inkarnation '" + getInkarnationsName() + "': " + e.getMessage());
		}

		path = Paths.get(ausgabeUmlenkung.getStdErrFile().toURI());
		try {
			String text = new String(Files.readAllBytes(path), Charset.defaultCharset());
			if (text.length() > 0) {
				ausgaben.append("Fehlerausgabe: ");
				ausgaben.append(text);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ausgaben.toString();
	}

	private void prozessStartFehler(String string) {
		setStatus(InkarnationsProzessStatus.STARTFEHLER);
		startFehler = string;
		System.out.println("Prozess Startfehler: " + string);
	}

	@Override
	public void stopp() {
		if (process == null || (process != null && !process.isAlive())) {
			return;
		}

		InkarnationProcessStopperThread inkarnationProcessThread = new InkarnationProcessStopperThread();
		inkarnationProcessThread.start();
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
}
