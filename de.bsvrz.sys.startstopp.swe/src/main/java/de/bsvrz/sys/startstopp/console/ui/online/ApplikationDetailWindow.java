package de.bsvrz.sys.startstopp.console.ui.online;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Panels;
import com.googlecode.lanterna.input.KeyStroke;

public class ApplikationDetailWindow extends BasicWindow {

	private Panel panel;

	public ApplikationDetailWindow(String string) {
		super(string);
		setCloseWindowWithEscape(true);
		initUI();
	}

	private void initUI() {
		
		List<Component> components = new ArrayList<>();
		for(int i = 0; i < 30; i++) {
			Label label = new Label("Zeile mit\nZeilennummer: " + i);
			components.add(label);
		}
		
		panel = Panels.vertical(components.toArray(new Component[components.size()]));
		setComponent(panel);
	}
	
	@Override
	public boolean handleInput(KeyStroke key) {
		System.err.println("Size: " +  getSize() + " Panelsize: " + panel.getSize());
		TerminalPosition altePosition = panel.getPosition();
		TerminalPosition neuePosition = new TerminalPosition(0, altePosition.getRow() - 1);
		panel.setPosition(neuePosition);
		
		System.err.println("Alt: " + altePosition + " Neu: " + neuePosition);
		panel.invalidate();
		System.err.println("Preferred: " + panel.calculatePreferredSize());
		
		return super.handleInput(key);
	}
}
