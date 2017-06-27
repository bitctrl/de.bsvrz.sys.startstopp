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

import de.bsvrz.sys.startstopp.api.server.ApiServer;
import de.bsvrz.sys.startstopp.config.SkriptManager;
import de.bsvrz.sys.startstopp.process.ProcessManager;

public class StartStopp  {

	private static StartStopp instance;

	private final StartStoppOptions options;

	private final SkriptManager skriptManager;

	public SkriptManager getSkriptManager() {
		return skriptManager;
	}

	private final ProcessManager processManager;

	public ProcessManager getProcessManager() {
		return processManager;
	}

	private ApiServer apiServer;

	public StartStopp(String[] args) {

		options = new StartStoppOptions(args);
		
		skriptManager = new SkriptManager(options);
		
		processManager = new ProcessManager(skriptManager, options);
		processManager.start();

		apiServer = new ApiServer(options, processManager);
		apiServer.start();
	}

	public static void main(String[] args) {
		instance = new StartStopp(args);
	}


	public StartStoppOptions getOptions() {
		return options;
	}

	public static StartStopp getInstance() {
		return instance;
	}
	
}
