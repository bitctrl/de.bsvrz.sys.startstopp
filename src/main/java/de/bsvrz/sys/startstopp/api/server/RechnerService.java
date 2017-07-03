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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import de.bsvrz.sys.startstopp.api.ManagedSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.api.jsonschema.Statusresponse;
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
			ManagedSkript currentSkript = startStopp.getSkriptManager().getCurrentSkript();
			List<Rechner> rechner = currentSkript.getSkript().getGlobal().getRechner();
			
			Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK).header("Content-Type",
					"application/json");
			responseBuilder.entity(rechner);
			return responseBuilder.build();
		} catch (StartStoppException e) {
			Statusresponse statusResponse = new Statusresponse();
			statusResponse.setCode(-1);
			statusResponse.getMessages().add(e.getLocalizedMessage());
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(statusResponse).build();
		}
	}

	@GET
	@Path("{rechnername}/skript")
	public Response responseRechnerSkript(@PathParam("rechnername") String rechnerName) {
		Response response = Response.status(Response.Status.NOT_IMPLEMENTED).build();
		// TODO Skript von Remoterechner ermitteln
		return response;
	}

	@GET
	@Path("{rechnername}/applikationen")
	public Response responseRechnerApplikationen(@PathParam("rechnername") String rechnerName) {
		Response response = Response.status(Response.Status.NOT_IMPLEMENTED).build();
		// TODO Applikationen von Remoterechner ermitteln
		return response;
	}

	@GET
	@Path("{rechnername}/applikationen/{inkarnationsname}")
	public Response responseRechnerApplikation(@PathParam("rechnername") String rechnerName, @PathParam("inkarnationsname") String inkarnationsName) {
		Response response = Response.status(Response.Status.NOT_IMPLEMENTED).build();
		// TODO Applikation von Remoterechner ermitteln
		return response;
	}
}
