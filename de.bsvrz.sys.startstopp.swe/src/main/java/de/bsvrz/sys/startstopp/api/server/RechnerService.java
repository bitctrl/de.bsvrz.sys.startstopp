/*
 * Segment 10 System (Sys), SWE 10.1 StartStopp
 * Copyright (C) 2007-2017 BitCtrl Systems GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Contact Information:<br>
 * BitCtrl Systems GmbH<br>
 * Weißenfelser Straße 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.sys.startstopp.api.server;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.bsvrz.sys.startstopp.api.client.StartStoppClient;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StatusResponse;
import de.bsvrz.sys.startstopp.config.StartStoppKonfiguration;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.startstopp.StartStopp;

@Path("/ststapi/v1/rechner")
public class RechnerService {

	private StartStopp startStopp;

	public RechnerService() {
		this(StartStopp.getInstance());
	}

	public RechnerService(StartStopp startStopp) {
		this.startStopp = startStopp;
	}

	@GET
	@Produces("application/json")
	public Response responseRechner() {

		try {
			StartStoppKonfiguration currentSkript = startStopp.getSkriptManager().getCurrentSkript();
			Collection<Rechner> rechner = currentSkript.getResolvedRechner();
			
			Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK).header("Content-Type",
					"application/json");
			responseBuilder.entity(rechner);
			return responseBuilder.build();
		} catch (StartStoppException e) {
			StatusResponse statusResponse = new StatusResponse();
			statusResponse.setCode(-1);
			statusResponse.getMessages().add(e.getLocalizedMessage());
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(statusResponse).build();
		}
	}

	@GET
	@Path("{rechnername}/skript")
	public Response responseRechnerSkript(@PathParam("rechnername") String rechnerName) {

		try {
			StartStoppKonfiguration currentSkript = startStopp.getSkriptManager().getCurrentSkript();
			Rechner rechner = currentSkript.getResolvedRechner(rechnerName);
			StartStoppClient client = new StartStoppClient(rechner.getTcpAdresse(), Integer.parseInt(rechner.getPort()));
			StartStoppSkript remoteSkript = client.getCurrentSkript();
			return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(remoteSkript).build();
		} catch (StartStoppException e) {
			StatusResponse statusResponse = new StatusResponse();
			statusResponse.setCode(-1);
			statusResponse.getMessages().add(e.getLocalizedMessage());
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).type(MediaType.APPLICATION_JSON_TYPE).entity(statusResponse).build();
		}
	}

	@GET
	@Path("{rechnername}/applikationen")
	public Response responseRechnerApplikationen(@PathParam("rechnername") String rechnerName) {

		try {
			StartStoppKonfiguration currentSkript = startStopp.getSkriptManager().getCurrentSkript();
			Rechner rechner = currentSkript.getResolvedRechner(rechnerName);
			StartStoppClient client = new StartStoppClient(rechner.getTcpAdresse(), Integer.parseInt(rechner.getPort()));
			List<Applikation> applikationen = client.getApplikationen();
			return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(applikationen).build();
		} catch (StartStoppException e) {
			StatusResponse statusResponse = new StatusResponse();
			statusResponse.setCode(-1);
			statusResponse.getMessages().add(e.getLocalizedMessage());
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).type(MediaType.APPLICATION_JSON_TYPE).entity(statusResponse).build();
		}
	}

	@GET
	@Path("{rechnername}/applikationen/{inkarnationsname}")
	public Response responseRechnerApplikation(@PathParam("rechnername") String rechnerName, @PathParam("inkarnationsname") String inkarnationsName) {
		try {
			StartStoppKonfiguration currentSkript = startStopp.getSkriptManager().getCurrentSkript();
			Rechner rechner = currentSkript.getResolvedRechner(rechnerName);
			StartStoppClient client = new StartStoppClient(rechner.getTcpAdresse(), Integer.parseInt(rechner.getPort()));
			Applikation applikation = client.getApplikation(inkarnationsName);
			return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(applikation).build();
		} catch (StartStoppException e) {
			StatusResponse statusResponse = new StatusResponse();
			statusResponse.setCode(-1);
			statusResponse.getMessages().add(e.getLocalizedMessage());
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).type(MediaType.APPLICATION_JSON_TYPE).entity(statusResponse).build();
		}
	}
}
