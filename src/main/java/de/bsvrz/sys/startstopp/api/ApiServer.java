package de.bsvrz.sys.startstopp.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.component.LifeCycle;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import de.bsvrz.sys.startstopp.data.StartStoppKonfiguration;
import de.bsvrz.sys.startstopp.process.ProcessManager;
import de.bsvrz.sys.startstopp.startstopp.StartStoppOptions;

public class ApiServer extends Thread {

	public ApiServer(StartStoppOptions options, ProcessManager processManager) {
		super("StartStoppApi");
		setDaemon(true);

		String address = "http://127.0.0.1:8080/";

		try {
			Server server = new Server(new InetSocketAddress("localhost", 8080));
			server.setStopAtShutdown(true);
			server.addLifeCycleListener(new LifeCycle.Listener() {

				@Override
				public void lifeCycleStopping(LifeCycle arg0) {
					// TODO Auto-generated method stub
					System.err.println("Stopping: " + arg0);

				}

				@Override
				public void lifeCycleStopped(LifeCycle arg0) {
					// TODO Auto-generated method stub
					System.err.println("Stopped: " + arg0);

				}

				@Override
				public void lifeCycleStarting(LifeCycle arg0) {
					// TODO Auto-generated method stub
					System.err.println("Starting: " + arg0);

				}

				@Override
				public void lifeCycleStarted(LifeCycle arg0) {
					// TODO Auto-generated method stub
					System.err.println("Started: " + arg0);

				}

				@Override
				public void lifeCycleFailure(LifeCycle arg0, Throwable arg1) {
					// TODO Auto-generated method stub
					System.err.println("Failure: " + arg0 + ": " + arg1);
				}
			});
			server.setHandler(new AbstractHandler() {

				@Override
				public void handle(String target, Request baseRequest, HttpServletRequest request,
						HttpServletResponse response) throws IOException, ServletException {
					// TODO Auto-generated method stub
					System.err.println("Handle: " + request);
					
					response.setContentType("text/json; charset=utf-8");
					response.setStatus(HttpServletResponse.SC_OK);
					
					if (request.toString().contains("stop")) {
						try {
							server.stop();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					} else {

						try (InputStream stream = StartStoppKonfiguration.class
								.getResourceAsStream("testkonfigurationen/startStopp01_1.xml")) {

							StartStoppKonfiguration konfiguration = new StartStoppKonfiguration(stream);
							JSONObject json = konfiguration.getJson();
							PrintWriter out = response.getWriter();
							out.println(json.toString());
						} catch (ParserConfigurationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SAXException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					baseRequest.setHandled(true);
				}
			});
			server.start();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
