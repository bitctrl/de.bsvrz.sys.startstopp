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

	/**
	 * F&uuml;gt einen {@link InkarnationsProzessListener} hinzu.
	 * 
	 * @param listener {@link InkarnationsProzessListener}
	 */
	void addProzessListener(InkarnationsProzessListener listener);

	/**
	 * Entfernt einen {@link InkarnationsProzessListener}.
	 * 
	 * @param listener {@link InkarnationsProzessListener}
	 */
	void removeProzessListener(InkarnationsProzessListener listener);

	/**
	 * Setzt den Logger.
	 * 
	 * @param logger {@link Debug}
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
	 * Stoppt die Protokollierung der Ausgabeumlenkung des Prozesses.
	 * <p>Der Prozess wird immer mit aktiver Umlenkung der Standardausgabe 
	 * und Standardfehlerausgabe gestartet, damit Startfehler analysiert werden k&ouml;nnen.
	 * </p>
	 * <p>Nach einem erfolgreichen Start sollte die Ausgabeumlenkung beendet werden, 
	 * da diese eine nicht zu vernachl&auml;ssigende Last erzeugt und damit das 
	 * Zeitverhalten des Prozesses beeinflussen kann. 
	 * </p>
	 */
	void stopAusgabeUmlenkung();

	/**
	 * Startet den Prozess.
	 */
	void start();

	/**
	 * Stoppt den Prozess.
	 */
	void stopp();
	
	/**
	 * Gibt die gespeicherten Standardausgaben und Standardfehlerausgaben des Prozesses zur&uuml;ck.
	 * 
	 * @return Standardausgaben und Standardfehlerausgaben
	 */
	String getProzessAusgabe();

	/**
	 * Gibt das auszuf&uuml;rende Programm des Prozesses zur&uuml;ck.
	 * 
	 * @return auszuf&uuml;rendes Programm 
	 */
	String getProgramm();

	/**
	 * Setzt das auszuf&uuml;rende Programm des Prozesses.
	 * 
	 * @param command auszuf&uuml;rendes Programm 
	 */
	void setProgramm(String command);

	/**
	 * Gibt die Programmargumente zur&uuml;ck.
	 * 
	 * @return  die Programmargumente
	 */
	String getProgrammArgumente();

	/**
	 * Setzt die Programmargumente.
	 * 
	 * @param args Programmargumente (Kommandozeile) 
	 */
	void setProgrammArgumente(String args);
}
