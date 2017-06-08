package de.bsvrz.sys.startstopp.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/tracks")
public interface Tracks {
  @GET
  @Produces("application/json")
  GetTracksResponse getTracks();

  @PUT
  @Consumes("application/json")
  void putTracks(Track entity);

  class GetTracksResponse extends ResponseDelegate {
    private GetTracksResponse(Response response, Object entity) {
      super(response, entity);
    }

    private GetTracksResponse(Response response) {
      super(response);
    }

    public static GetTracksResponse respond200WithApplicationJson(Track entity) {
      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new GetTracksResponse(responseBuilder.build(), entity);
    }
  }
}
