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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.GridLayout.Alignment;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowListenerAdapter;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialog;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.input.KeyStroke;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus;
import de.bsvrz.sys.startstopp.console.StartStoppConsole;
import de.bsvrz.sys.startstopp.console.ui.JaNeinDialog;
import de.bsvrz.sys.startstopp.console.ui.MenuLabel;
import de.bsvrz.sys.startstopp.console.ui.MenuPanel;
import de.bsvrz.sys.startstopp.console.ui.StartStoppInfoDialog;
import de.bsvrz.sys.startstopp.console.ui.TerminalCloseAction;
import de.bsvrz.sys.startstopp.console.ui.editor.SkriptEditor;

public class StartStoppOnlineWindow extends BasicWindow {

	private final class Updater implements Runnable {

		@Override
		public void run() {

			try {
				table.updateApplikationen(StartStoppConsole.getClient().getApplikationen());
			} catch (StartStoppException e) {
				LOGGER.fine(e.getLocalizedMessage());
				table.updateApplikationen(Collections.emptyList());
			}

			try {
				onlineDisplay.setStatus(StartStoppConsole.getClient().getStartStoppStatus());
			} catch (StartStoppException e) {
				LOGGER.fine(e.getLocalizedMessage());
				onlineDisplay.setStatus(null);
			}
		}
	}

	public static class OnlineDisplay extends Panel {

		private Label betriebsMeldungsStatusLabel = new Label("BM: OFF");
		private OnlineStatusLabel verbindungsStatus;
		private Label letzteAbfrage;

		public OnlineDisplay() {
			setLayoutManager(new GridLayout(4));
			addComponent(new Label("Startstopp - Online"), GridLayout.createHorizontallyFilledLayoutData(1));
			letzteAbfrage = new Label("00.00.0000 00:00:00");
			addComponent(letzteAbfrage);
			verbindungsStatus = new OnlineStatusLabel();
			addComponent(verbindungsStatus);
			addComponent(betriebsMeldungsStatusLabel);
		}

