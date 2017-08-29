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

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyStroke;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;

class ApplikationDetailWindow extends BasicWindow {

	private final Label applikationLabel = new Label("");
	private final Table<String> parameterTable = new Table<>("Parameter");

	ApplikationDetailWindow(String string) {
		super(string);
		setCloseWindowWithEscape(true);
		initUI();
	}

	private void initUI() {

		
		Panel panel = new Panel();
		panel.setLayoutManager(new GridLayout(2));
		panel.setLayoutData(GridLayout.createHorizontallyEndAlignedLayoutData(1));

		panel.addComponent(new Label("Applikation: "));
		panel.addComponent(applikationLabel);
		
		parameterTable.setLayoutData(GridLayout.createHorizontallyEndAlignedLayoutData(2));
		parameterTable.setVisibleRows(3);

		panel.addComponent(parameterTable);
		setComponent(panel);
	}

	public void setApplikation(Applikation applikation) {
		applikationLabel.setText(applikation.getInkarnation().getApplikation());
		for( String parameter : applikation.getInkarnation().getAufrufParameter()) {
			parameterTable.getTableModel().addRow(parameter);
		}
		// TODO weitere Daten ergänzen
	}
}
