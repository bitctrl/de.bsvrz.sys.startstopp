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

import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.console.ui.EditableTable;

class RechnerTable extends EditableTable<Rechner> {

	private StartStoppSkript skript;

	RechnerTable(StartStoppSkript skript) {
		super(skript.getGlobal().getRechner(), "Name", "Host", "Port");
		this.skript = skript;

		this.skript = skript;
		for (Rechner rechner : skript.getGlobal().getRechner()) {
			addElement(rechner);
		}
	}


	@Override
	protected Rechner requestNewElement() {
		RechnerEditor dialog = new RechnerEditor(skript, new Rechner().withName("Neuer Rechner"));
		if( dialog.showDialog(getTextGUI())) {
			return dialog.getElement();
		}
		return null;
	}

	@Override
	protected Rechner editElement(Rechner oldElement) {
		RechnerEditor dialog = new RechnerEditor(skript, oldElement);
		if( dialog.showDialog(getTextGUI())) {
			return dialog.getElement();
		}
		return null;
	}

	@Override
	protected List<String> getStringsFor(Rechner rechner) {
		List<String> result = new ArrayList<>();
		result.add(rechner.getName());
		result.add(rechner.getTcpAdresse());
		result.add(rechner.getPort());
		return result;	}
}
