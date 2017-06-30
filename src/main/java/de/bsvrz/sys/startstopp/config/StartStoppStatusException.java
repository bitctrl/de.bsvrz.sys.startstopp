package de.bsvrz.sys.startstopp.config;

import java.util.ArrayList;
import java.util.List;

import de.bsvrz.sys.startstopp.api.jsonschema.Statusresponse;

public class StartStoppStatusException extends StartStoppException {

	private List<String> messages = new ArrayList<>();
	
	public List<String> getMessages() {
		return messages;
	}

	public StartStoppStatusException(String string, Statusresponse statusResponse) {
		super(string);
		if( statusResponse != null) {
			messages.addAll(statusResponse.getMessages());
		}
	}
}
