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

package de.bsvrz.sys.startstopp.process.remote;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.client.StartStoppClient;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.muspellheim.events.Action;

public class RechnerClient implements Runnable {

	private static final Debug LOGGER = Debug.getLogger();
	private StartStoppClient client;
	private Map<String, Applikation> applikationen = new LinkedHashMap<>();
	private boolean listeErmittelt;
	private long fehlerGemeldet;
	private Rechner rechner;

	final Action onRechnerAktualisiert = new Action();

	RechnerClient(Rechner rechner) {
		this.rechner = rechner;
		client = new StartStoppClient(rechner.getTcpAdresse(), Integer.parseInt(rechner.getPort()));
	}

	@Override
	public void run() {

		boolean changed = false;

		try {
			List<Applikation> remoteList = client.getApplikationen();
			synchronized (applikationen) {
				Set<String> remoteNames = new LinkedHashSet<>();
				for (Applikation applikation : remoteList) {
					remoteNames.add(applikation.getInkarnation().getInkarnationsName());
					Applikation oldApplikation = applikationen.put(applikation.getInkarnation().getInkarnationsName(),
							applikation);
					if (oldApplikation == null) {
						changed = true;
					} else {
						changed = changed || (oldApplikation.getStatus() != applikation.getStatus());
					}
				}
				
				Iterator<String> iterator = applikationen.keySet().iterator();
				while( iterator.hasNext()) {
					String next = iterator.next();
					if( !remoteNames.contains(next)) {
						iterator.remove();
						changed = true;
					}
				}
				
				listeErmittelt = true;
			}
		} catch (StartStoppException e) {
			if (System.currentTimeMillis() - fehlerGemeldet > 60000) {
				LOGGER.warning(rechner.getName() + ": Liste der Applikationen konnte von \"" + rechner.getTcpAdresse() + ":"
						+ rechner.getPort() + "\" nicht abgerufen werden!", e.getLocalizedMessage());
				fehlerGemeldet = System.currentTimeMillis();
			}
			synchronized (applikationen) {
				changed = changed || !applikationen.isEmpty();
				applikationen.clear();
				listeErmittelt = false;
			}
		}
		
		if( changed ) {
			onRechnerAktualisiert.trigger();
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
