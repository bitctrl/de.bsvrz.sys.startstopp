package de.bsvrz.sys.startstopp.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppKonfiguration;
import de.bsvrz.sys.startstopp.api.jsonschema.Startstoppkonfigurationen;

public class StartstoppkonfigurationenImpl implements Startstoppkonfigurationen {

	@Override
	public GetStartstoppkonfigurationenResponse getStartstoppkonfigurationen() {
	    StartStoppKonfiguration konfiguration = StartStoppKonfigurationParser.getKonfigurationFrom("../testkonfigurationen/startStopp01_1.xml");
	    
	    ObjectMapper mapper = new ObjectMapper();

	    try {
			String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(konfiguration);
			System.err.println(jsonString);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    
	    return GetStartstoppkonfigurationenResponse.respond200WithApplicationJson(konfiguration);
	}

}
