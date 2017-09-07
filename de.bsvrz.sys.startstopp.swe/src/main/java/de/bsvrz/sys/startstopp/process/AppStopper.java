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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.util.NamingThreadFactory;

public class AppStopper implements Runnable {

	class AppStatusHandler implements Consumer<ApplikationEvent> {

		@Override
		public void accept(ApplikationEvent status) {
			if (status.status == Applikation.Status.GESTOPPT) {
				status.event.removeHandler(this);
				synchronized (lock) {
					lock.notifyAll();
				}
			}
		}
	}

	private static final Debug LOGGER = Debug.getLogger();
	private Map<String, OnlineApplikation> applikations = new LinkedHashMap<>();
	private Object lock = new Object();

	private boolean waitOnly;

	AppStopper(Collection<OnlineApplikation> applikations, boolean waitOnly) {
		this.waitOnly = waitOnly;
		for (OnlineApplikation applikation : applikations) {
			if (applikation.getStatus() != Applikation.Status.GESTOPPT) {
				this.applikations.put(applikation.getName(), applikation);
				applikation.onStatusChanged.addHandler(new AppStatusHandler());
			}
		}
	}

	@Override
	public void run() {
		if (allAppsStopped()) {
			return;
		}
		
		
		
		ExecutorService appStopperExecutor = null;
		if (!waitOnly) {
			appStopperExecutor = Executors.newFixedThreadPool(applikations.size(), new NamingThreadFactory("AppStopper"));
			
			for (OnlineApplikation applikation : applikations.values()) {
				// TODO Beenden mit Bedingungen!!
				appStopperExecutor.submit(() -> {
							applikation.requestStopp("Skript wird angehalten", false);
						});
			}
		}

		while (!allAppsStopped()) { 
			synchronized (lock) {
				try {
					lock.wait(1000);
				} catch (InterruptedException e) {
					LOGGER.warning(e.getLocalizedMessage());
				}
			}
		}
		
		if( appStopperExecutor != null) {
			appStopperExecutor.shutdown();
		}
	}

	private boolean allAppsStopped() {
		for (OnlineApplikation applikation : applikations.values()) {
			if (applikation.getStatus() != Applikation.Status.GESTOPPT) {
				return false;
			}
		}
		return true;
	}

}