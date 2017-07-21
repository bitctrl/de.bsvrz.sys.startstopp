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

package de.bsvrz.sys.startstopp.console;

import java.io.IOException;

import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import de.bsvrz.sys.startstopp.api.client.StartStoppClient;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.console.ui.StartStoppOnlineWindow;

public class StartStoppConsole {

	private static StartStoppConsole instance;
	private StartStoppClient client;

	public static StartStoppConsole getInstance() {
		return instance;
	}

	public StartStoppClient getClient() {
		return client;
	}

	public StartStoppConsole() throws StartStoppException {
		client = new StartStoppClient("localhost", 3000);
	}

	void run() throws IOException, StartStoppException {
		
		DefaultTerminalFactory factory = new DefaultTerminalFactory();
//		factory.setTelnetPort(6500);
//		factory.setForceTextTerminal(true);
		Terminal term = factory.createTerminal();
// 		System.err.println("Term: " + term.getClass());
		Screen screen = new TerminalScreen(term);
		WindowBasedTextGUI gui = new MultiWindowTextGUI(screen);

		screen.startScreen();

		StartStoppOnlineWindow onlineWindow = new StartStoppOnlineWindow();
		gui.addWindow(onlineWindow);

		onlineWindow.waitUntilClosed();

		screen.stopScreen();
		System.exit(0);
	}
	
	public static void main(String[] args) throws IOException, InterruptedException, StartStoppException {
		instance = new StartStoppConsole();
		instance.run();
	}

}
