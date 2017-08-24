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

import java.util.SortedMap;
import java.util.TreeMap;

import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;

public class InkarnationSelektor {
	
	private Inkarnation selected;
	private ActionListDialogBuilder builder;

	private SortedMap<String, Inkarnation> inkarnationen = new TreeMap<>();
	
	public InkarnationSelektor(StartStoppSkript skript) {
		for( Inkarnation inkarnation : skript.getInkarnationen()) {
			inkarnationen.put(inkarnation.getInkarnationsName(), inkarnation);
		}
	}

	public void removeInkarnation( String name) {
		inkarnationen.remove(name);
	}
	
	public Inkarnation getInkarnation(WindowBasedTextGUI gui) {

		builder = new ActionListDialogBuilder();
		builder.setTitle("Inkarnation");
		for( Inkarnation inkarnation : inkarnationen.values()) {
			builder.addAction(inkarnation.getInkarnationsName(), () -> { selected = inkarnation;});
		}

		builder.build().showDialog(gui);
		return selected;
	}
}
