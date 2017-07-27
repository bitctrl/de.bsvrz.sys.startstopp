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

package de.bsvrz.sys.startstopp.console.ui;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.AbstractComponent;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Border;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.GridLayout.Alignment;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowListener;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.input.KeyStroke;

import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.console.StartStoppConsole;

public class StartStoppEditWindow extends BasicWindow implements WindowListener {
	private InkarnationTable inkarnationTable;
	private StartStoppSkript skript;
	private MakroTable makroTable;
	



	public StartStoppEditWindow() throws StartStoppException {
		super("StartStopp - Editor");

		setHints(Arrays.asList(Window.Hint.FULL_SCREEN));
		skript = StartStoppConsole.getInstance().getClient()
				.getCurrentSkript();
		
		showInkarnationTable();
		addWindowListener(this);
	}

	private void showInkarnationTable() throws StartStoppException {
		Panel panel = new Panel();
		panel.setLayoutManager(new GridLayout(1));
		panel.setLayoutData(GridLayout.createLayoutData(Alignment.BEGINNING, Alignment.BEGINNING, true, true));

		Label infoLabel = new Label("s-System   i-Inkarnation e-Bearbeiten");
		panel.addComponent(infoLabel.withBorder(Borders.singleLine()));
		infoLabel.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));

		inkarnationTable = new InkarnationTable(skript);
		inkarnationTable.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.FILL, true, true));
		inkarnationTable.setPreferredSize(TerminalSize.ONE);
		panel.addComponent(inkarnationTable.withBorder(Borders.singleLine()));
	
		setComponent(panel);
		
	}

	private void showMakroTable() throws StartStoppException {
		Panel panel = new Panel();
		panel.setLayoutManager(new GridLayout(1));
		panel.setLayoutData(GridLayout.createLayoutData(Alignment.BEGINNING, Alignment.BEGINNING, true, true));

		Label infoLabel = new Label("s-System   i-Inkarnation e-Bearbeiten");
		panel.addComponent(infoLabel.withBorder(Borders.singleLine()));
		infoLabel.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));

		makroTable = new MakroTable(skript);
		makroTable.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.FILL, true, true));
		makroTable.setPreferredSize(TerminalSize.ONE);

		panel.addComponent(makroTable.withBorder(Borders.singleLine()));
		setComponent(panel);

		makroTable.setVisibleRows(getSize().getRows() - 7);
	}
	
	@Override
	public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
		// TODO Auto-generated method stub
		switch (keyStroke.getKeyType()) {
		case Character:
 			switch (keyStroke.getCharacter()) {
			case 's':
				ActionListDialogBuilder builder = new ActionListDialogBuilder().setTitle("System");
				builder.addActions(new EditorSaveAction(), new EditorCloseAction(this));
				builder.build().showDialog(getTextGUI());
				break;

			case 'm':
				try {
					showMakroTable();
				} catch (StartStoppException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
				
			case 'i':
				try {
					showInkarnationTable();
				} catch (StartStoppException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;

			default:
				break;
			}
			break;

		case Escape:
			close();
			break;
		default:
			break;
		}
	}

	@Override
	public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
		inkarnationTable.setVisibleRows(newSize.getRows() - 2);

	}

	@Override
	public void onMoved(Window window, TerminalPosition oldPosition, TerminalPosition newPosition) {
		// TODO Auto-generated method stub

	}
}