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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.util.NamingThreadFactory;
import de.muspellheim.events.Event;

/**
 * Systemprozess einer Inkarnation.
 * 
 * @author gysi
 */
public class OSApplikation {

	public static final int STARTFEHLER_LAUFZEIT_ERKENNUNG_IN_SEC = 5;

	private static final Debug LOGGER = Debug.getLogger();
	public final Event<OSApplikationStatus> onStatusChange = new Event<>();

	private final String programm;
	private final String inkarnation;
	private String programmArgumente;
	
	private Process process;
	
	private OSApplikationStatus status = OSApplikationStatus.UNDEFINED;
	private int exitCode;

	private List<String> ausgaben = new ArrayList<>();
	private Integer pid;


	public OSApplikation(String inkarnation, String programm) {
		super();
		this.inkarnation = inkarnation;
		this.programm = programm;
	}
	
	/**
	 * fügt der gesicherten Prozessausgabe eine Zeile hinzu
	 * 
	 * @param meldung
	 */
	 void addProzessAusgabe(String meldung) {
		if( ausgaben.isEmpty() && meldung.trim().isEmpty()) {
			return;
		}
		ausgaben.add(meldung);
	}

	/**
	 * liefert den zugewiesenen Inkarnationsname.
	 * 
	 * @return der Name
	 */
	String getInkarnationsName() {
		return inkarnation;
	}

	/**
	 * Gibt den letzten Exit-Code des Prozesses zur&uuml;ck.
	 * 
	 * @return letzter Exit-Code
	 */
	public int getExitCode() {
		return exitCode;
	}

	/**
	 * Gibt die Pid des Prozesses zur&uuml;ck.
	 * 
	 * @return die Pid des Prozesses, <code>null</code> wenn der Prozess nicht
	 *         gefunden werden konnte
	 */
	public Integer getPid() {
		return pid;
	}

	/**
	 * Gibt das auszuf&uuml;rende Programm des Prozesses zur&uuml;ck.
	 * 
	 * @return auszuf&uuml;rendes Programm
	 */
	String getProgramm() {
		return programm;
	}

	/**
	 * Gibt die Programmargumente zur&uuml;ck.
	 * 
	 * @return die Programmargumente
	 */
	String getProgrammArgumente() {
		return programmArgumente;
	}

	/**
	 * Gibt die gespeicherten Standardausgaben und Standardfehlerausgaben des
	 * Prozesses zur&uuml;ck.
	 * 
	 * @return Standardausgaben und Standardfehlerausgaben
	 */

	public List<String> getProzessAusgabe() {
		return Collections.unmodifiableList(ausgaben);
	}

	/**
	 * Gibt den aktuellen Status des Prozesses zur&uuml;ck.
	 * 
	 * @return {@link OSApplikationStatus}
	 */
	public OSApplikationStatus getStatus() {
		return status;
	}

	/**
	 * Beendet den Prozess (harte Variante).
	 * <p>
	 * Der Prozess wird &uuml;ber <code>Process.destroyForcibly</code> beendet.
	 * </p>
	 */
	public void kill() {
		process.destroyForcibly();
	}

	void prozessBeendet(int code) {
		this.exitCode = code;
		setStatus(OSApplikationStatus.GESTOPPT);
	}

	void prozessGestartet() {
		setStatus(OSApplikationStatus.GESTARTET);
	}

	void prozessStartFehler(int code) {
		prozessStartFehler(code, null);
	}
	
	void prozessStartFehler(int code, String message) {
		this.exitCode = code;
		if( message != null) {
			addProzessAusgabe(message);
		}
		setStatus(OSApplikationStatus.STARTFEHLER);
	}

	/**
	 * Setzt die Programmargumente.
	 * 
	 * @param args
	 *            Programmargumente (Kommandozeile)
	 */
	public void setProgrammArgumente(String args) {
		this.programmArgumente = args;

	}

	private void setStatus(OSApplikationStatus status) {
		this.status = status;
		onStatusChange.send(status);
	}

	/**
	 * Startet den Prozess.
	 */
	public void start() {
		if (getProgramm() == null || getProgramm().length() == 0) {
			throw new IllegalStateException("Kein Programm versorgt");
		}

		if (getInkarnationsName() == null || getInkarnationsName().length() == 0) {
			throw new IllegalStateException("Kein Inkarnationsname versorgt");
		}

		Ueberwacher ueberwacher = new Ueberwacher(this);
		Executors.newSingleThreadExecutor(new NamingThreadFactory(getInkarnationsName())).submit(ueberwacher);
	}

	/**
	 * Terminiert den Prozess (weiche Variante).
	 * <p>
	 * Unter Linux wird der Prozess &uuml;ber <code>Process.destroy</code> beendet
	 * (Signal 15).
	 * </p>
	 * <p>
	 * Unter Windows wird ein CTRl-C-EVENT an den Konsolen-Prozess gesendet.
	 * </p>
	 */
	public void terminate() {
		if (OSTools.isWindows() && pid != null) {
			int terminateWindowsProzess = OSTools.terminateWindowsProzess(pid);
			if (terminateWindowsProzess != 0) {
				LOGGER.warning("Fehler beim Terminieren der Inkarnation '" + getInkarnationsName() + "': "
						+ terminateWindowsProzess);
			}
		} else {
			process.destroy();
		}
	}

	/**
	 * gibt an, ob ein Prozess terminiert werden kann oder ob nur kill möglich ist.
	 * 
	 * @return den Status
	 */
	public boolean terminateSupported() {
		// TODO Eventuell unter Java 9 anpassen
		return !OSTools.isWindows();
	}

	void setPid(String pidStr) {
		try {
			this.pid = Integer.parseInt(pidStr);
		} catch (NumberFormatException e) {
			LOGGER.error("PID kann nicht ermittelt werden: " + e.getLocalizedMessage());
		}
	}

	void setProcess(Process process) {
		this.process = process;
	}
}
