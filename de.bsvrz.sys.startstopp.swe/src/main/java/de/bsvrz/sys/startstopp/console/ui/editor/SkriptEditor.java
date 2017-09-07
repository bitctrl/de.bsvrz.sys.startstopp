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

package de.bsvrz.sys.startstopp.console.ui.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Border;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.GridLayout.Alignment;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowListenerAdapter;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import de.bsvrz.sys.startstopp.api.jsonschema.Global;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;
import de.bsvrz.sys.startstopp.api.jsonschema.ZugangDav;
import de.bsvrz.sys.startstopp.console.ui.InfoDialog;
import de.bsvrz.sys.startstopp.console.ui.MenuLabel;
import de.bsvrz.sys.startstopp.console.ui.MenuPanel;

public final class SkriptEditor extends BasicWindow {

	private StartStoppSkript skript;

	private Border currentTableBorder;
	private Panel panel;
	private MenuPanel menuPanel;

	public SkriptEditor(StartStoppSkript skript) {
		super("StartStopp - Editor");
		this.skript = (StartStoppSkript) Util.cloneObject(skript);
		init();
		
		skriptVervollstaendigen();
		
	}

	private void skriptVervollstaendigen() {
		if( skript.getGlobal() == null) {
			skript.setGlobal(new Global());
		}
		if( skript.getGlobal().getZugangDav() == null) {
			skript.getGlobal().setZugangDav(new ZugangDav());
		}
	}

	private void init() {
		setHints(Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS));

		panel = new Panel();
		panel.setLayoutManager(new GridLayout(1));
		panel.setLayoutData(GridLayout.createLayoutData(Alignment.BEGINNING, Alignment.BEGINNING, true, true));

