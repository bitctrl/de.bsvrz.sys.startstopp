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

import com.googlecode.lanterna.gui2.Window.Hint;

import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.console.StartStoppConsole;
import de.bsvrz.sys.startstopp.console.ui.InfoDialog;

class ApplikationLogAction implements Runnable {


	private Applikation applikation;

	ApplikationLogAction(Applikation applikation) {
		this.applikation = applikation;
	}
	
	@Override
	public void run() {
		ApplikationLogWindow window = new ApplikationLogWindow("Meldungen: " + applikation.getInkarnation().getInkarnationsName());
		window.setHints(Collections.singleton(Hint.EXPANDED));
		StartStoppConsole.getGui().addWindow(window);

		try {
			window.setLog(StartStoppConsole.getClient().getApplikationLog(applikation.getInkarnation().getInkarnationsName()));
		} catch (StartStoppException e) {
			new InfoDialog("FEHLER", e.getLocalizedMessage()).display();
		}
	}

	@Override
	public String toString() {
		return "Ausgaben anzeigen";
	}
}
