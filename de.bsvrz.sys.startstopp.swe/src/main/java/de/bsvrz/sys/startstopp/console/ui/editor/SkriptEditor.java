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

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.GridLayout.Alignment;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowListenerAdapter;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.console.ui.GuiComponentFactory;

public class SkriptEditor extends BasicWindow {
	private InkarnationTable inkarnationTable;
	private StartStoppSkript skript;
	private MakroTable makroTable;
	private RechnerTable rechnerTable;

	@Inject
	GuiComponentFactory factory;

	@Inject
	public SkriptEditor(@Assisted StartStoppSkript skript) {
		super("StartStopp - Editor");
		this.skript = (StartStoppSkript) Util.cloneObject(skript);
	}

	@PostConstruct
	@Inject
	void init() {
		setHints(Arrays.asList(Window.Hint.FULL_SCREEN));

		try {
			showInkarnationTable();
		} catch (StartStoppException e) {
			Debug.getLogger().warning(e.getLocalizedMessage());
		}
		addWindowListener(new WindowListenerAdapter() {
			@Override
			public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
				if (inkarnationTable != null) {
					inkarnationTable.setVisibleRows(newSize.getRows() - 2);
				}
			}
		});
	}

	private void showInkarnationTable() throws StartStoppException {
		Panel panel = new Panel();
		panel.setLayoutManager(new GridLayout(1));
		panel.setLayoutData(GridLayout.createLayoutData(Alignment.BEGINNING, Alignment.BEGINNING, true, true));

		Label infoLabel = new Label("s-System   i-Inkarnation e-Bearbeiten");
		panel.addComponent(infoLabel.withBorder(Borders.singleLine()));
		infoLabel.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));

		inkarnationTable = factory.createInkarnationTable(skript);
		inkarnationTable.setLayoutData(
				GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.FILL, true, true));
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

		makroTable = factory.createMakroTable(skript);
		makroTable.setLayoutData(
				GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.FILL, true, true));
		makroTable.setPreferredSize(TerminalSize.ONE);

		panel.addComponent(makroTable.withBorder(Borders.singleLine()));
		setComponent(panel);

		makroTable.setVisibleRows(getSize().getRows() - 7);
	}

	private void showRechnerTable() throws StartStoppException {
		Panel panel = new Panel();
		panel.setLayoutManager(new GridLayout(1));
		panel.setLayoutData(GridLayout.createLayoutData(Alignment.BEGINNING, Alignment.BEGINNING, true, true));

		Label infoLabel = new Label("s-System   i-Inkarnation e-Bearbeiten");
		panel.addComponent(infoLabel.withBorder(Borders.singleLine()));
		infoLabel.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));

		rechnerTable = factory.createRechnerTable(skript);
		rechnerTable.setLayoutData(
				GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.FILL, true, true));
		rechnerTable.setPreferredSize(TerminalSize.ONE);

		panel.addComponent(rechnerTable.withBorder(Borders.singleLine()));
		setComponent(panel);

		rechnerTable.setVisibleRows(getSize().getRows() - 7);
	}

	@Override
	public boolean handleInput(KeyStroke key) {

		switch (key.getKeyType()) {
		case Character:
			switch (key.getCharacter()) {
			case 's':
				ActionListDialogBuilder builder = new ActionListDialogBuilder().setTitle("System");
				builder.addActions(factory.createVersionierenAction(skript), factory.createSichernAction(skript), factory.createEditorCloseAction(this));
				builder.build().showDialog(getTextGUI());
				return true;

			case 'm':
				try {
					showMakroTable();
				} catch (StartStoppException e) {
					factory.createInfoDialog("FEHLER", e.getLocalizedMessage()).display();
				}
				return true;

			case 'i':
				try {
					showInkarnationTable();
				} catch (StartStoppException e) {
					factory.createInfoDialog("FEHLER", e.getLocalizedMessage()).display();
				}
				return true;

			case 'r':
				try {
					showRechnerTable();
				} catch (StartStoppException e) {
					factory.createInfoDialog("FEHLER", e.getLocalizedMessage()).display();
				}
				return true;

			case 'l':
				FileDialogBuilder fileDialogBuilder = new FileDialogBuilder();
				fileDialogBuilder.setTitle("StartStopp-Konfiguration auswählen");
				fileDialogBuilder.setActionLabel("Laden");
				File selectedFile = fileDialogBuilder.build().showDialog(getTextGUI());
				if ((selectedFile != null) && selectedFile.exists()) {
					try (InputStream stream = new FileInputStream(selectedFile)) {
						ObjectMapper mapper = new ObjectMapper();
						skript = mapper.readValue(stream, StartStoppSkript.class);
					} catch (IOException e) {
						factory.createInfoDialog("FEHLER", e.getLocalizedMessage()).display();
					}
				}
				return true;

			case 'k':
				KernsystemEditor ksEditor = factory.createKernsystemEditor(skript);
				if (ksEditor.showDialog(getTextGUI())) {
					skript.getGlobal().getKernsysteme().clear();
					skript.getGlobal().getKernsysteme().addAll(ksEditor.getElement());
				}
				return true;

			case 'u':
				UsvEditor usvEditor = factory.createUsvEditor(skript);
				if (usvEditor.showDialog(getTextGUI())) {
					skript.getGlobal().setUsv(usvEditor.getElement());
				}
				return true;

			case 'z':
				ZugangDavEditor zugangDavEditor = factory.createZugangDavEditor(skript);
				if (zugangDavEditor.showDialog(getTextGUI())) {
					skript.getGlobal().setZugangDav(zugangDavEditor.getElement());
				}
				return true;

			default:
				System.err.println(getClass().getSimpleName() + ": " + key);
				break;
			}
			break;

		default:
			break;
		}

		return super.handleInput(key);
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