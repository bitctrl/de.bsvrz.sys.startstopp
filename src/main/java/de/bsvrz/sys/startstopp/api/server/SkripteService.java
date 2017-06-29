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

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.NoContentException;
import javax.ws.rs.core.Response;

import de.bsvrz.sys.startstopp.api.jsonschema.Startstoppskript;
import de.bsvrz.sys.startstopp.api.jsonschema.Startstoppskriptstatus;
import de.bsvrz.sys.startstopp.api.jsonschema.Statusresponse;
import de.bsvrz.sys.startstopp.config.SkriptManager;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.startstopp.StartStopp;

@Path("/skripte")
public class SkripteService {

	private SkriptManager skriptManager;

	public SkripteService() {
		this(StartStopp.getInstance().getSkriptManager());
	}

	public SkripteService(SkriptManager skriptManager) {
		this.skriptManager = skriptManager;
	}

	@GET
	@Path("current")
	@Produces("application/json")
	@Inject
	public Response responseSkripteCurrent() throws NoContentException {

		Response response;

		try {
			Startstoppskript konfiguration = skriptManager.getCurrentSkript();

			Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK).header("Content-Type",
					"application/json");
			responseBuilder.entity(konfiguration);
			response = responseBuilder.build();
		} catch (StartStoppException e) {
			Statusresponse statusResponse = new Statusresponse();
			statusResponse.setCode(-1);
			statusResponse.getMessages().add("Die StartStoppApplikation hat kein aktuelles Skript");
			response = Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(statusResponse).build();
		}

		return response;
	}

	@GET
	@Path("current/status")
	@Produces("application/json")
	public Response responseSkripteCurrentStatus() {

		Startstoppskriptstatus status;
		
		try {
			Startstoppskript konfiguration = skriptManager.getCurrentSkript();
			status = skriptManager.checkStatus(konfiguration);

		} catch (StartStoppException e) {
			status = new Startstoppskriptstatus();
			status.setStatus(Startstoppskriptstatus.Status.FAILURE);
			status.getMessages().add(e.getLocalizedMessage());
		}

		Response.ResponseBuilder responseBuilder = Response.ok().header("Content-Type",
				"application/json");
		responseBuilder.entity(status);
		return responseBuilder.build();
	}


	@PUT
	@Path("current")
	@Produces("application/json")
	public Response responseSkripteCurrentPut(Startstoppskript skript) {

		if( skript == null) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		
		Startstoppskriptstatus status = skriptManager.setNewSkript(skript);
		Response.ResponseBuilder responseBuilder = Response.ok().header("Content-Type",
				"application/json");
		responseBuilder.entity(status);
		return responseBuilder.build();
	}

}
