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

import java.util.Collections;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.table.Table;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;
import de.bsvrz.sys.startstopp.console.StartStoppConsole;
import de.bsvrz.sys.startstopp.console.ui.StartStoppButton;

class ApplikationDetailWindow extends BasicWindow {

	private Applikation applikation;

	ApplikationDetailWindow(Applikation applikation) {
		super("Details:" + applikation.getInkarnation().getInkarnationsName());
		this.applikation = applikation;

		setCloseWindowWithEscape(true);
		initUI();
	}

	private void initUI() {
		Panel panel = new Panel();
		panel.setLayoutManager(new GridLayout(2));
		panel.setLayoutData(GridLayout.createHorizontallyEndAlignedLayoutData(1));

		panel.addComponent(new Label("Applikation: "));
		panel.addComponent(new Label(applikation.getInkarnation().getApplikation()));

		Table<String> parameterTable = new Table<>("Parameter");
		parameterTable.setVisibleRows(5);
		parameterTable.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2));
		panel.addComponent(parameterTable.withBorder(Borders.singleLine()));
		for (String parameter : applikation.getInkarnation().getAufrufParameter()) {
			parameterTable.getTableModel().addRow(parameter);
		}

		panel.addComponent(new Label("Status: "));
		panel.addComponent(new Label(applikation.getStatus().name()));

		panel.addComponent(new Label("Startzeit: "));
		panel.addComponent(new Label(Util.nonEmptyString(applikation.getLetzteStartzeit())));

		panel.addComponent(new Label("Initialisierung: "));
		panel.addComponent(new Label(Util.nonEmptyString(applikation.getLetzteInitialisierung())));
		
		panel.addComponent(new Label("Stoppzeit: "));
		panel.addComponent(new Label(Util.nonEmptyString(applikation.getLetzteStoppzeit())));

		TextBox messageLabel = new TextBox(Util.nonEmptyString(applikation.getStartMeldung()));
		messageLabel.setEnabled(false);
		panel.addComponent(messageLabel);
		messageLabel.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2));

		Button inkarnationButton = new StartStoppButton("Inkarnation", () -> showInkarnationPanel());
		panel.addComponent(inkarnationButton.withBorder(Borders.singleLine()));
		inkarnationButton.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2));

		setComponent(panel);
	}

	private void showInkarnationPanel() {
		if (applikation != null) {
			InkarnationDetailWindow window = new InkarnationDetailWindow(applikation);
			window.setHints(Collections.singleton(Hint.EXPANDED));
			close();
			StartStoppConsole.getGui().addWindow(window);
		}
	}
}
