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

import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkriptStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus;
import de.bsvrz.sys.startstopp.api.server.ApiServer;
import de.bsvrz.sys.startstopp.config.StartStoppKonfiguration;
import de.bsvrz.sys.startstopp.config.SkriptManager;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.process.ProzessManager;

public class StartStopp {

	private static StartStopp instance = new StartStopp();

	public static StartStopp getInstance() {
		return instance;
	}

	public static void main(String[] args) throws Exception {
		try {
			instance.init(args);
			instance.start();
		} catch (Exception e) {
			instance.cancel(e);
		}
	}

	private StartStoppOptions options;

	private SkriptManager skriptManager;

	private ProzessManager processManager;

	private ApiServer apiServer;

	private void cancel(Exception e) {
		System.err.println(e.getLocalizedMessage());
		if( processManager != null) {
			processManager.stopp();
		}
		System.exit(-1);
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
		StartStoppKonfiguration skript = null;
		try {
			skript = skriptManager.getCurrentSkript();
		} catch (StartStoppException e) {
			status.setStatus(StartStoppStatus.Status.CONFIGERROR);
			return status;
		}
		
		if (skript.getSkriptStatus().getStatus() == StartStoppSkriptStatus.Status.FAILURE) {
			status.setStatus(StartStoppStatus.Status.CONFIGERROR);
		} else {
			if (processManager.isSkriptRunning()) {
				status.setStatus(StartStoppStatus.Status.RUNNING);
			} else if (processManager.isSkriptStopped()) {
				status.setStatus(StartStoppStatus.Status.STOPPED);
			} else {
				status.setStatus(StartStoppStatus.Status.INITIALIZED);
			}
		}
		return status;
	}

	private void init(String[] args) throws Exception {

		Debug.init("StartStopp", new ArgumentList(args));

		options = new StartStoppOptions(args);
		skriptManager = new SkriptManager();
		processManager = new ProzessManager();
		apiServer = new ApiServer();
	}

	private void start() throws Exception {
		processManager.start();
		apiServer.start();
	}

	public void stoppApplikation() {
		
		new Thread() {
			@Override
			public void run() {
				Thread stopper = processManager.stoppeSkript(false);
				if( stopper != null) {
					try {
						stopper.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				System.exit(0);
			}
		}.start();
	}
}
