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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialogBuilder;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyStroke;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;

public class AufrufParameterEditor extends StartStoppElementEditor<List<String>> {

	private List<String> parameterListe = new ArrayList<>();
	private Table<String> parameterTable;

	@Inject
	public AufrufParameterEditor(@Assisted Inkarnation inkarnation) {
		super("Aufrufparameter");
		this.parameterListe.addAll(inkarnation.getAufrufParameter());
	}

	protected void initComponents(Panel mainPanel) {

		mainPanel.setLayoutManager(new GridLayout(1).setLeftMarginSize(1).setRightMarginSize(1));

		parameterTable = new Table<>("Aufrufparameter");
		for (String parameter : parameterListe) {
			parameterTable.getTableModel().addRow(parameter);
		}
		
		parameterTable.setSelectAction(new Runnable() {
			
			@Override
			public void run() {
				TextInputDialogBuilder builder = new TextInputDialogBuilder();
				builder.setTitle("Parameter").setDescription("Parameter bearbeiten:").setInitialContent(parameterListe.get(parameterTable.getSelectedRow()));
				String newParameter = builder.build().showDialog(getTextGUI());
				if (newParameter != null) {
					int row = parameterTable.getSelectedRow();
					parameterListe.remove(row);
					parameterListe.add(row, newParameter);
					parameterTable.getTableModel().setCell(0, row, newParameter);
				}
			}
		});
		
		mainPanel.addComponent(parameterTable, GridLayout.createHorizontallyFilledLayoutData(1));
	}


	@Override
	public boolean handleInput(KeyStroke key) {
		if (parameterTable.isFocused()) {
			if (Util.isDeleteKey(key)) {
				int row = parameterTable.getSelectedRow();
				if ((row >= 0) && (row < parameterListe.size())) {
					parameterListe.remove(row);
					parameterTable.getTableModel().removeRow(row);
					parameterTable.setSelectedRow(Math.max(0, row - 1));
				}
				return true;
			} else if (Util.isInsertAfterKey(key)) {
				TextInputDialogBuilder builder = new TextInputDialogBuilder();
				builder.setTitle("Parameter").setDescription("Neuen Parameter angeben:");
				String newParameter = builder.build().showDialog(getTextGUI());
				if (newParameter != null) {
					int row = parameterTable.getSelectedRow();
					parameterListe.add(row, newParameter);
					parameterTable.getTableModel().insertRow(row + 1, Collections.singleton(newParameter));
					parameterTable.setSelectedRow(row + 1);
				}
				return true;
			} else if (Util.isInsertBeforeKey(key)) {
				TextInputDialogBuilder builder = new TextInputDialogBuilder();
				builder.setTitle("Parameter").setDescription("Neuen Parameter angeben:");
				String newParameter = builder.build().showDialog(getTextGUI());
				if (newParameter != null) {
					int row = parameterTable.getSelectedRow();
					parameterListe.add(row, newParameter);
					parameterTable.getTableModel().insertRow(row, Collections.singleton(newParameter));
				}
				return true;
			} else if (Util.isEintragNachObenKey(key)) {
				int row = parameterTable.getSelectedRow();
				if (row > 0) {
					String parameter = parameterListe.remove(row);
					parameterListe.add(row - 1, parameter);
					parameterTable.getTableModel().setCell(0, row - 1, parameterListe.get(row - 1));
					parameterTable.getTableModel().setCell(0, row, parameterListe.get(row));
					parameterTable.setSelectedRow(row - 1);
				}
				return true;
			} else if (Util.isEintragNachUntenKey(key)) {
				int row = parameterTable.getSelectedRow();
				if (row < parameterListe.size() - 1) {
					String parameter = parameterListe.remove(row);
					parameterListe.add(row + 1, parameter);
					parameterTable.getTableModel().setCell(0, row + 1, parameterListe.get(row + 1));
					parameterTable.getTableModel().setCell(0, row, parameterListe.get(row));
					parameterTable.setSelectedRow(row + 1);
				}
				return true;
			}
		}

 		return super.handleInput(key);
	}

	@Override
	public List<String> getElement() {
		return parameterListe;
	}
}
