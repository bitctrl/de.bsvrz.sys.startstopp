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
import java.util.Collection;
import java.util.List;

import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkriptStatus;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.console.StartStoppConsole;

public class OnlineInkarnationTable extends Table<Object> {

	private static class ApplikationenTableModell extends TableModel<Object> {

		public ApplikationenTableModell() {
			super("Inkarnation", "Status", "Startzeit");
		}

		public void setApplikationen(List<Applikation> applikationen) {

			while (getRowCount() > 0) {
				removeRow(0);
			}

			for (Applikation applikation : applikationen) {
				addRow(getValues(applikation));
			}
		}

		public void updateApplikationen(List<Applikation> applikationen) {
			for (int idx = 0; idx < applikationen.size(); idx++) {
				Applikation applikation = applikationen.get(idx);
				if (getRowCount() <= idx) {
					addRow(applikation.getInkarnation().getInkarnationsName(), applikation.getStatus(),
							applikation.getLetzteStartzeit());
				} else if (getCell(0, idx).equals(applikation.getInkarnation().getInkarnationsName())) {
					setCell(1, idx, applikation.getStatus());
					setCell(2, idx, applikation.getLetzteStartzeit());
				} else {
					insertRow(idx, getValues(applikation));
				}
			}

			while (getRowCount() > applikationen.size()) {
				removeRow(getRowCount() - 1);
			}
		}

		private Collection<Object> getValues(Applikation applikation) {
			Collection<Object> result = new ArrayList<>();
			result.add(applikation.getInkarnation().getInkarnationsName());
			result.add(applikation.getStatus());
			result.add(applikation.getLetzteStartzeit());
			return result;
		}

	}

	private static class EmptyTableModell extends TableModel<Object> {

		public EmptyTableModell() {
			super("Meldungen");
		}

		public void addMessage(String string) {
			addRow(string);
		}

		public void setMessage(String message) {
			setCell(0, 0, message);
		}
	}

	private final class Updater extends Thread {
		private Updater() {
			super("StatusUpdater");
			setDaemon(true);
		}

		public void run() {

			while (true) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					List<Applikation> applikationen = StartStoppConsole.getInstance().getClient().getApplikationen();

					boolean showApplikationen = true;
					if( applikationen.isEmpty()) {
						StartStoppSkriptStatus skriptStatus = StartStoppConsole.getInstance().getClient().getCurrentSkriptStatus();
						if( skriptStatus.getStatus() == StartStoppSkriptStatus.Status.FAILURE) {
							emtpyTableModell.setMessage(skriptStatus.getMessages().get(0));
							setTableModel(emtpyTableModell);
							showApplikationen = false;
						}
					} 
					
					if( showApplikationen) {
					applikationenTableModell.updateApplikationen(applikationen);
					setTableModel(applikationenTableModell);
					}
				} catch (StartStoppException e) {
					emtpyTableModell.setMessage(e.getLocalizedMessage());
					setTableModel(emtpyTableModell);
				}
			}
		}
	}

	private EmptyTableModell emtpyTableModell = new EmptyTableModell();
	private ApplikationenTableModell applikationenTableModell = new ApplikationenTableModell();

	public OnlineInkarnationTable() {
		super("");

		setTableModel(emtpyTableModell);
		emtpyTableModell.addMessage("Unbekannter Status");

		try {
			applikationenTableModell.setApplikationen(StartStoppConsole.getInstance().getClient().getApplikationen());
			setTableModel(applikationenTableModell);
		} catch (StartStoppException e) {
			System.err.println(e.getLocalizedMessage());
		}

		Thread updater = new Updater();
		updater.start();
	}

	public String getSelectedOnlineInkarnation() {

		if (getTableModel() != applikationenTableModell) {
			return null;
		}

		int row = getSelectedRow();
		if ((row < 0) || (row >= getTableModel().getRowCount())) {
			return null;
		}

		return (String) getTableModel().getCell(0, row);
	}
}