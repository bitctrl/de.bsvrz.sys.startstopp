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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation.Status;

public class AppStopper implements Runnable, StartStoppApplikationListener {

	private static final Debug LOGGER = Debug.getLogger();
	private Map<String, StartStoppApplikation> applikations = new LinkedHashMap<>();
	private Object lock = new Object();
	private boolean waitOnly;

	AppStopper(Collection<StartStoppApplikation> applikations, boolean waitOnly) {
		this.waitOnly = waitOnly;
		for (StartStoppApplikation applikation : applikations) {
			if (applikation.getStatus() != Applikation.Status.GESTOPPT) {
				this.applikations.put(applikation.getInkarnation().getInkarnationsName(), applikation);
				applikation.addManagedApplikationListener(this);
			}
		}
	}

	@Override
	public void run() {
		if (applikations.isEmpty()) {
			return;
		}
		if (!waitOnly) {
			for (StartStoppApplikation applikation : applikations.values()) {
				CompletableFuture.runAsync(
						() -> applikation.updateStatus(Applikation.Status.STOPPENWARTEN, "Skript wird angehalten"));
			}
		}
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				LOGGER.warning(e.getLocalizedMessage());
			}
		}
	}

	@Override
	public void applicationStatusChanged(StartStoppApplikation applikation, Status oldValue, Status newValue) {
		if (newValue != Applikation.Status.STOPPENWARTEN) {
			applikations.remove(applikation.getInkarnation().getInkarnationsName());
			applikation.removeManagedApplikationListener(this);
			if (applikations.isEmpty()) {
				synchronized (lock) {
					lock.notifyAll();
				}
			} 
		}
	}
}