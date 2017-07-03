package de.bsvrz.sys.startstopp.config;

import de.bsvrz.sys.startstopp.api.jsonschema.Statusresponse;

public class StartStoppStatusException extends StartStoppException {

	public StartStoppStatusException(String string, Statusresponse statusResponse) {
		super(string);
		if( statusResponse != null) {
			addMessages(statusResponse.getMessages());
		}
	}
}
