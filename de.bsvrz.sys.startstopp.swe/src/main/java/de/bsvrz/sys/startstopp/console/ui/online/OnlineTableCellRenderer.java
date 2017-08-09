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

import java.util.EnumSet;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.graphics.ThemeStyle;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.ComponentRenderer;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.gui2.table.Table;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation.Status;
import de.bsvrz.sys.startstopp.console.ui.EditableTable;
import de.bsvrz.sys.startstopp.console.ui.EditableTableCellRenderer;

class OnlineTableCellRenderer extends EditableTableCellRenderer<Applikation> {

	public OnlineTableCellRenderer(EditableTable<Applikation> table) {
		super(table);
	}
	
	@Override
	protected ThemeStyle getCellStyle(Table<Applikation> table, Applikation cell, int columnIndex, int rowIndex) {

		Status status = cell.getStatus();
		String statusName = status.name();

		ThemeDefinition themeDefinition = table.getThemeDefinition();
		ThemeStyle style = themeDefinition.getNormal();
		if ((table.getSelectedColumn() == columnIndex && table.getSelectedRow() == rowIndex)
				|| (table.getSelectedRow() == rowIndex && !table.isCellSelection())) {
			statusName = statusName + "_SELECTED";
			if (table.isFocused()) {
				style = themeDefinition.getActive();
			} else {
				style = themeDefinition.getSelected();
			}
		}
		if (themeDefinition.getBooleanProperty("COLOR_STATUS", false)) {
			style = themeDefinition.getCustom(statusName);
		}
		
		return style;
	}
}
