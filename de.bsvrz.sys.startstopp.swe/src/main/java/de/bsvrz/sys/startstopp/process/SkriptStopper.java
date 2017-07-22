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

package de.bsvrz.sys.startstopp.process;

import java.util.LinkedHashMap;
import java.util.Map;

public class SkriptStopper extends Thread {

	private final Map<String, StartStoppApplikation> applikationen = new LinkedHashMap<>();
	private final Map<String, StartStoppApplikation> kernsystem = new LinkedHashMap<>();
	private ProcessManager processManager;

	public SkriptStopper(ProcessManager processManager) {
		
		this.processManager = processManager;
		
		for (StartStoppApplikation applikation : processManager.getManagedApplikationen()) {
			if (applikation.isKernsystem()) {
				kernsystem.put(applikation.getInkarnationsName(), applikation);
			} else {
				this.applikationen.put(applikation.getInkarnationsName(), applikation);
			}
		}
	}

	@Override
	public void run() {
		// TODO Herunterfahren implementieren
		for( String name : applikationen.keySet()) {
			System.err.println("Beende App: " + name);
		}
		for( String name : kernsystem.keySet()) {
			System.err.println("Beende Kernsystem: " + name);
		}
		
		processManager.stopp();
		
	}
}
