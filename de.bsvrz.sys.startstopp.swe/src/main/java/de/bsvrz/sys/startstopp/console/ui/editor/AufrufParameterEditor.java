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

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialogBuilder;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.console.ui.GuiComponentFactory;
import de.bsvrz.sys.startstopp.console.ui.MakroTextInputDialog;

public class AufrufParameterEditor extends StartStoppElementEditor<List<String>> {

	public class AufrufParameterTable extends EditableTable<String> {

		public AufrufParameterTable(List<String> parameterListe, String string) {
			super(parameterListe, string);
		}

		@Override
		protected String requestNewElement() {
			MakroTextInputDialog dialog = factory.createMakroTextInputDialog(getSkript(), "Parameter", "Neuen Parameter angeben:", "");
			if( dialog.showDialog((WindowBasedTextGUI) getTextGUI())) {
				return dialog.getElement();
			}
			return null;
		}

		@Override
		protected String editElement(String oldElement) {
			MakroTextInputDialog dialog = factory.createMakroTextInputDialog(getSkript(), "Parameter", "Parameter bearbeiten:", oldElement);
			if( dialog.showDialog((WindowBasedTextGUI) getTextGUI())) {
				return dialog.getElement();
			}
			return null;
		}

		@Override
		protected String renderElement(String element) {
			return element;
		}

	}


	private List<String> parameterListe = new ArrayList<>();
	private EditableTable<String> parameterTable;

	@Inject
	GuiComponentFactory factory;
	
	@Inject
	public AufrufParameterEditor(@Assisted StartStoppSkript skript, @Assisted Inkarnation inkarnation) {
		super(skript, "Aufrufparameter");
		this.parameterListe.addAll(inkarnation.getAufrufParameter());
	}

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