		public void setStatus(StartStoppStatus startStoppStatus) {
			letzteAbfrage.setText(DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis())));
			if (startStoppStatus == null) {
				verbindungsStatus.setForegroundColor(TextColor.ANSI.WHITE);
				verbindungsStatus.setBackgroundColor(TextColor.ANSI.RED);
				verbindungsStatus.setText("XXXXX");
				betriebsMeldungsStatusLabel.setText("BM: ???");
			} else {
				verbindungsStatus.setForegroundColor(getForegroundColor(startStoppStatus));
				verbindungsStatus.setBackgroundColor(getBackgroundColor(startStoppStatus));
				verbindungsStatus.setText(startStoppStatus.getStatus().name());
				if( startStoppStatus.getBetriebsmeldungen()) {
					betriebsMeldungsStatusLabel.setText("BM: EIN");
				} else {
					betriebsMeldungsStatusLabel.setText("BM: AUS");
				}
			}
		}

		private TextColor getForegroundColor(StartStoppStatus startStoppStatus) {

			TextColor result = TextColor.ANSI.RED;

			switch (startStoppStatus.getStatus()) {
			case CONFIGERROR:
				result = TextColor.ANSI.WHITE;
				break;
			case INITIALIZED:
				result = TextColor.ANSI.WHITE;
				break;
			case RUNNING:
				result = TextColor.ANSI.WHITE;
				break;
			case RUNNING_CANCELED:
				result = TextColor.ANSI.WHITE;
				break;
			case SHUTDOWN:
				result = TextColor.ANSI.WHITE;
				break;
			case STOPPED:
				result = TextColor.ANSI.WHITE;
				break;
			case STOPPING:
				result = TextColor.ANSI.WHITE;
				break;
			case STOPPING_CANCELED:
				result = TextColor.ANSI.WHITE;
				break;
			default:
				break;
			}
			return result;
		}

		private TextColor getBackgroundColor(StartStoppStatus startStoppStatus) {

			TextColor result = TextColor.ANSI.RED;

			switch (startStoppStatus.getStatus()) {
			case CONFIGERROR:
				result = TextColor.ANSI.BLUE;
				break;
			case INITIALIZED:
				result = TextColor.ANSI.BLUE;
				break;
			case RUNNING:
				result = TextColor.ANSI.GREEN;
				break;
			case RUNNING_CANCELED:
				result = TextColor.ANSI.RED;
				break;
			case SHUTDOWN:
				result = TextColor.ANSI.RED;
				break;
			case STOPPED:
				result = TextColor.ANSI.RED;
				break;
			case STOPPING:
				result = TextColor.ANSI.RED;
				break;
			case STOPPING_CANCELED:
				result = TextColor.ANSI.RED;
				break;
			default:
				break;
			}
			return result;
		}
	}

	private static final Debug LOGGER = Debug.getLogger();
	private OnlineInkarnationTable table;
	private List<Applikation> applikationen = new ArrayList<>();
	private OnlineDisplay onlineDisplay;

	public StartStoppOnlineWindow() {
		super("StartStopp - Online");

		// setCloseWindowWithEscape(false);

		this.table = new OnlineInkarnationTable(applikationen);
		table.setEditierbar(false);

		addWindowListener(new WindowListenerAdapter() {
			@Override
			public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
				table.setVisibleRows(newSize.getRows() - 7);
			}
		});

		
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
		Label statusLabel = new MenuLabel("s-System  ENTER-Applikation t-Theme   e-Editieren   i-Info");
		menuPanel.addComponent(statusLabel, GridLayout.createHorizontallyFilledLayoutData(1));
		panel.addComponent(menuPanel, GridLayout.createHorizontallyFilledLayoutData(1));

		Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread thread = Executors.defaultThreadFactory().newThread(r);
				thread.setDaemon(true);
				return thread;
			}
		}).scheduleAtFixedRate(new Updater(), 0, 2, TimeUnit.SECONDS);

		setComponent(panel);
	}

	@Override
	public boolean handleInput(KeyStroke keyStroke) {

		switch (keyStroke.getKeyType()) {
		case Character:
			switch (keyStroke.getCharacter()) {
			case 't':
				return handleThemeSelection();

			case 's':
				return handleSystemFunktion();

			case 'i':
				new StartStoppInfoDialog().display();
				return true;

				
			case 'e':
				try {
					getTextGUI().addWindow(new SkriptEditor(StartStoppConsole.getClient().getCurrentSkript()));
				} catch (StartStoppException e) {
					if (new JaNeinDialog("FEHLER",
							e.getLocalizedMessage() + "\nSoll eine leere Konfiguration angelegt werden?").display()) {
						getTextGUI().addWindow(new SkriptEditor(new StartStoppSkript()));
					}
				}

				return true;

			default:
				break;
			}
			break;

		case Escape:
			if (new JaNeinDialog("INFO", "Benutzeroberfläche schließen").display()) {
				close();
			}
			break;

		default:
			break;
		}

		return super.handleInput(keyStroke);
	}

	private boolean handleSystemFunktion() {
		ActionListDialogBuilder builder;
		builder = new ActionListDialogBuilder().setTitle("System-Funktionen").setCanCancel(false);
		builder.addAction(new StartStoppStoppAction());
		builder.addAction(new StartStoppStartAction());
		builder.addAction(new StartStoppRestartAction());
		builder.addAction(new StartStoppBetriebsmeldungenUmschaltenAction());
		builder.addAction(new StartStoppExitAction());
		builder.addAction(new TerminalCloseAction());
		builder.setDescription("ESC - Abbrechen");
		ActionListDialog dialog = builder.build();
		dialog.setCloseWindowWithEscape(true);
		dialog.showDialog(getTextGUI());
		return true;
	}

	private boolean handleThemeSelection() {
		ActionListDialogBuilder builder = new ActionListDialogBuilder().setTitle("Theme").setCanCancel(false)
				.setDescription("Theme auswählen");
		for (String theme : LanternaThemes.getRegisteredThemes()) {
			builder.addAction(theme, new Runnable() {
				@Override
				public void run() {
					getTextGUI().setTheme(LanternaThemes.getRegisteredTheme(theme));
				}
			});
		}

		builder.setDescription("ESC - Abbrechen");
		ActionListDialog dialog = builder.build();
		dialog.setCloseWindowWithEscape(true);
		dialog.showDialog(getTextGUI());
		return true;
	}
}