		Label infoLabel = new Label("Startstopp - Editor");
		panel.addComponent(infoLabel.withBorder(Borders.singleLine()));
		infoLabel.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));

		menuPanel = new MenuPanel();
		menuPanel.setLayoutManager(new GridLayout(1));
		Label statusLabel = new MenuLabel("s-System");
		menuPanel.addComponent(statusLabel, GridLayout.createHorizontallyFilledLayoutData(1));

		addWindowListener(new WindowListenerAdapter() {
			@Override
			public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
				if (currentTableBorder != null) {
					Table<?> table = (Table<?>) currentTableBorder.getChildren().iterator().next();
					table.setVisibleRows(newSize.getRows() - 7);
				}
			}
		});

		setComponent(panel);
		showInkarnationTable();
	}

	private void showInkarnationTable() {

		InkarnationTable table = new InkarnationTable(skript);
		table.setLayoutData(
				GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.FILL, true, true));
		table.setPreferredSize(TerminalSize.ONE);
		if (currentTableBorder != null) {
			panel.removeComponent(currentTableBorder);
			panel.removeComponent(menuPanel);
		}
		currentTableBorder = table.withBorder(Borders.singleLine());
		panel.addComponent(currentTableBorder);
		panel.addComponent(menuPanel, GridLayout.createHorizontallyFilledLayoutData(1));

		setFocusedInteractable(table);
	}

	private void showMakroTable() {
		MakroTable table = new MakroTable(getTextGUI(), skript);
		table.setLayoutData(
				GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.FILL, true, true));
		table.setPreferredSize(TerminalSize.ONE);
		if (currentTableBorder != null) {
			panel.removeComponent(currentTableBorder);
			panel.removeComponent(menuPanel);
		}
		currentTableBorder = table.withBorder(Borders.singleLine());
		panel.addComponent(currentTableBorder);
		panel.addComponent(menuPanel, GridLayout.createHorizontallyFilledLayoutData(1));

		setFocusedInteractable(table);
	}

	private void showRechnerTable() {
		RechnerTable table = new RechnerTable(skript);
		table.setLayoutData(
				GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.FILL, true, true));
		table.setPreferredSize(TerminalSize.ONE);

		if (currentTableBorder != null) {
			panel.removeComponent(currentTableBorder);
			panel.removeComponent(menuPanel);
		}
		currentTableBorder = table.withBorder(Borders.singleLine());
		panel.addComponent(currentTableBorder);
		panel.addComponent(menuPanel, GridLayout.createHorizontallyFilledLayoutData(1));

		setFocusedInteractable(table);
	}

	@Override 
	public boolean handleInput(KeyStroke key) {

		switch (key.getKeyType()) {
		case Character:
			switch (key.getCharacter()) {
			case 's':
				showSystemActionMenu();
				return true;

			case 'm':
				showMakroTable();
				return true;

			case 'i':
				showInkarnationTable();
				return true;

			case 'r':
				showRechnerTable();
				return true;

			case 'l':
				loadSkriptFromFile();
				return true;

			case 'k':
				showKernsystemEditor();
				return true;

			case 'u':
				showUsvEditor();
				return true;

			case 'z':
				showZugangDavEditor();
				return true;

			default:
				break;
			}
			break;

		default:
			break;
		}

		return super.handleInput(key);
	}

	private void showZugangDavEditor() {
		ZugangDavEditor zugangDavEditor = new ZugangDavEditor(skript);
		if (zugangDavEditor.showDialog(getTextGUI())) {
			skript.getGlobal().setZugangDav(zugangDavEditor.getElement());
		}
	}

	private void showUsvEditor() {
		UsvEditor usvEditor = new UsvEditor(skript);
		if (usvEditor.showDialog(getTextGUI())) {
			skript.getGlobal().setUsv(usvEditor.getElement());
		}
	}

	private void showKernsystemEditor() {
		KernsystemEditor ksEditor = new KernsystemEditor(skript);
		if (ksEditor.showDialog(getTextGUI())) {
			skript.getGlobal().getKernsysteme().clear();
			skript.getGlobal().getKernsysteme().addAll(ksEditor.getElement());
		}
	}

	private void showSystemActionMenu() {
		ActionListDialogBuilder builder = new ActionListDialogBuilder().setTitle("System");
		builder.addActions(new EditorVersionierenAction(this, skript), new EditorSichernAction(skript),
				new EditorCloseAction(this));
		builder.build().showDialog(getTextGUI());
	}

	private void loadSkriptFromFile() {
		FileDialogBuilder fileDialogBuilder = new FileDialogBuilder();
		fileDialogBuilder.setTitle("StartStopp-Konfiguration auswählen");
		fileDialogBuilder.setActionLabel("Laden");
		File selectedFile = fileDialogBuilder.build().showDialog(getTextGUI());
		if ((selectedFile != null) && selectedFile.exists()) {
			try (InputStream stream = new FileInputStream(selectedFile)) {
				ObjectMapper mapper = new ObjectMapper();
				skript = mapper.readValue(stream, StartStoppSkript.class);
			} catch (IOException e) {
				new InfoDialog("FEHLER", e.getLocalizedMessage()).display();
			}
		}
	}

	public static boolean isDeleteKey(KeyStroke key) {
		return key.getKeyType() == KeyType.Delete && key.isAltDown();
	}

	public static boolean isInsertAfterKey(KeyStroke key) {
		return key.getKeyType() == KeyType.Insert && key.isAltDown() && !key.isShiftDown();
	}

	public static boolean isInsertBeforeKey(KeyStroke key) {
		return key.getKeyType() == KeyType.Insert && key.isAltDown() && key.isShiftDown();
	}

	public static boolean isEintragNachObenKey(KeyStroke key) {
		return key.getKeyType() == KeyType.ArrowUp && key.isAltDown();
	}

	public static boolean isEintragNachUntenKey(KeyStroke key) {
		return key.getKeyType() == KeyType.ArrowDown && key.isAltDown();
	}

	public static boolean isSelectMakroKey(KeyStroke key) {
		return key.getKeyType() == KeyType.Character && key.isAltDown() && key.getCharacter() == 'm';
	}
}