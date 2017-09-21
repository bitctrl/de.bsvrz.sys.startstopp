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

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.GridLayout.Alignment;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowListenerAdapter;
import com.googlecode.lanterna.gui2.table.Table;

import de.bsvrz.sys.startstopp.api.jsonschema.ApplikationLog;

class ApplikationLogWindow extends BasicWindow {

	private Table<String> messagesTable;

	ApplikationLogWindow(String string) {
		super(string);
		setCloseWindowWithEscape(true);
		initUI();
	}

	private void initUI() {
		
		Panel panel = new Panel();
		panel.setLayoutManager(new GridLayout(1));
		panel.setLayoutData(GridLayout.createLayoutData(Alignment.BEGINNING, Alignment.BEGINNING, true, true));
		
		messagesTable = new Table<>("Meldungen");
		messagesTable.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.FILL, true, true));
		addWindowListener(new WindowListenerAdapter() {
			@Override
			public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
				messagesTable.setVisibleRows(newSize.getRows() - 3);
			}
		});

		panel.addComponent(messagesTable);
		setComponent(panel);
	}

	public void setLog(ApplikationLog applikationLog) {
		for (String message : applikationLog.getMessages()) {
			messagesTable.getTableModel().addRow(message);
		}
	}
}
