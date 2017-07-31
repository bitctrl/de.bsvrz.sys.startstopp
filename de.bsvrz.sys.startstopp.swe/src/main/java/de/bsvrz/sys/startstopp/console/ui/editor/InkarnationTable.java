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
import java.util.List;

import com.googlecode.lanterna.gui2.table.Table;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.config.StartStoppException;

public class InkarnationTable extends Table<Object> {

	private static final Debug LOGGER = Debug.getLogger();
	
	private List<Inkarnation> inkarnations = new ArrayList<>();

	public InkarnationTable(StartStoppSkript skript) throws StartStoppException {
		super("Name");

		for (Inkarnation inkarnation : skript.getInkarnationen()) {
			getTableModel().addRow(inkarnation.getInkarnationsName());
			inkarnations.add(inkarnation);
		}
	}
	
	public Inkarnation getSelectedOnlineInkarnation() {
		int row = getSelectedRow();
		if(( row < 0 ) || (row >= inkarnations.size())) {
			return null;
		}
		
		return inkarnations.get(row);
	}

}