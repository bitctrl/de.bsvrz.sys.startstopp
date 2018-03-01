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

package de.bsvrz.sys.startstopp.console;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.StartStoppClient;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus;

/**
 * Hilfsklasse zum Beenden einer StartStopp-Applikation von der Kommandozeile.
 * 
 * Die Anwendung verbindet sich mit der gewünschten StartStopp-Applikation und
 * versendet eine Anforderung zum Beenden des StartStopp-Servers. Nach dem
 * Versand prüft die Anwendung zyklisch, ob StartStopp erreichbar bleibt. Ist
 * das nicht mehr der Fall, wird davon ausgegangen, dass StartStopp erfolgreich
 * beendet wurde.
 * 
 * Über folgende Aufrufparameter kann das Verhalten der Anwendung gesteuert
 * werden:
 * 
 * <ul>
 * <li>hostname=&lt;name&gt; bestimmt per Name oder IP-Adresse den Host der
 * StartStopp-Applikation, der Standardwert ist 'localhost'</li>
 * <li>port=&lt;port&gt; bestimmt den Port über den die StartStopp-Applikation
 * erreicht werden kann, der Standardwert ist '3000'</li>
 * <li>timeoutInMinuten=&lt;wert&gt; bestimmt die Zeit, die maximal auf das Ende
 * des StartStopp-Servers gewartet wird in Minuten. Der Standardwert ist '0',
 * d.h. es wird unendlich lange gewartet</li>
 * </ul>
 * 
 * @author BitCtrl Systems GmbH, Uwe Peuker
 */
public class StartStoppStopper {

	private Debug logger;
	private String host;
	private int port;
	private int timeoutInMinuten;

	public static void main(String[] args) {

		StartStoppStopper stopper = new StartStoppStopper(new ArgumentList(args));
		stopper.stopp();
	}

	private StartStoppStopper(ArgumentList argumentList) {

		Debug.init("StartStopp", argumentList);
		logger = Debug.getLogger();

		host = argumentList.fetchArgument("-host=localhost").asString();
		port = Integer.parseInt(argumentList.fetchArgument("-port=3000").asString());
		timeoutInMinuten = Integer.parseInt(argumentList.fetchArgument("-timeoutInMinuten=0").asString());
		logger.info("Beende StartStopp auf Host: " + host + " Port: " + port);
	}

	private void stopp() {

		StartStoppClient client = new StartStoppClient(host, port);
		try {
			client.exitStartStopp();
		} catch (StartStoppException e) {
			logger.error("Anforderung zum Beenden konnte nicht versendet werden: " + e.getLocalizedMessage());
			return;
		}

		// warte bis die Verbindung nicht mehr möglich ist
		boolean run = true;
		LocalDateTime startZeit = LocalDateTime.now();
		while (run) {
			try {
				StartStoppStatus startStoppStatus = client.getStartStoppStatus();
				if (timeoutInMinuten > 0) {
					if (LocalDateTime.now().isAfter(startZeit.plusMinutes(timeoutInMinuten))) {
						run = false;
						logger.warning("StartStopp steht im Status: " + startStoppStatus.getStatus()
								+ " und wurde nach " + timeoutInMinuten
								+ " Minuten immer noch nicht beendet, StoppVorgang wird abgebrochen.");
					}
				}
			} catch (StartStoppException e) {
				logger.fine(e.getLocalizedMessage());
				run = false;
				logger.info("StartStopp-Aplikation ist nicht mehr erreichbar und wird als beendet betrachtet!");
			}
			try {
				TimeUnit.SECONDS.sleep(10);
			} catch (InterruptedException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
	}
}
