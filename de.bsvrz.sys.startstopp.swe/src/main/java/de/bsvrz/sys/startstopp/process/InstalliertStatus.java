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
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.process.OnlineApplikation.TaskType;

public class InstalliertStatus extends OnlineApplikationStatus {

	private static final Debug LOGGER = Debug.getLogger();

	InstalliertStatus(OnlineApplikation applikation) {
		super(Applikation.Status.INSTALLIERT, applikation);
	}

	@Override
	public boolean wechsleStatus(TaskType task, StartStoppStatus.Status startStoppStatus) {

		if (task != TaskType.DEFAULT) {
			return false;
		}

		if (!applikation.isManuellGestartetOderGestoppt()
				&& (startStoppStatus != StartStoppStatus.Status.RUNNING)) {
			return false;
		}

		switch (applikation.getApplikation().getInkarnation().getStartArt().getOption()) {
		case AUTOMATISCH:
		case INTERVALLRELATIV:
		case INTERVALLABSOLUT:
			break;
		case MANUELL:
		default:
			return false;
		}

		String kernSystemMessage = applikation.kernSystemVerfuegbar();
		if (kernSystemMessage != null) {
			LOGGER.info(applikation.getName() + ": " + kernSystemMessage);
			return applikation.updateStatus(Applikation.Status.STARTENWARTEN, kernSystemMessage);
		}

		StartBedingung startBedingung = applikation.getStartBedingung();
		if (startBedingung != null) {
			StartBedingungStatus pruefStatus = applikation.getStartbedingungStatus();
			if (!pruefStatus.isErfuellt()) {
				return applikation.updateStatus(Applikation.Status.STARTENWARTEN, pruefStatus.getMessage());
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
				return applikation.updateStatus(Applikation.Status.STARTENWARTEN,
						applikation.getApplikation().getStartMeldung());
			}
		}

		switch (applikation.getApplikation().getInkarnation().getStartArt().getOption()) {
		case INTERVALLRELATIV:
		case INTERVALLABSOLUT:
			try {
				applikation.getOnlineApplikationTimer().initZyklusTimer();
				return applikation.updateStatus(Applikation.Status.STARTENWARTEN, "Nächster Ausführungszeitpunkt "
						+ DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()
								+ applikation.getOnlineApplikationTimer().getTaskDelay(TimeUnit.MILLISECONDS))));
			} catch (StartStoppException e) {
				LOGGER.warning(e.getLocalizedMessage());
				return false;
			}
		case AUTOMATISCH:
		case MANUELL:
		default:
			break;
		}

		applikation.starteOSApplikation();
		return applikation.updateStatus(Applikation.Status.GESTARTET, "Start initialisiert");
	}

}
