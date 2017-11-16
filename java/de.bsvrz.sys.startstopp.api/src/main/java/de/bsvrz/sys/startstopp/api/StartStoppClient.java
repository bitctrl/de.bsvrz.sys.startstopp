/*
 * Segment 10 System (Sys), SWE 10.1 StartStopp API
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

package de.bsvrz.sys.startstopp.api;

import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.client.StartStoppStatusException;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.ApplikationLog;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkriptStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.StatusResponse;
import de.bsvrz.sys.startstopp.api.jsonschema.VersionierungsRequest;

public class StartStoppClient {

	private static final Debug LOGGER = Debug.getLogger();

	private class StartStoppHostnameVerifier implements HostnameVerifier {

		@Override
		public boolean verify(String hostname, SSLSession session) {
			if (!startStoppHostName.equals(hostname)) {
				return false;
			}

			String peerHost = session.getPeerHost();
			if (!startStoppHostName.equals(peerHost)) {
				return false;
			}

			return true;
		}
	}

	private StartStoppHostnameVerifier verifier = new StartStoppHostnameVerifier();
	private String startStoppHostName;
	private Client connector;
	private int port;

	public StartStoppClient(String host, int port) {
		this.startStoppHostName = host;
		this.port = port;
	}

	private Client getConnector() throws StartStoppException {
		if (connector == null) {
			SslContextFactory sslContextFactory = new SslContextFactory();
			sslContextFactory.setKeyStorePath(StartStoppClient.class.getResource("keystore.jks").toExternalForm());
			sslContextFactory.setKeyStorePassword("startstopp");
			sslContextFactory.setKeyManagerPassword("startstopp");
			try {
				sslContextFactory.start();
			} catch (Exception e) {
				throw new StartStoppException(e);
			}

			SSLContext sslContext = sslContextFactory.getSslContext();
			connector = ClientBuilder.newBuilder().sslContext(sslContext).hostnameVerifier(verifier)
					.withConfig(new ClientConfig().register(JacksonJsonProvider.class)).build();
		}
		return connector;
	}

	private Response createPostResponse(String string) throws StartStoppException {
		return createPostResponse(string, null);
	}

	private Response createPostResponse(String path, Object object) throws StartStoppException {
		Entity<?> entity = null;
		if (object != null) {
			entity = Entity.entity(object, MediaType.APPLICATION_JSON_TYPE);
		}
		Response response = getConnector().target("https://" + startStoppHostName + ":" + port + "/ststapi/v1" + path)
				.request(MediaType.APPLICATION_JSON).post(entity);
		return response;
	}

	private Response createPutResponse(String path, Object object) throws StartStoppException {
		Entity<?> entity = null;
		if (object != null) {
			entity = Entity.entity(object, MediaType.APPLICATION_JSON_TYPE);
		}
		Response response = getConnector().target("https://" + startStoppHostName + ":" + port + "/ststapi/v1" + path)
				.request(MediaType.APPLICATION_JSON).put(entity);
		return response;
	}

	private Response createGetResponse(String path) throws StartStoppException {
		Response response = getConnector().target("https://" + startStoppHostName + ":" + port + "/ststapi/v1" + path)
				.request(MediaType.APPLICATION_JSON).get(Response.class);
		return response;
	}

	/* System-Funktionen. */

	public StartStoppStatus getStartStoppStatus() throws StartStoppException {
		int status;
		try (Response response = createGetResponse("/system")) {
			status = response.getStatus();
			if (status == Response.Status.OK.getStatusCode()) {
				return response.readEntity(StartStoppStatus.class);
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		throw new StartStoppException("SystemStatus konnte nicht abgerufen werden (Response: " + status + ")");
	}

	public void exitStartStopp() throws StartStoppException {
		int status;
		try (Response response = createPostResponse("/system/exit")) {
			status = response.getStatus();
			if (status == Response.Status.ACCEPTED.getStatusCode()) {
				return;
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		throw new StartStoppException(
				"Anforderung zum Beenden von StartStopp wurde nicht entgegengenommen (Response: " + status + ")");
	}

	public void stoppStartStopp() throws StartStoppException {
		int status;
		try (Response response = createPostResponse("/system/stopp")) {
			status = response.getStatus();
			if (status == Response.Status.ACCEPTED.getStatusCode()) {
				return;
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		throw new StartStoppException(
				"Anforderung zum Beenden der StartStopp-Konfiguration wurde nicht entgegengenommen (Response: " + status
						+ ")");
	}

	public void startStartStopp() throws StartStoppException {
		int status;

		try (Response response = createPostResponse("/system/start")) {
			status = response.getStatus();
			if (status == Response.Status.ACCEPTED.getStatusCode()) {
				return;
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		throw new StartStoppException(
				"Anforderung zum Starten der StartStopp-Konfiguration wurde nicht entgegengenommen (Response: " + status
						+ ")");
	}

	public void restartStartStopp() throws StartStoppException {
		int status;

		try (Response response = createPostResponse("/system/restart")) {
			status = response.getStatus();
			if (status == Response.Status.ACCEPTED.getStatusCode()) {
				return;
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		throw new StartStoppException(
				"Anforderung zum Neustart der StartStopp-Konfiguration wurde nicht entgegengenommen (Response: "
						+ status + ")");
	}

	public void betriebsmeldungenUmschalten() throws StartStoppException {

		int status;

		try (Response response = createPostResponse("/system/betriebsmeldungen")) {
			status = response.getStatus();
			if (status == Response.Status.ACCEPTED.getStatusCode()) {
				return;
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		throw new StartStoppException(
				"Anforderung zum Neustart der StartStopp-Konfiguration wurde nicht entgegengenommen (Response: "
						+ status + ")");
	}

	/* Skript-Funktionen. */

	public StartStoppSkript getCurrentSkript() throws StartStoppException {
		int status;

		try (Response response = createGetResponse("/skripte/current")) {
			status = response.getStatus();
			if (status == Response.Status.OK.getStatusCode()) {
				return response.readEntity(StartStoppSkript.class);
			}
			if (status == Response.Status.SERVICE_UNAVAILABLE.getStatusCode()) {
				throw new StartStoppStatusException(
						"Die aktuelle StartStopp-Konfiguration konnte nicht abgerufen werden",
						response.readEntity(StatusResponse.class));
			}
		} catch (Exception e) {
			throw new StartStoppException("Die aktuelle StartStopp-Konfiguration konnte nicht abgerufen werden", e);
		}

		throw new StartStoppException(
				"Die aktuelle StartStopp-Konfiguration konnte nicht abgerufen werden (Response: " + status + ")");
	}

	public StartStoppSkript setCurrentSkript(String veranlasser, String passwort, String name, String grund,
			StartStoppSkript skript) throws StartStoppException {

		VersionierungsRequest request = new VersionierungsRequest();
		request.setVeranlasser(veranlasser);
		request.setPasswort(passwort);
		if (name != null) {
			request.setName(name);
		}
		request.setAenderungsgrund(grund);
		request.setSkript(skript);

		int status;
		try (Response response = createPutResponse("/skripte/current", request)) {

			status = response.getStatus();
			if (status == Response.Status.OK.getStatusCode()) {
				return response.readEntity(StartStoppSkript.class);
			}
			if (status == Response.Status.BAD_REQUEST.getStatusCode()) {
				throw new StartStoppStatusException("Die aktuelle StartStopp-Konfiguration konnte nicht gesetzt werden",
						response.readEntity(StatusResponse.class));
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}

		throw new StartStoppException(
				"Die aktuelle StartStopp-Konfiguration konnte nicht gesetzt werden (Response: " + status + ")");
	}

	public StartStoppSkriptStatus getCurrentSkriptStatus() throws StartStoppException {

		int status;

		try (Response response = createGetResponse("/skripte/current/status")) {

			status = response.getStatus();
			if (status == Response.Status.OK.getStatusCode()) {
				return response.readEntity(StartStoppSkriptStatus.class);
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		throw new StartStoppException(
				"Die Status der aktuellen StartStopp-Konfiguration konnte nicht abgerufen werden (Response: " + status
						+ ")");
	}

	/* Applikation-Funktionen. */

	public List<Applikation> getApplikationen() throws StartStoppException {

		int status;
		try (Response response = createGetResponse("/applikationen")) {
			status = response.getStatus();
			if (status == Response.Status.OK.getStatusCode()) {
				return response.readEntity(new GenericType<List<Applikation>>() {
					// keine zusätzlicher Code erforderlich
				});
			}
		} catch (Exception e) {
			LOGGER.fine(e.getLocalizedMessage());
			throw new StartStoppException("Keine Verbindung zu StartStopp!");
		}
		throw new StartStoppException("Applikationen konnten nicht abgerufen werden (Response: " + status + ")");
	}

	public Applikation getApplikation(String inkarnationsName) throws StartStoppException {

		int status;

		try (Response response = createGetResponse("/applikationen/" + inkarnationsName)) {
			status = response.getStatus();
			if (status == Response.Status.OK.getStatusCode()) {
				return response.readEntity(Applikation.class);
			}
		} catch (Exception e) {
			LOGGER.fine(e.getLocalizedMessage());
			throw new StartStoppException("Keine Verbindung zu StartStopp!");
		}
		throw new StartStoppException(
				"Die Applikation \"" + inkarnationsName + "\"konnte nicht abgerufen werden (Response: " + status + ")");
	}

	public ApplikationLog getApplikationLog(String inkarnationsName) throws StartStoppException {

		int status;

		try (Response response = createGetResponse("/applikationen/" + inkarnationsName + "/log")) {

			status = response.getStatus();
			if (status == Response.Status.OK.getStatusCode()) {
				return response.readEntity(ApplikationLog.class);
			}
		} catch (Exception e) {
			LOGGER.fine(e.getLocalizedMessage());
			throw new StartStoppException("Keine Verbindung zu StartStopp!");
		}
		throw new StartStoppException("Die Ausgaben der Applikation \"" + inkarnationsName
				+ "\"konnten nicht abgerufen werden (Response: " + status + ")");
	}

	public Applikation starteApplikation(String inkarnationsName) throws StartStoppException {

		int status;
		try (Response response = createPostResponse("/applikationen/" + inkarnationsName + "/start")) {

			status = response.getStatus();
			if (status == Response.Status.OK.getStatusCode()) {
				return response.readEntity(Applikation.class);
			}
			if (status == Response.Status.CONFLICT.getStatusCode()) {
				throw new StartStoppStatusException("Die Applikation \"" + inkarnationsName
						+ "\"konnte nicht gestartet werden (Response: " + status + ")",
						response.readEntity(StatusResponse.class));
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		throw new StartStoppException(
				"Die Applikation \"" + inkarnationsName + "\"konnte nicht gestartet werden (Response: " + status + ")");
	}

	public Applikation restarteApplikation(String inkarnationsName) throws StartStoppException {

		int status;

		try (Response response = createPostResponse("/applikationen/" + inkarnationsName + "/restart");) {
			status = response.getStatus();
			if (status == Response.Status.OK.getStatusCode()) {
				return response.readEntity(Applikation.class);
			}
			if (status == Response.Status.CONFLICT.getStatusCode()) {
				throw new StartStoppStatusException("Die Applikation \"" + inkarnationsName
						+ "\"konnte nicht neu gestartet werden (Response: " + status + ")",
						response.readEntity(StatusResponse.class));
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		throw new StartStoppException("Die Applikation \"" + inkarnationsName
				+ "\"konnte nicht neu gestartet werden (Response: " + status + ")");
	}

	public Applikation stoppeApplikation(String inkarnationsName) throws StartStoppException {

		int status;

		try (Response response = createPostResponse("/applikationen/" + inkarnationsName + "/stopp")) {
			status = response.getStatus();
			if (status == Response.Status.OK.getStatusCode()) {
				return response.readEntity(Applikation.class);
			}
			if (status == Response.Status.CONFLICT.getStatusCode()) {
				throw new StartStoppStatusException("Die Applikation \"" + inkarnationsName
						+ "\"konnte nicht gestoppt gestartet werden (Response: " + status + ")",
						response.readEntity(StatusResponse.class));
			}
		} catch (Exception e) {
			throw new StartStoppException(e);
		}
		throw new StartStoppException("Die Applikation \"" + inkarnationsName
				+ "\"konnte nicht gestoppt werden (Response: " + status + ")");
	}

	public String getStartStoppHostName() {
		return startStoppHostName;
	}

	public int getPort() {
		return port;
	}
}
