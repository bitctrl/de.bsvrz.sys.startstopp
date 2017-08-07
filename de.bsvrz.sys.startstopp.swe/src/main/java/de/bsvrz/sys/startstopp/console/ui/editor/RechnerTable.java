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

import java.util.Arrays;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyStroke;

import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.console.ui.GuiComponentFactory;
import de.bsvrz.sys.startstopp.console.ui.JaNeinDialog;

public class RechnerTable extends Table<String> {

	@Inject
	private GuiComponentFactory factory;

	private StartStoppSkript skript;
	private WindowBasedTextGUI gui;

	@Inject
	public RechnerTable(WindowBasedTextGUI gui, @Assisted StartStoppSkript skript) {
		super("Name", "Host", "Port");
		this.skript = skript;
		this.gui = gui;

		setSelectAction(new Runnable() {
			@Override
			public void run() {
				int row = getSelectedRow();
				Rechner rechner = skript.getGlobal().getRechner().get(row);
				RechnerEditor editor = factory.createRechnerEditor(skript, rechner);
				if (editor.showDialog(gui)) {
					getTableModel().setCell(0, row, editor.getElement().getName());
					getTableModel().setCell(1, row, editor.getElement().getTcpAdresse());
					getTableModel().setCell(2, row, editor.getElement().getPort());
				}
			}
		});

		this.skript = skript;
		for (Rechner rechner : skript.getGlobal().getRechner()) {
			getTableModel().addRow(rechner.getName(), rechner.getTcpAdresse(), rechner.getPort());
		}
	}

	@Override
	public Result handleKeyStroke(KeyStroke keyStroke) {
		if (SkriptEditor.isInsertAfterKey(keyStroke)) {
			int row = getSelectedRow() + 1;
			Rechner rechner = new Rechner();
			rechner.setName("Neuer Rechner");
			rechner.setTcpAdresse("");
			rechner.setPort("");
			RechnerEditor editor = factory.createRechnerEditor(skript, rechner);
			if (editor.showDialog(gui)) {
				skript.getGlobal().getRechner().add(row, editor.getElement());
				getTableModel().insertRow(row, Arrays.asList(editor.getElement().getName(),
						editor.getElement().getTcpAdresse(), editor.getElement().getPort()));
			}
		} else if (SkriptEditor.isDeleteKey(keyStroke)) {
			JaNeinDialog dialog = factory.createJaNeinDialog("Rechner löschen", "Soll der ausgewählte Rechner wirklich gelöscht werden?");
			if (dialog.display()) {
				int deleteRow = getSelectedRow();
				skript.getGlobal().getRechner().remove(deleteRow);
				getTableModel().removeRow(deleteRow);
			}
		}

		return super.handleKeyStroke(keyStroke);
	}
}
