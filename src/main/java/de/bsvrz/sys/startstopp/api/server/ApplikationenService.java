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

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Statusresponse;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.process.ProcessManager;
import de.bsvrz.sys.startstopp.startstopp.StartStopp;

@Path("/applikationen")
public class ApplikationenService {

	private ProcessManager processManager;

	public ApplikationenService() {
		this(StartStopp.getInstance().getProcessManager());
	}

	public ApplikationenService(ProcessManager processManager) {
		this.processManager = processManager;
	}

	@GET
	@Produces("application/json")
	public Response responseApplikationen() {

		List<Applikation> applikationen = processManager.getApplikationen();
		
		Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK).header("Content-Type",
				"application/json");
		responseBuilder.entity(applikationen);
		return responseBuilder.build();
	}

	@GET
	@Path("{inkarnationsName}")
	@Produces("application/json")
	public Response responseApplikation(@PathParam("inkarnationsName") String inkarnationsName) {

		try {
			Applikation applikation = processManager.getApplikation(inkarnationsName);

			Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK).header("Content-Type",
					"application/json");
			responseBuilder.entity(applikation);
			return responseBuilder.build();
		} catch (StartStoppException e) {
			// TODO Auto-generated catch block
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

	@POST
	@Path("{inkarnationsName}/start")
	@Produces("application/json")
	public Response responseApplikationStart(@PathParam("inkarnationsName") String inkarnationsName) {

		try {
			Applikation applikation = processManager.starteApplikation(inkarnationsName);
			Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK).header("Content-Type",
					"application/json");
			responseBuilder.entity(applikation);
			return responseBuilder.build();
		} catch (StartStoppException e) {
			Statusresponse status = new Statusresponse();
			status.setCode(-1);
			status.getMessages().add(e.getLocalizedMessage());
			return Response.status(Response.Status.CONFLICT).entity(status).build();
		}
	}

	@POST
	@Path("{inkarnationsName}/restart")
	@Produces("application/json")
	public Response responseApplikationRestart(@PathParam("inkarnationsName") String inkarnationsName) {

		try {
			Applikation applikation = processManager.restarteApplikation(inkarnationsName);
			Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK).header("Content-Type",
					"application/json");
			responseBuilder.entity(applikation);
			return responseBuilder.build();
		} catch (StartStoppException e) {
			Statusresponse status = new Statusresponse();
			status.setCode(-1);
			status.getMessages().add(e.getLocalizedMessage());
			return Response.status(Response.Status.CONFLICT).entity(status).build();
		}
	}

	@POST
	@Path("{inkarnationsName}/stopp")
	@Produces("application/json")
	public Response responseApplikationStopp(@PathParam("inkarnationsName") String inkarnationsName) {

		try {
			Applikation applikation = processManager.stoppeApplikation(inkarnationsName);
			Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK).header("Content-Type",
					"application/json");
			responseBuilder.entity(applikation);
			return responseBuilder.build();
		} catch (StartStoppException e) {
			Statusresponse status = new Statusresponse();
			status.setCode(-1);
			status.getMessages().add(e.getLocalizedMessage());
			return Response.status(Response.Status.CONFLICT).entity(status).build();
		}
	}
}
