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

import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Interface Systemprozess einer Inkarnation.
 * 
 * @author BitCtrl Systems GmbH, Gieseler
 * @version $Id: $
 *
 */
public interface InkarnationsProzessIf {

	static final int STARTFEHLER_LAUFZEIT_ERKENNUNG_IN_SEC = 5;

	/**
	 * F&uuml;gt einen {@link InkarnationsProzessListener} hinzu.
	 * 
	 * @param listener
	 *            {@link InkarnationsProzessListener}
	 */
	void addProzessListener(InkarnationsProzessListener listener);

	/**
	 * Entfernt einen {@link InkarnationsProzessListener}.
	 * 
	 * @param listener
	 *            {@link InkarnationsProzessListener}
	 */
	void removeProzessListener(InkarnationsProzessListener listener);

	/**
	 * Setzt den Logger.
	 * 
	 * @param logger
	 *            {@link Debug}
	 */
	void setLogger(Debug logger);

	/**
	 * Gibt den letzten Exit-Code des Prozesses zur&uuml;ck.
	 * 
	 * @return letzter Exit-Code
	 */
	int getLastExitCode();

	/**
	 * Gibt Informationen zum Startfehler des Prozesses zur&uuml;ck.
	 * 
	 * @return Startfehler-Informationen
	 */
	String getStartFehler();

	/**
	 * Gibt den aktuellen Status des Prozesses zur&uuml;ck.
	 * 
	 * @return {@link InkarnationsProzessStatus}
	 */
	InkarnationsProzessStatus getStatus();

	/**
	 * Startet den Prozess.
	 */
	void start();

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
	void terminate();

	/**
	 * Beendet den Prozess (harte Variante).
	 * <p>
	 * Der Prozess wird &uuml;ber <code>Process.destroyForcibly</code> beendet.
	 * </p>
	 */
	void kill();

	/**
	 * Gibt die gespeicherten Standardausgaben und Standardfehlerausgaben des
	 * Prozesses zur&uuml;ck.
	 * 
	 * @return Standardausgaben und Standardfehlerausgaben
	 */
	String getProzessAusgabe();

	/**
	 * liefert den zugewiesenen Inkarnationsname.
	 * 
	 * @return der Name
	 */
	String getInkarnationsName();

	/**
	 * Setzt den Inkarnationsnamen des Prozesses.
	 * 
	 * @param command
	 *            der Name
	 */
	void setInkarnationsName(String command);

	/**
	 * Gibt das auszuf&uuml;rende Programm des Prozesses zur&uuml;ck.
	 * 
	 * @return auszuf&uuml;rendes Programm
	 */
	String getProgramm();

	/**
	 * Setzt das auszuf&uuml;rende Programm des Prozesses.
	 * 
	 * @param command
	 *            auszuf&uuml;rendes Programm
	 */
	void setProgramm(String command);

	/**
	 * Gibt die Programmargumente zur&uuml;ck.
	 * 
	 * @return die Programmargumente
	 */
	String getProgrammArgumente();

	/**
	 * Setzt die Programmargumente.
	 * 
	 * @param args
	 *            Programmargumente (Kommandozeile)
	 */
	void setProgrammArgumente(String args);

	/**
	 * Gibt die Pid des Prozesses zur&uuml;ck.
	 * 
	 * @return die Pid des Prozesses, <code>null</code> wenn der Prozess nicht
	 *         gefunden werden konnte
	 */
	Integer getPid();

	/**
	 * gibt an, ob ein Prozess terminiert werden kann oder ob nur kill möglich ist.
	 * 
	 * @return den Status
	 */
	boolean terminateSupported();
}
