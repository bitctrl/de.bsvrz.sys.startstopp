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

package de.bsvrz.sys.startstopp.console.ui;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.lanterna.gui2.table.Table;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.console.StartStoppConsole;

public class InkarnationTable extends Table<Object> {

	private static final Debug LOGGER = Debug.getLogger();
	
	private final class Simulator extends Thread {

		private Simulator() {
			super("StatusUpdater");
			setDaemon(true);
		}

		public void run() {

			while (true) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					LOGGER.warning(e.getLocalizedMessage());
				}
				for (int row = 0; row < getTableModel().getRowCount(); row++) {
					Applikation applikation = inkarnations.get(row);
					try {
						applikation = StartStoppConsole.getInstance().getClient().getApplikation(applikation.getInkarnationsName());
					} catch (StartStoppException e) {
						LOGGER.warning(e.getLocalizedMessage());
					}
					getTableModel().setCell(1, row, applikation.getStatus());
				}
			}
		}
	}

	private List<Applikation> inkarnations = new ArrayList<>();

	public InkarnationTable() throws StartStoppException {
		super("Name", "Status", "Startzeit");

		for (Applikation inkarnation : StartStoppConsole.getInstance().getClient().getApplikationen()) {
			getTableModel().addRow(inkarnation.getInkarnationsName(), inkarnation.getStatus(),
					inkarnation.getLetzteStartzeit());
			inkarnations.add(inkarnation);
		}

		Thread simulator = new Simulator();
		simulator.start();
	}
	
	public Applikation getSelectedOnlineInkarnation() {
		int row = getSelectedRow();
		if(( row < 0 ) || (row >= inkarnations.size())) {
			return null;
		}
		
		return inkarnations.get(row);
	}

}