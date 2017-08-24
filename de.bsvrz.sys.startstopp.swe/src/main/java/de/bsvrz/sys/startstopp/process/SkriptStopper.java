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

import java.util.LinkedHashMap;
import java.util.Map;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.process.ProzessManager.StartStoppMode;
import de.bsvrz.sys.startstopp.process.os.OSTools;

class SkriptStopper implements Runnable {

	private static final Debug LOGGER = Debug.getLogger();
	private final Map<String, OnlineApplikation> applikationen = new LinkedHashMap<>();
	private final Map<String, OnlineApplikation> kernsystem = new LinkedHashMap<>();
	private ProzessManager processManager;
	private StartStoppMode modus;

	SkriptStopper(ProzessManager processManager, StartStoppMode modus) {

		this.processManager = processManager;
		this.modus = modus;

		for (OnlineApplikation applikation : processManager.getApplikationen()) {
			if (applikation.isKernsystem()) {
				kernsystem.put(applikation.getName(), applikation);
			} else {
				this.applikationen.put(applikation.getName(), applikation);
			}
		}
	}

	@Override
	public void run() {
		AppStopper appStopper = new AppStopper(applikationen.values(), modus, false);
		appStopper.run();

		System.err.println("Applikationen beendet");
		
		if (OSTools.isWindows()) {
			System.err.println("Suche Transmitter");
			for (OnlineApplikation applikation : kernsystem.values()) {
				if (applikation.isTransmitter()) {
					try {
						processManager.stoppeApplikation(applikation.getName(), StartStoppMode.MANUELL);
					} catch (StartStoppException e) {
						LOGGER.warning(e.getLocalizedMessage());
					}
					break;
				}
			}
			appStopper = new AppStopper(kernsystem.values(), modus, true);
			appStopper.run();
		} else {
			appStopper = new AppStopper(kernsystem.values(), modus, false);
			appStopper.run();
		}
	}
}
