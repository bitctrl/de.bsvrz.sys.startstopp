package de.bsvrz.sys.startstopp.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.bsvrz.sys.startstopp.api.jsonschema.Startstoppskript;


@Path("/skripte")
public class SkripteService {
	  @GET
	  @Path("current")
	  @Produces("application/json")
	  public Response responseSkripteCurrent() {
		  
		    ObjectMapper mapper = new ObjectMapper();
		    Startstoppskript konfiguration = StartStoppKonfigurationParser.getKonfigurationFrom("testkonfigurationen/startStopp01_1.xml");

		    try {
				String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(konfiguration);
				System.err.println(jsonString);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
	      responseBuilder.entity(konfiguration);
	      return responseBuilder.build();

	  }
}
