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
		System.err.println("Term: " + term.getClass());
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
