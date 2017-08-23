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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;

public class RechnerManager {

	private static class ManagedRechner {
		private RechnerClient client;
		private ScheduledFuture<?> future;
	}

	private ScheduledThreadPoolExecutor rechnerExecutor = new ScheduledThreadPoolExecutor(5);
	private Map<String, ManagedRechner> managedRechner = new LinkedHashMap<>();

	public RechnerManager() {
		rechnerExecutor.setRemoveOnCancelPolicy(true);
	}

	public void reconnect(Collection<Rechner> rechnerListe) {

		Set<String> names = new LinkedHashSet<>();
		for (Rechner rechnerEintrag : rechnerListe) {
			String name = rechnerEintrag.getName();
			names.add(name);
			ManagedRechner managed = managedRechner.get(name);
			if ((managed == null) || !Objects.equals(rechnerEintrag, managed.client.getRechner())) {
				if (managed != null) {
					managed.future.cancel(true);
				}
				addRechnerClient(rechnerEintrag, name);
			}
		}
		
		Iterator<Entry<String, ManagedRechner>> iterator = managedRechner.entrySet().iterator();
		while( iterator.hasNext()) {
			Entry<String, ManagedRechner> entry = iterator.next();
			if( !names.contains(entry.getKey())) {
				entry.getValue().future.cancel(true);
				iterator.remove();
			}
		}
	}

	private void addRechnerClient(Rechner rechnerEintrag, String name) {
		ManagedRechner managed;
		managed = new ManagedRechner();
		managed.client = new RechnerClient(rechnerEintrag);
		managed.future = rechnerExecutor.scheduleAtFixedRate(managed.client, 0, 30, TimeUnit.SECONDS);
		managedRechner.put(name, managed);
	}

	public RechnerClient getClient(String rechnerName) {
		ManagedRechner managed = managedRechner.get(rechnerName);
		if( managed == null) {
			return null;
		}
		
		return managed.client;
	}
}
