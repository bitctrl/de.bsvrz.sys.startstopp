package de.bsvrz.sys.startstopp.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import de.bsvrz.sys.startstopp.api.jsonschema.StatusResponse;

@Path("/skripte")
public class Dummy {
	  @GET
	  @Path("current")
	  @Produces("application/json")
	  public Response dummy() {
		  System.err.println("Dummy");
	      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
	      responseBuilder.entity(new StatusResponse());
	      return responseBuilder.build();

	  }
}
