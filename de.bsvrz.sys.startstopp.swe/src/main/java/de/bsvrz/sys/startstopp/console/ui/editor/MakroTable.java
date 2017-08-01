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

import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.table.Table;

import de.bsvrz.sys.startstopp.api.jsonschema.MakroDefinition;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;

public class MakroTable extends Table<Object> {

	public MakroTable(WindowBasedTextGUI gui, StartStoppSkript skript) {
		super("Name", "Wert");

		for (MakroDefinition makroDefinition : skript.getGlobal().getMakrodefinitionen()) {
			getTableModel().addRow(makroDefinition.getName(), makroDefinition.getWert());
		}
		
		setSelectAction(new Runnable() {
			@Override
			public void run() {
				int row = getSelectedRow();
				MakroDefinition makroDefinition = skript.getGlobal().getMakrodefinitionen().get(row);
				MakroEditor editor = new MakroEditor(skript, makroDefinition); 
				MakroDefinition result = editor.showDialog(gui);
				if (result != null) {
					makroDefinition.setName(result.getName());
					getTableModel().setCell(0, row, makroDefinition.getName());
					makroDefinition.setWert(result.getWert());
					getTableModel().setCell(1, row, makroDefinition.getWert());
				}
			}
		});
	}
}
