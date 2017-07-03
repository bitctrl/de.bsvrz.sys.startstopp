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

public class ApiServer {

	public void start() throws Exception {
		URI baseUri = UriBuilder.fromUri("https://localhost/").port(9998).build();
		ResourceConfig config = new ResourceConfig(SystemService.class, SkripteService.class,
				ApplikationenService.class, RechnerService.class);

		SslContextFactory sslContextFactory = new SslContextFactory();
		sslContextFactory.setKeyStorePath(ApiServer.class.getResource("keystore.jks").toExternalForm());
		sslContextFactory.setKeyStorePassword("startstopp");
		sslContextFactory.setKeyManagerPassword("startstopp");
		Server httpsServer = JettyHttpContainerFactory.createServer(baseUri, sslContextFactory, config);

		baseUri = UriBuilder.fromUri("http://localhost/").port(9999).build();
		Server httpServer = JettyHttpContainerFactory.createServer(baseUri, config);

		httpsServer.start();
		httpServer.start();
	}

}
