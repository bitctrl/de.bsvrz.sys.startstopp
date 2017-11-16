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

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import de.bsvrz.sys.startstopp.api.StartStoppClient;
import de.bsvrz.sys.startstopp.startstopp.StartStopp;
import de.bsvrz.sys.startstopp.startstopp.StartStoppOptions;

public class ApiServer {

	private final StartStoppOptions options;

	public ApiServer() {
		this(StartStopp.getInstance().getOptions());
	}

	public ApiServer(StartStoppOptions options) {
		this.options = options;
	}

	public void start() throws Exception {
		URI baseUri = UriBuilder.fromUri("https://localhost/").port(options.getHttpsPort()).build();
		ResourceConfig config = new ResourceConfig(SystemService.class, SkripteService.class,
				ApplikationenService.class, RechnerService.class);

		SslContextFactory sslContextFactory = new SslContextFactory();
		sslContextFactory.setKeyStorePath(StartStoppClient.class.getResource("keystore.jks").toExternalForm());
		sslContextFactory.setKeyStorePassword("startstopp");
		sslContextFactory.setKeyManagerPassword("startstopp");
		Server httpsServer = JettyHttpContainerFactory.createServer(baseUri, sslContextFactory, config);
		httpsServer.start();

		if (options.getHttpPort() > 0) {
			baseUri = UriBuilder.fromUri("http://localhost/").port(options.getHttpPort()).build();
			Server httpServer = JettyHttpContainerFactory.createServer(baseUri, config);
			httpServer.start();
		}
	}

}
