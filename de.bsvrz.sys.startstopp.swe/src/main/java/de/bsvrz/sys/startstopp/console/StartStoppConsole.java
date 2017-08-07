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

import javax.annotation.PostConstruct;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.PropertyTheme;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import de.bsvrz.sys.startstopp.api.client.StartStoppClient;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.console.ui.GuiComponentFactory;
import de.bsvrz.sys.startstopp.console.ui.online.StartStoppOnlineWindow;

@Singleton
public class StartStoppConsole {

	@Singleton
	private static class TextGuiProvider implements Provider<WindowBasedTextGUI> {

		WindowBasedTextGUI gui;

		@Override
		public WindowBasedTextGUI get() {
			if (gui == null) {
				try {
					DefaultTerminalFactory factory = new DefaultTerminalFactory();
					Terminal term = factory.createTerminal();
					Screen screen = new TerminalScreen(term);
					gui = new MultiWindowTextGUI(screen);
					screen.startScreen();
				} catch (IOException e) {
					throw new IllegalStateException("Die UI kann nicht initialisiert werden!", e);

				}
			}
			return gui;
		}

	}

	private static class StartStoppModule implements Module {

		private StartStoppConsoleOptions options;
		private StartStoppClient client;

		StartStoppModule(String[] args) {
			options = new StartStoppConsoleOptions(args);
			client = new StartStoppClient(options.getHost(), options.getPort());
		}

		@Override
		public void configure(Binder binder) {
			binder.bind(StartStoppConsoleOptions.class).toInstance(options);
			binder.bind(StartStoppClient.class).toInstance(client);
			binder.bind(WindowBasedTextGUI.class).toProvider(TextGuiProvider.class);

			binder.install(new FactoryModuleBuilder().build(GuiComponentFactory.class));
		}
	}

	@PostConstruct
	@Inject
	void run(WindowBasedTextGUI gui, StartStoppOnlineWindow onlineWindow) throws IOException, StartStoppException {
		gui.getScreen().startScreen();
		gui.addWindow(onlineWindow);
		onlineWindow.waitUntilClosed();
		gui.getScreen().stopScreen();
		System.exit(0);
	}

	public static void main(String[] args) throws IOException, InterruptedException, StartStoppException {

		Properties themeProperties = new Properties();
		try (InputStream stream = StartStoppConsole.class.getResourceAsStream("nerz-mono.properties")) {
			themeProperties.load(stream);
			LanternaThemes.registerTheme("NERZ-Mono", new StartStoppTheme(themeProperties));
		}

		Injector injector = Guice.createInjector(new StartStoppModule(args));
		injector.getInstance(StartStoppConsole.class);
	}
}
