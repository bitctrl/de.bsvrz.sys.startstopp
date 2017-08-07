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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.googlecode.lanterna.gui2.table.Table;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.client.StartStoppClient;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkriptStatus;
import de.bsvrz.sys.startstopp.config.StartStoppException;

public class OnlineInkarnationTable extends Table<Object> {

	private static final Debug LOGGER = Debug.getLogger();
	
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
					LOGGER.warning(e.getLocalizedMessage());
				}

				try {
					List<Applikation> applikationen = client.getApplikationen();

					boolean showApplikationen = true;
					if( applikationen.isEmpty()) {
						StartStoppSkriptStatus skriptStatus = client.getCurrentSkriptStatus();
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

	private final MessagesTableModell emtpyTableModell;
	private final ApplikationenTableModell applikationenTableModell;

	private StartStoppClient client;

	@Inject
	public OnlineInkarnationTable(StartStoppClient client, MessagesTableModell messagesModell, ApplikationenTableModell applikationenModell) {
		super("");
		this.client = client;
		this.applikationenTableModell = applikationenModell;
		this.emtpyTableModell = messagesModell;
		setTableModel(emtpyTableModell);
		setTableCellRenderer(new OnlineTableRenderer());
		emtpyTableModell.addMessage("Unbekannter Status");
	}

	@PostConstruct
	@Inject
	void init() {
		try {
			applikationenTableModell.setApplikationen(client.getApplikationen());
			setTableModel(applikationenTableModell);
		} catch (StartStoppException e) {
			System.err.println(e.getLocalizedMessage());
		}

		Thread updater = new Updater();
		updater.start();
	}

	public Applikation getSelectedApplikation() {

		if (getTableModel() != applikationenTableModell) {
			return null;
		}

		return applikationenTableModell.getApplikation(getSelectedRow());
		
	}
}