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

import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Panel;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.console.ui.EditableTable;
import de.bsvrz.sys.startstopp.console.ui.ParameterInputDialog;

class AufrufParameterEditor extends StartStoppElementEditor<List<String>> {

	private class AufrufParameterTable extends EditableTable<String> {

		AufrufParameterTable(List<String> parameterListe, String string) {
			super(parameterListe, string);
		}

		@Override
		protected String requestNewElement() {
			ParameterInputDialog dialog = new ParameterInputDialog(getSkript(), "Parameter", "Neuen Parameter angeben:", "");
			if( dialog.showDialog(getTextGUI())) {
				return dialog.getElement();
			}
			return null;
		}

		@Override
		protected String editElement(String oldElement) {
			ParameterInputDialog dialog = new ParameterInputDialog(getSkript(), "Parameter", "Parameter bearbeiten:", oldElement);
			if( dialog.showDialog(getTextGUI())) {
				return dialog.getElement();
			}
			return null;
		}

		@Override
		protected List<String> getStringsFor(String element) {
			return Collections.singletonList(element);
		}

		@Override
		protected boolean checkDelete(String element) {
			return true;
		}
	}


	private List<String> parameterListe = new ArrayList<>();
	private EditableTable<String> parameterTable;

	AufrufParameterEditor(StartStoppSkript skript, Inkarnation inkarnation) {
		super(skript, "Aufrufparameter");
		this.parameterListe.addAll(inkarnation.getAufrufParameter());
	}

	@Override
	protected void initComponents(Panel mainPanel) {
		mainPanel.setLayoutManager(new GridLayout(1).setLeftMarginSize(1).setRightMarginSize(1));
		parameterTable = new AufrufParameterTable(parameterListe, "Aufrufparameter");
		mainPanel.addComponent(parameterTable, GridLayout.createHorizontallyFilledLayoutData(1));
	}

	@Override
	public List<String> getElement() {
		return parameterListe;
	}
}
