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
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyStroke;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.KernSystem;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;
import de.bsvrz.sys.startstopp.console.ui.GuiComponentFactory;

public class KernsystemEditor extends StartStoppElementEditor<List<KernSystem>> {

	private List<KernSystem> kernSysteme = new ArrayList<>();
	private Table<String> ksTable;
	private StartStoppSkript skript;

	@Inject
	private GuiComponentFactory factory;

	@Inject
	public KernsystemEditor(@Assisted StartStoppSkript skript) {
		super("Kernsystem");
		this.skript = skript;
		for (KernSystem kernSystem : skript.getGlobal().getKernsysteme()) {
			kernSysteme.add((KernSystem) Util.cloneObject(kernSystem));
		}
	}

	protected void initComponents(Panel mainPanel) {
		mainPanel.setLayoutManager(new GridLayout(1).setLeftMarginSize(1).setRightMarginSize(1));

		ksTable = new Table<>("Kernsystem");
		for (KernSystem ks : kernSysteme) {
			ksTable.getTableModel().addRow(ks.getInkarnationsName());
		}
		mainPanel.addComponent(ksTable, GridLayout.createHorizontallyFilledLayoutData(1));
	}

	public List<KernSystem> getElement() {
		return kernSysteme;
	}

	@Override
	public boolean handleInput(KeyStroke key) {
		if (ksTable.isFocused()) {
			if (Util.isDeleteKey(key)) {
				int row = ksTable.getSelectedRow();
				if ((row >= 0) && (row < kernSysteme.size())) {
					kernSysteme.remove(row);
					ksTable.getTableModel().removeRow(row);
					ksTable.setSelectedRow(Math.max(0, row - 1));
				}
				return true;
			} else if (Util.isInsertAfterKey(key)) {
				InkarnationSelektor inkarnationSelektor = factory.createInkarnationSelektor(skript);
				for (KernSystem ks : kernSysteme) {
					inkarnationSelektor.removeInkarnation(ks.getInkarnationsName());
				}
				Inkarnation inkarnation = inkarnationSelektor.getInkarnation();
				if (inkarnation != null) {
					int row = ksTable.getSelectedRow();
					kernSysteme.add(row, new KernSystem(inkarnation.getInkarnationsName()));
					ksTable.getTableModel().insertRow(row + 1, Collections.singleton(inkarnation.getInkarnationsName()));
					ksTable.setSelectedRow(row + 1);
				}
				return true;
			} else if (Util.isInsertBeforeKey(key)) {
				InkarnationSelektor inkarnationSelektor = factory.createInkarnationSelektor(skript);
				for (KernSystem ks : kernSysteme) {
					inkarnationSelektor.removeInkarnation(ks.getInkarnationsName());
				}
				Inkarnation inkarnation = inkarnationSelektor.getInkarnation();
				if (inkarnation != null) {
					int row = ksTable.getSelectedRow();
					kernSysteme.add(row, new KernSystem(inkarnation.getInkarnationsName()));
					ksTable.getTableModel().insertRow(row, Collections.singleton(inkarnation.getInkarnationsName()));
				}
				return true;
			} else if (Util.isEintragNachObenKey(key)) {
				int row = ksTable.getSelectedRow();
				if (row > 0) {
					KernSystem kernSystem = kernSysteme.remove(row);
					kernSysteme.add(row - 1, kernSystem);
					ksTable.getTableModel().setCell(0, row - 1, kernSysteme.get(row - 1).getInkarnationsName());
					ksTable.getTableModel().setCell(0, row, kernSysteme.get(row).getInkarnationsName());
					ksTable.setSelectedRow(row - 1);
				}
				return true;
			} else if (Util.isEintragNachUntenKey(key)) {
				int row = ksTable.getSelectedRow();
				if (row < kernSysteme.size() - 1) {
					KernSystem kernSystem = kernSysteme.remove(row);
					kernSysteme.add(row + 1, kernSystem);
					ksTable.getTableModel().setCell(0, row + 1, kernSysteme.get(row + 1).getInkarnationsName());
					ksTable.getTableModel().setCell(0, row, kernSysteme.get(row).getInkarnationsName());
					ksTable.setSelectedRow(row + 1);
				}
				return true;
			}
		}

		return super.handleInput(key);
	}
}
