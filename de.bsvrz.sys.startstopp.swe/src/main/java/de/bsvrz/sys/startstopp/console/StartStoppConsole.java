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
import java.io.InputStream;
import java.util.Properties;
import java.util.TooManyListenersException;

import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.PropertyTheme;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import de.bsvrz.sys.startstopp.api.client.StartStoppClient;
import de.bsvrz.sys.startstopp.console.ui.online.StartStoppOnlineWindow;
import de.bsvrz.sys.startstopp.process.os.OSTools;

public class StartStoppConsole {

	private static final StartStoppConsole INSTANZ = new StartStoppConsole();
	private StartStoppConsoleOptions options;
	private StartStoppClient client;
	private MultiWindowTextGUI gui;

	public static void main(String[] args) throws IOException {

		Properties themeProperties = new Properties();
		try (InputStream stream = StartStoppConsole.class.getResourceAsStream("nerz-mono.properties")) {
			themeProperties.load(stream);
			LanternaThemes.registerTheme("NERZ-Mono", new PropertyTheme(themeProperties));
		}

		themeProperties = new Properties();
		try (InputStream stream = StartStoppConsole.class.getResourceAsStream("nerz-color.properties")) {
			themeProperties.load(stream);
			LanternaThemes.registerTheme("NERZ-Color", new PropertyTheme(themeProperties));
		}

		INSTANZ.options = new StartStoppConsoleOptions(args);
		INSTANZ.client = new StartStoppClient(INSTANZ.options.getHost(), INSTANZ.options.getPort());

		DefaultTerminalFactory factory = new DefaultTerminalFactory();
		factory.setPreferTerminalEmulator(OSTools.isWindows());
		factory.setForceTextTerminal(!OSTools.isWindows());
		try (Terminal term = factory.createTerminal()) {
			try (Screen screen = new TerminalScreen(term)) {
				INSTANZ.gui = new MultiWindowTextGUI(screen);
				if (INSTANZ.options.isMonochrome()) {
					INSTANZ.gui.setTheme(LanternaThemes.getRegisteredTheme("NERZ-Mono"));
				} else {
					INSTANZ.gui.setTheme(LanternaThemes.getRegisteredTheme("NERZ-Color"));
				}
				screen.startScreen();

				INSTANZ.gui.getScreen().startScreen();
				StartStoppOnlineWindow onlineWindow = new StartStoppOnlineWindow();
				INSTANZ.gui.addWindow(onlineWindow);
				onlineWindow.waitUntilClosed();
				INSTANZ.gui.getScreen().stopScreen();
			}
			term.clearScreen();
		}
		System.exit(0);
	}

	public static WindowBasedTextGUI getGui() {
		return INSTANZ.gui;
	}

	public static StartStoppClient getClient() {
		return INSTANZ.client;
	}
}
