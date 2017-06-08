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

package de.bsvrz.sys.startstopp.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import de.bsvrz.sys.startstopp.data.StartStoppKonfiguration;
import de.bsvrz.sys.startstopp.process.ProcessManager;
import de.bsvrz.sys.startstopp.startstopp.StartStoppOptions;

public class ApiServer extends Thread {

	public ApiServer(StartStoppOptions options, ProcessManager processManager) {
		super("StartStoppApi");
		setDaemon(true);


//		try {
			
//			Server server = new Server(new InetSocketAddress("localhost", 8080));
//			server.setStopAtShutdown(true);
//			server.setHandler(new AbstractHandler() {
//
//				@Override
//				public void handle(String target, Request baseRequest, HttpServletRequest request,
//						HttpServletResponse response) throws IOException, ServletException {
//					// TODO Auto-generated method stub
//					System.err.println("Handle: " + request);
//					
//					response.setContentType("text/json; charset=utf-8");
//					response.setStatus(HttpServletResponse.SC_OK);
//					
//					if (request.toString().contains("stop")) {
//						try {
//							server.stop();
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//
//					} else {
//
//						try (InputStream stream = StartStoppKonfiguration.class
//								.getResourceAsStream("testkonfigurationen/startStopp01_1.xml")) {
//
//							StartStoppKonfiguration konfiguration = new StartStoppKonfiguration(stream);
//							JSONObject json = konfiguration.getJson();
//							PrintWriter out = response.getWriter();
//							out.println(json.toString());
//						} catch (ParserConfigurationException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						} catch (SAXException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//
//					baseRequest.setHandled(true);
//				}
//			});
//			server.start();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		URI baseUri = UriBuilder.fromUri("http://localhost/").port(9998).build();
		ResourceConfig config = new ResourceConfig(TracksImpl.class);
	    Server server = JettyHttpContainerFactory.createServer(baseUri, config);
//	    Server server = JettyHttpContainerFactory.createServer(baseUri, false);
//	    server.setHandler(new AbstractHandler() {
//
//			@Override
//			public void handle(String target, Request baseRequest, HttpServletRequest request,
//					HttpServletResponse response) throws IOException, ServletException {
//				// TODO Auto-generated method stub
//				System.err.println("Handle: " + request);
//				
//				response.setContentType("text/json; charset=utf-8");
//				response.setStatus(HttpServletResponse.SC_OK);
//				
//				if (request.toString().contains("stop")) {
//					try {
//						server.stop();
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//
//				} else {
//
//					try (InputStream stream = StartStoppKonfiguration.class
//							.getResourceAsStream("testkonfigurationen/startStopp01_1.xml")) {
//
//						StartStoppKonfiguration konfiguration = new StartStoppKonfiguration(stream);
//						JSONObject json = konfiguration.getJson();
//						PrintWriter out = response.getWriter();
//						out.println(json.toString());
//					} catch (ParserConfigurationException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (SAXException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//
//				baseRequest.setHandled(true);
//			}
//		});
	    try {
			server.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
