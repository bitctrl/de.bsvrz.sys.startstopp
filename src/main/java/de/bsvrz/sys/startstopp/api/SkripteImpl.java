package de.bsvrz.sys.startstopp.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.bsvrz.sys.startstopp.api.jsonschema.Skripte;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StatusResponse;

public class SkripteImpl implements Skripte {


	@Override
	public GetSkripteCurrentResponse getSkripteCurrent() {
	    StartStoppSkript konfiguration = StartStoppKonfigurationParser.getKonfigurationFrom("../testkonfigurationen/startStopp01_1.xml");
	    
	    ObjectMapper mapper = new ObjectMapper();

	    try {
			String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(konfiguration);
			System.err.println(jsonString);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    
	    return GetSkripteCurrentResponse.respond200WithApplicationJson(konfiguration);
	}

	@Override
	public GetSkripteCurrentStatusResponse getSkripteCurrentStatus() {
		// TODO Auto-generated method stub
		return GetSkripteCurrentStatusResponse.respond200WithApplicationJson(new StatusResponse());
	}
}
