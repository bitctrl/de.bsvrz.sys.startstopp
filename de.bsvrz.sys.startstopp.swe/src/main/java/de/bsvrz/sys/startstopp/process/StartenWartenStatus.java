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

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;
import de.bsvrz.sys.startstopp.process.OnlineApplikation.TaskType;

public class StartenWartenStatus extends OnlineApplikationStatus {

	private static final Debug LOGGER = Debug.getLogger();

	StartenWartenStatus(OnlineApplikation applikation) {
		super(Applikation.Status.STARTENWARTEN, applikation);
	}

	@Override
	public boolean wechsleStatus(TaskType task, StartStoppStatus.Status startStoppStatus) {

		if (task == TaskType.STOPPFEHLER) {
			return false;
		}

		if (!applikation.isManuellGestartetOderGestoppt() && startStoppStatus != StartStoppStatus.Status.RUNNING
				&& startStoppStatus != StartStoppStatus.Status.INITIALIZED) {
			applikation.getOnlineApplikationTimer().clear();
			return applikation.updateStatus(Applikation.Status.STARTENWARTEN, "");
		}

		if (applikation.isManuellGestartetOderGestoppt()) {
			if (!applikation.getOnlineApplikationTimer().isIntervallTaskAktiv()) {
				applikation.getOnlineApplikationTimer().clear();
			}
		} else {
			String kernSystemMessage = applikation.kernSystemVerfuegbar();
			if (kernSystemMessage != null) {
				LOGGER.info(applikation.getName() + ": " + kernSystemMessage);
				return applikation.updateStatus(Applikation.Status.STARTENWARTEN, kernSystemMessage);
			}

			StartBedingung startBedingung = applikation.getStartBedingung();
			if (startBedingung != null) {
				StartBedingungStatus pruefer = applikation.getStartbedingungStatus();
				if (!pruefer.isErfuellt()) {
					applikation.getOnlineApplikationTimer().clear();
					return applikation.updateStatus(Applikation.Status.STARTENWARTEN, pruefer.getMessage());
				}

				if (task != TaskType.WARTETIMER) {
					if (applikation.getOnlineApplikationTimer().isWarteTaskAktiv()) {
						return applikation.updateStatus(Applikation.Status.STARTENWARTEN,
								applikation.getApplikation().getStartMeldung());
					}
					long warteZeitInMsec;
					try {
						warteZeitInMsec = Util.convertToWarteZeitInMsec(startBedingung.getWartezeit());
					} catch (StartStoppException e) {
						throw new IllegalStateException(
								"Sollte hier nicht passieren, weil nur geprüfte Skripte ausgeführt werden!", e);
					}
					if (warteZeitInMsec > 0) {
						applikation.getOnlineApplikationTimer().initWarteTask(warteZeitInMsec);
						return applikation.updateStatus(Applikation.Status.STARTENWARTEN, "Wartezeit bis " + DateFormat
								.getDateTimeInstance().format(new Date(System.currentTimeMillis() + warteZeitInMsec)));
					}
				}
			}

			if (applikation.getOnlineApplikationTimer().isWarteTaskAktiv()) {
				return false;
			}
		}

		if (applikation.getOnlineApplikationTimer().isIntervallTaskAktiv()) {
			if (task != TaskType.INTERVALLTIMER) {
				return false;
			}
		}

		if (task != TaskType.INTERVALLTIMER) {
			switch (applikation.getStartArtOption()) {
			case INTERVALLRELATIV:
			case INTERVALLABSOLUT:
				try {
					applikation.getOnlineApplikationTimer().initZyklusTimer();
					return applikation.updateStatus(Applikation.Status.STARTENWARTEN, "Nächster Ausführungszeitpunkt "
							+ DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()
									+ applikation.getOnlineApplikationTimer().getTaskDelay(TimeUnit.MILLISECONDS))));
				} catch (StartStoppException e) {
					LOGGER.warning("Zyklische Ausführung einer Applikation konnte nicht eingeplant werden: " + e.getLocalizedMessage());
					return false;
				}
			case AUTOMATISCH:
			case MANUELL:
			default:
				break;
			}
		}

		try {
			applikation.starteOSApplikation();
			return applikation.updateStatus(Applikation.Status.GESTARTET, "");
		} catch (StartStoppException e) {
			return applikation.updateStatus(Applikation.Status.GESTOPPT, e.getLocalizedMessage());
		}
	}

}
