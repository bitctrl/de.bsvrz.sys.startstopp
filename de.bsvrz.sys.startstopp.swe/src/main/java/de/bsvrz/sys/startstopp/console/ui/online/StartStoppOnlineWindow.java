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

import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.googlecode.lanterna.TextColor;
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

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkriptStatus;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.console.StartStoppConsole;
import de.bsvrz.sys.startstopp.console.ui.InfoDialog;
import de.bsvrz.sys.startstopp.console.ui.MenuLabel;
import de.bsvrz.sys.startstopp.console.ui.MenuPanel;
import de.bsvrz.sys.startstopp.console.ui.TerminalCloseAction;
import de.bsvrz.sys.startstopp.console.ui.editor.SkriptEditor;

public class StartStoppOnlineWindow extends BasicWindow {

	private final class Updater extends Thread {

		private Updater() {
			super("OnlineWindowUpdater");
			setDaemon(true);
		}

		public void run() {

			while (true) {
				try {
					List<Applikation> applikationen = StartStoppConsole.getClient().getApplikationen();

					if (applikationen.isEmpty()) {
						StartStoppSkriptStatus skriptStatus = StartStoppConsole.getClient().getCurrentSkriptStatus();
						if (skriptStatus.getStatus() == StartStoppSkriptStatus.Status.FAILURE) {
							onlineDisplay.setStatus(OnlineDisplay.Status.SKRIPT_FEHLER);
						}
					}

					table.updateApplikationen(applikationen);
					onlineDisplay.setStatus(OnlineDisplay.Status.ONLINE);
				} catch (StartStoppException e) {
					table.updateApplikationen(Collections.emptyList());
					onlineDisplay.setStatus(OnlineDisplay.Status.VERBINDUNG_FEHLER);
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					LOGGER.warning(e.getLocalizedMessage());
				}
			}
		}
	}

	public static class OnlineDisplay extends Panel {

		private OnlineStatusLabel verbindungsStatus;
		private Label letzteAbfrage;

		enum Status {
			UNKNOWN("-", TextColor.ANSI.WHITE),
			SKRIPT_FEHLER("S", TextColor.ANSI.BLUE),
			VERBINDUNG_FEHLER("X", TextColor.ANSI.RED),
			ONLINE("O", TextColor.ANSI.GREEN);

			private String text;
			private TextColor color;

			private Status(String text, TextColor color) {
				this.text = text;
				this.color = color;
			}
		};

		public OnlineDisplay() {
			setLayoutManager(new GridLayout(3));
			addComponent(new Label("Startstopp - Online"), GridLayout.createHorizontallyFilledLayoutData(1));
			letzteAbfrage = new Label("00.00.0000 00:00:00");
			addComponent(letzteAbfrage);
			verbindungsStatus = new OnlineStatusLabel();
			addComponent(verbindungsStatus);
		}

		public void setStatus(Status status) {
			letzteAbfrage.setText(DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis())));
			verbindungsStatus.setBackgroundColor(status.color);
			verbindungsStatus.setText(status.text);
		}
	}

	private static final Debug LOGGER = Debug.getLogger();
	private OnlineInkarnationTable table;
	private List<Applikation> applikationen = new ArrayList<>();
	private OnlineDisplay onlineDisplay;

	public StartStoppOnlineWindow() throws StartStoppException {
		super("StartStopp - Online");

		this.table = new OnlineInkarnationTable(applikationen);
		table.setEditierbar(false);

		setHints(Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS));

		Panel panel = new Panel();
		panel.setLayoutManager(new GridLayout(1));
		panel.setLayoutData(GridLayout.createLayoutData(Alignment.BEGINNING, Alignment.BEGINNING, true, true));

		onlineDisplay = new OnlineDisplay();
		panel.addComponent(onlineDisplay.withBorder(Borders.singleLine()),
				GridLayout.createHorizontallyFilledLayoutData(1));
		panel.addComponent(table.withBorder(Borders.singleLine()),
				GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.FILL, true, true));

		MenuPanel menuPanel = new MenuPanel();
		panel.setLayoutManager(new GridLayout(1));
		Label statusLabel = new MenuLabel("s-System  p-ENTER t-Theme   e-Editieren   i-Info");
		menuPanel.addComponent(statusLabel, GridLayout.createHorizontallyFilledLayoutData(1));
		panel.addComponent(menuPanel, GridLayout.createHorizontallyFilledLayoutData(1));

		new Updater().start();

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
				builder.addAction(new StartStoppStartAction());
				builder.addAction(new StartStoppRestartAction());
				builder.addAction(new StartStoppExitAction());
				builder.addAction(new TerminalCloseAction());
				builder.build().showDialog(getTextGUI());
				return true;

			case 'e':
				try {
					getTextGUI().addWindow(new SkriptEditor(StartStoppConsole.getClient().getCurrentSkript()));
				} catch (StartStoppException e) {
					new InfoDialog("FEHLER", e.getLocalizedMessage()).display();
				}

				return true;

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

		return super.handleInput(keyStroke);
	}
}