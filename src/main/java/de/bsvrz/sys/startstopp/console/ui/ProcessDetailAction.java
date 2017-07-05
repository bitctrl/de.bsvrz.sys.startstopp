package de.bsvrz.sys.startstopp.console.ui;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.Window.Hint;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.WindowListenerAdapter;
import com.googlecode.lanterna.input.KeyStroke;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;

public class ProcessDetailAction implements Runnable {

	private WindowBasedTextGUI gui;
	private Applikation inkarnation;

	public ProcessDetailAction(WindowBasedTextGUI gui, Applikation inkarnation) {
		this.gui = gui;
		this.inkarnation = inkarnation;
	}

	@Override
	public void run() {
		BasicWindow window = new BasicWindow("Details: " + inkarnation.getInkarnationsName());
		window.setHints(Collections.singleton(Hint.EXPANDED));
		window.addWindowListener(new WindowListenerAdapter() {
			@Override
			public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
				window.close();
			}
		});
		gui.addWindow(window);
	}

	@Override
	public String toString() {
		return "Details anzeigen";
	}
}
