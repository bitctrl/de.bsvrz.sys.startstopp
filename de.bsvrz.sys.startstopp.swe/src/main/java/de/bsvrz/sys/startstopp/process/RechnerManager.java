package de.bsvrz.sys.startstopp.process;

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
import de.bsvrz.sys.startstopp.startstopp.StartStopp;

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
