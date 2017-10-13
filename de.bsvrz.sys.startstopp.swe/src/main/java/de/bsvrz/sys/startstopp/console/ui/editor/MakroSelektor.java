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
import com.googlecode.lanterna.gui2.dialogs.ActionListDialog;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;

import de.bsvrz.sys.startstopp.api.jsonschema.MakroDefinition;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;

public class MakroSelektor {
	
	private MakroDefinition selectedMakroDefinition;
	private ActionListDialogBuilder builder;
	private WindowBasedTextGUI gui;
	
	public MakroSelektor(WindowBasedTextGUI gui, StartStoppSkript skript) {
		
		this.gui = gui;
		
		builder = new ActionListDialogBuilder();
		builder.setTitle("Makroauswahl").setCanCancel(false);
		for( MakroDefinition definition : skript.getGlobal().getMakrodefinitionen()) {
			builder.addAction(definition.getName(), new Runnable() {
				@Override
				public void run() {
					selectedMakroDefinition = definition;
				}
			});
		}
		
	}

	public MakroDefinition getSelectedMakroDefinition() {
		builder.setDescription("ESC - Abbrechen");
		ActionListDialog dialog = builder.build();
		dialog.setCloseWindowWithEscape(true);
		dialog.showDialog(gui);

		return selectedMakroDefinition;
	}
}
