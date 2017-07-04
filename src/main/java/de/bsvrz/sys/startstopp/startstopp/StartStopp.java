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

import de.bsvrz.sys.startstopp.api.jsonschema.Startstoppskriptstatus;
import de.bsvrz.sys.startstopp.api.jsonschema.Startstoppstatus;
import de.bsvrz.sys.startstopp.api.server.ApiServer;
import de.bsvrz.sys.startstopp.config.ManagedSkript;
import de.bsvrz.sys.startstopp.config.SkriptManager;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.process.ProcessManager;

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

	private ProcessManager processManager;

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

	public ProcessManager getProcessManager() {
		return processManager;
	}

	public SkriptManager getSkriptManager() {
		return skriptManager;
	}

	public Startstoppstatus getStatus() {
		Startstoppstatus status = new Startstoppstatus();
		ManagedSkript skript = null;
		try {
			skript = skriptManager.getCurrentSkript();
		} catch (StartStoppException e) {
			status.setStatus(Startstoppstatus.Status.CONFIGERROR);
			return status;
		}
		
		if (skript.getSkriptStatus().getStatus() == Startstoppskriptstatus.Status.FAILURE) {
			status.setStatus(Startstoppstatus.Status.CONFIGERROR);
		} else {
			if (processManager.isSkriptRunning()) {
				status.setStatus(Startstoppstatus.Status.RUNNING);
			} else if (processManager.isSkriptStopped()) {
				status.setStatus(Startstoppstatus.Status.STOPPED);
			} else {
				status.setStatus(Startstoppstatus.Status.INITIALIZED);
			}
		}
		return status;
	}

	private void init(String[] args) throws Exception {
		options = new StartStoppOptions(args);
		skriptManager = new SkriptManager();
		processManager = new ProcessManager();
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.exit(0);
			}
		}.start();
	}

	public void stoppCurrentSkript() {
		processManager.stoppeSkript(false);
	}

	public void restartCurrentSkript() {
		processManager.stoppeSkript(true);
	}
}
