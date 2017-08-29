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
import de.bsvrz.sys.startstopp.api.server.ApiServer;
import de.bsvrz.sys.startstopp.config.SkriptManager;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.process.ProzessManager;

public class StartStopp {

	private static final Debug LOGGER = Debug.getLogger();

	private static StartStopp instance = new StartStopp();

	private StartStoppOptions options;

	private SkriptManager skriptManager;

	private ProzessManager processManager;

	private ApiServer apiServer;
	private String inkarnationsPrefix;

	
	public static StartStopp getInstance() {
		return instance;
	}

	public static void main(String[] args) throws Exception {
		try {
			System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "1");
			instance.init(args);
			instance.start();
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(-1);
		}
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
				LOGGER.warning("Hostname kann nicht bestimmt werden: " + e.getLocalizedMessage());
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

	public StartStoppStatus getStatus() {
		StartStoppStatus status = new StartStoppStatus();
		try {
			skriptManager.getCurrentSkript();
		} catch (StartStoppException e) {
			LOGGER.fine(e.getLocalizedMessage());
			status.setStatus(StartStoppStatus.Status.CONFIGERROR);
			return status;
		}

		status.setStatus(processManager.getStartStoppStatus());
		return status;
	}

	private void init(String ... args) throws Exception {

		Debug.init("StartStopp", new ArgumentList(args));

		options = new StartStoppOptions(args);
		skriptManager = new SkriptManager();
		processManager = new ProzessManager();
		apiServer = new ApiServer();
	}

	private void start() throws Exception {
		apiServer.start();
	}
}
