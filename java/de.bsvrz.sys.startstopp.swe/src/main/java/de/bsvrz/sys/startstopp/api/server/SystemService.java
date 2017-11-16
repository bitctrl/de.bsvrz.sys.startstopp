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

import java.util.concurrent.CompletableFuture;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus;
import de.bsvrz.sys.startstopp.startstopp.StartStopp;

@Path("/ststapi/v1/system")
public class SystemService {

	private static final Debug LOGGER = Debug.getLogger();
	private StartStopp startStopp;

	public SystemService() {
		this(StartStopp.getInstance());
	}

	public SystemService(StartStopp startStopp) {
		this.startStopp = startStopp;
	}

	@GET
	@Produces("application/json")
	public Response responseStartStoppStatus() {

		Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK).header("Content-Type",
				"application/json");
		responseBuilder.entity(
				new StartStoppStatus(startStopp.getStatus(), startStopp.getOptions().isBetriebsMeldungVersenden(),
						startStopp.getProcessManager().getDavConnector().getConnectionStatus()));
		return responseBuilder.build();
	}

	@POST
	@Path("exit")
	public Response responseStartStoppExit() {
		Response response = Response.accepted().build();
		CompletableFuture.runAsync(() -> System.exit(0));
		return response;
	}

	@POST
	@Path("stopp")
	public Response responseStartStoppStopp() {
		Response response = Response.accepted().build();
		startStopp.getProcessManager().stoppeSkript();
		return response;
	}

	@POST
	@Path("restart")
	public Response responseStartStoppRestart() {
		Response response = Response.accepted().build();
		startStopp.getProcessManager().restarteSkript();
		return response;
	}

	@POST
	@Path("betriebsmeldungen")
	public Response responseBetriebsmeldungenUmschalten() {
		Response response = Response.accepted().build();
		startStopp.getOptions().setBetriebsMeldungVersenden(!startStopp.getOptions().isBetriebsMeldungVersenden());
		return response;
	}

	@POST
	@Path("start")
	public Response responseStartStoppStart() {
		try {
			startStopp.getProcessManager().starteSkript();
		} catch (StartStoppException e) {
			LOGGER.warning("Fehler beim Verarbeiten einer StartStopp-Startanforderung: " + e.getLocalizedMessage());
		}
		return Response.accepted().build();
	}

	@GET
	@Path("applikationen")
	public Response responseStartStoppSystemApplikationen() {
		Response response = Response.status(Response.Status.NOT_IMPLEMENTED).build();
		// TODO Applikationen aller Rechner ermitteln
		return response;
	}
}
