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

package de.bsvrz.sys.startstopp.startstopp;

import java.net.InetAddress;
import java.net.UnknownHostException;

import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus.Status;
import de.bsvrz.sys.startstopp.api.server.ApiServer;
import de.bsvrz.sys.startstopp.config.SkriptManager;
import de.bsvrz.sys.startstopp.process.ProzessManager;
import de.bsvrz.sys.startstopp.process.os.OSTools;
import de.muspellheim.events.Event;

public class StartStopp {

	private static Debug logger = Debug.getLogger();
	private static StartStopp instance = new StartStopp(); 

	public final Event<StartStoppStatus.Status> onStartStoppStatusChanged = new Event<>();

	private Object stopLock = new Object();

	private StartStoppOptions options;

	private SkriptManager skriptManager;

	private ProzessManager processManager;

	private StartStoppStatus.Status status = Status.INITIALIZED;

	private ApiServer apiServer;
	private String inkarnationsPrefix;

	public static StartStopp getInstance() {
		return instance;
	}

	public static void main(String[] args) throws Exception {
		try {

			Debug.init("StartStopp", new ArgumentList(args));
			logger = Debug.getLogger();

			instance.init(args);
			instance.start();

			
		} catch (Exception e) {
			logger.error("StartStopp abgebrochen: " + e.getLocalizedMessage());
			System.err.println("StartStopp abgebrochen: " + e.getLocalizedMessage());
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static boolean isMinimalWindowsVersion() {
		String property = System.getProperty("os.version");
		try {
			if( property != null) {
				String[] parts = property.split("\\.");
				if( parts.length > 1) {
					int mainVersion = Integer.parseInt(parts[0]);
					if( mainVersion == 6) {
						if( Integer.parseInt(parts[1]) >= 2) {
							return true;
						}
					} else if (mainVersion > 6) {
						return true;
					}
				}
			}
		} catch (NumberFormatException e) {
			logger.warning("Windowsversion kann nicht interpretiert werden: " + property);
		}
		logger.warning("Minimale Windowsversion nicht gegeben: " + property);
		return false;
	}

	public String getInkarnationsPrefix() {

		if (inkarnationsPrefix == null) {

			StringBuilder builder = new StringBuilder(200);
			builder.append("StartStopp_");
			String hostName;
			try {
				hostName = InetAddress.getLocalHost().getHostName();
				builder.append(hostName);
			} catch (UnknownHostException e) {
				logger.warning("Lokaler Hostname kann nicht bestimmt werden: " + e.getLocalizedMessage());
				builder.append("unknown_host");
			}
			builder.append('_');
			inkarnationsPrefix = builder.toString();
		}

		return inkarnationsPrefix;
	}

	public StartStoppOptions getOptions() {
		return options;
	}

	public ProzessManager getProcessManager() {
		return processManager;
	}

	public SkriptManager getSkriptManager() {
		return skriptManager;
	}

	private void init(String... args) throws Exception {

		options = new StartStoppOptions(args);
		skriptManager = new SkriptManager();
		processManager = new ProzessManager();
		apiServer = new ApiServer();
	}

	private void start() throws Exception {
		apiServer.start();
	}

	public StartStoppStatus.Status getStatus() {
		return status;
	}

	public void setStatus(StartStoppStatus.Status status) {
		setStatus(status, false);
	}

	public void setStatus(StartStoppStatus.Status newStatus, boolean force) {

		if ((this.status != newStatus) || force) {
			this.status = newStatus;
			onStartStoppStatusChanged.send(newStatus);
		}
		synchronized (stopLock) {
			stopLock.notifyAll();
		}
	}

	public void waitForStopp() {
		while (getStatus() == Status.STOPPING) {
			synchronized (stopLock) {
				try {
					stopLock.wait(1000);
				} catch (InterruptedException e) {
					logger.fine(e.getLocalizedMessage());
				}
			}
		}
	}
}
