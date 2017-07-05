package de.bsvrz.sys.startstopp.config;

import de.bsvrz.sys.startstopp.api.jsonschema.StatusResponse;

public class StartStoppStatusException extends StartStoppException {

	public StartStoppStatusException(String string, StatusResponse statusResponse) {
		super(string);
		if( statusResponse != null) {
			addMessages(statusResponse.getMessages());
		}
	}
}
