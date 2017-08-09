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

package de.bsvrz.sys.startstopp.console.ui.online;

import java.util.Arrays;

import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.GridLayout.Alignment;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.input.KeyStroke;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.console.StartStoppConsole;
import de.bsvrz.sys.startstopp.console.ui.InfoDialog;
import de.bsvrz.sys.startstopp.console.ui.MenuLabel;
import de.bsvrz.sys.startstopp.console.ui.MenuPanel;
import de.bsvrz.sys.startstopp.console.ui.TerminalCloseAction;
import de.bsvrz.sys.startstopp.console.ui.editor.SkriptEditor;

public class StartStoppOnlineWindow extends BasicWindow {


	private OnlineInkarnationTable table;

	public StartStoppOnlineWindow() throws StartStoppException {
		super("StartStopp - Online");

		this.table = new OnlineInkarnationTable();
		this.table.setSelectAction(()->handleApplikation());

		setHints(Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS));

		Panel panel = new Panel();
		panel.setLayoutManager(new GridLayout(1));
		panel.setLayoutData(GridLayout.createLayoutData(Alignment.BEGINNING, Alignment.BEGINNING, true, true));

		Label infoLabel = new Label("Startstopp - Online");
		panel.addComponent(infoLabel.withBorder(Borders.singleLine()));
		infoLabel.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));

		panel.addComponent(table.withBorder(Borders.singleLine()), GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.FILL, true, true));

		MenuPanel menuPanel = new MenuPanel();
		panel.setLayoutManager(new GridLayout(1));
		Label statusLabel = new MenuLabel("s-System   p-Prozess   t-Theme   e-Editieren   i-Info");
		menuPanel.addComponent(statusLabel, GridLayout.createHorizontallyFilledLayoutData(1));
		panel.addComponent(menuPanel, GridLayout.createHorizontallyFilledLayoutData(1));
		
		setComponent(panel);
	}

	@Override
	public boolean handleInput(KeyStroke keyStroke) {

		switch (keyStroke.getKeyType()) {
		case Character:
			switch (keyStroke.getCharacter()) {
			case 't':
				ActionListDialogBuilder builder = new ActionListDialogBuilder().setTitle("Theme")
						.setDescription("Theme auswählen");
				for (String theme : LanternaThemes.getRegisteredThemes()) {
					builder.addAction(theme, new Runnable() {
						@Override
						public void run() {
							getTextGUI().setTheme(LanternaThemes.getRegisteredTheme(theme));
						}
					});
				}
				;
				builder.build().showDialog(getTextGUI());
				return true;

			case 's':
				builder = new ActionListDialogBuilder().setTitle("System");
				builder.addAction(new StartStoppStoppAction());
				builder.addAction(new StartStoppRestartAction());
				builder.addAction(new StartStoppExitAction());
				builder.addAction(new TerminalCloseAction());
				builder.build().showDialog(getTextGUI());
				return true;

			case 'p':
				handleApplikation();
				return true;

			case 'e':
				try {
					getTextGUI().addWindow(new SkriptEditor(StartStoppConsole.getClient().getCurrentSkript()));
				} catch (StartStoppException e) {
					new InfoDialog("FEHLER", e.getLocalizedMessage()).display();
				}

				return true;

			default:
				System.err.println(getClass().getSimpleName() + ": " + keyStroke);
				break;
			}
			break;

		case Escape:
			close();
			break;

		default:
			break;
		}

		return super.handleInput(keyStroke);
	}

	private void handleApplikation() {
		ActionListDialogBuilder builder;
		Applikation applikation = table.getSelectedApplikation();
		if (applikation != null) {
			builder = new ActionListDialogBuilder().setTitle("Applikation");
			builder.addAction(new ApplikationStartAction(applikation));
			builder.addAction(new ApplikationRestartAction(applikation));
			builder.addAction(new ApplikationStoppAction(applikation));
			builder.addAction(new ApplikationDetailAction(applikation));
			builder.build().showDialog(getTextGUI());
		}
	}
}