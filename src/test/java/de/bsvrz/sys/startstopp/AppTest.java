package de.bsvrz.sys.startstopp;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;

public class AppTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
 
		Client client = ClientBuilder.newClient(new ClientConfig().register(Applikation.class));
		Response response = client.target("http://localhost:9998/applikationen").request(MediaType.APPLICATION_JSON)
				.get(Response.class);
		List<Applikation> readEntity = response.readEntity(new GenericType<List<Applikation>>() {
		});
		
		System.err.println(readEntity);
	}

}
