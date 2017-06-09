package de.bsvrz.sys.startstopp.api.jsonschema;

import de.bsvrz.sys.startstop.api.jsonschema.StartStoppKonfiguration;
import de.bsvrz.sys.startstop.api.jsonschema.Startstoppkonfigurationen;

public class StartstoppkonfigurationenImpl implements Startstoppkonfigurationen {

	@Override
	public GetStartstoppkonfigurationenResponse getStartstoppkonfigurationen() {
	    StartStoppKonfiguration track = new StartStoppKonfiguration();
	    return GetStartstoppkonfigurationenResponse.respond200WithApplicationJson(track);
	}

}
