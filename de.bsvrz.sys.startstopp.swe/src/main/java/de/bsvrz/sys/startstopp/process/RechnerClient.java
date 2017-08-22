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
import java.util.List;
import java.util.Map;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.client.StartStoppClient;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.config.StartStoppException;

class RechnerClient implements Runnable {

	private static final Debug LOGGER = Debug.getLogger();
	private StartStoppClient client;
	private Map<String, Applikation> applikationen = new LinkedHashMap<>();
	private boolean listeErmittelt = false;
	private boolean fehlerGemeldet = false;
	private Rechner rechner;

	RechnerClient(Rechner rechner) {
		this.rechner = rechner;
		client = new StartStoppClient(rechner.getTcpAdresse(), Integer.parseInt(rechner.getPort()));
	}

	@Override
	public void run() {
		try {
			List<Applikation> remoteList = client.getApplikationen();
			synchronized (applikationen) {
				applikationen.clear();
				for (Applikation applikation : remoteList) {
					applikationen.put(applikation.getInkarnation().getInkarnationsName(), applikation);
				}
				listeErmittelt = true;
				fehlerGemeldet = false;
			}
		} catch (StartStoppException e) {
			if (!fehlerGemeldet) {
				LOGGER.fine(rechner.getName() + ": Liste der Applikationen konnte nicht abgerufen werden!",
						e.getLocalizedMessage());
				fehlerGemeldet = true;
			}
			synchronized (applikationen) {
				applikationen.clear();
				listeErmittelt = false;
			}
		}
	}

	boolean checkApplikationsStatus(String inkarnationsName, Applikation.Status status) {
		if (listeErmittelt) {
			Applikation applikation = applikationen.get(inkarnationsName);
			if (applikation != null) {
				return applikation.getStatus() == status;
			}
		}
		return false;
	}

	public Applikation getApplikation(String name) {
		if (listeErmittelt) {
			return applikationen.get(name);
		}
		return null;
	}

	public Rechner getRechner() {
		return rechner;
	}
}
