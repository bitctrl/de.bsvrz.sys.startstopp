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

import java.util.ArrayList;
import java.util.List;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.startstopp.StartStopp;

public class ProcessManager extends Thread {

	private boolean stopped;
	private Object lock = new Object();

	public ProcessManager() {
		this(StartStopp.getInstance());
	}
	
	public ProcessManager(StartStopp startStopp) {
		super("ProcessManager");
	}
	
	@Override
	public void run() {
		while (!stopped) {
//			System.err.println("Überwache Inkarnationen");
			try {
				synchronized (lock) {
					lock.wait(30000);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
	}

	public List<Applikation> getApplikationen() {
		// TODO Auto-generated method stub
		
		Applikation applikation = new Applikation();
		applikation.setInkarnationsName("Heinz");
		applikation.setStatus(Applikation.Status.STARTENWARTEN);
		applikation.getArguments().add("Arg1");
		applikation.setLetzteStartzeit("Letzte Startzeit");
		applikation.setLetzteStoppzeit("Letzte Stoppzeit");

		List<Applikation> result = new ArrayList<>();
		result.add(applikation);
		
		return result;
	}

	public Applikation getApplikation(String inkarnationsName) throws StartStoppException {
		// TODO Auto-generated method stub
		
		if( "Datenverteiler".equals(inkarnationsName)) {
			Applikation applikation = new Applikation();
			applikation.setInkarnationsName("Datenverteiler");
			applikation.setStatus(Applikation.Status.STARTENWARTEN);
			applikation.getArguments().add("Arg1");
			applikation.setLetzteStartzeit("Letzte Startzeit");
			applikation.setLetzteStoppzeit("Letzte Stoppzeit");
			return applikation;
		}
		
		throw new StartStoppException("Eine Applikation mit dem Inkarnationsname \"" + inkarnationsName + "\" konnte nicht gefunden werden");
	}

	public Applikation starteApplikation(String inkarnationsName) throws StartStoppException {
		// TODO Auto-generated method stub
		
		Applikation applikation = getApplikation(inkarnationsName);
		if( applikation != null) {
			return applikation;
		}
		
		throw new StartStoppException("Eine Applikation mit dem Inkarnationsname \"" + inkarnationsName + "\" konnte nicht gefunden werden");
	}

	public Applikation restarteApplikation(String inkarnationsName) throws StartStoppException {
		// TODO Auto-generated method stub
		Applikation applikation = getApplikation(inkarnationsName);
		if( applikation != null) {
			return applikation;
		}

		throw new StartStoppException("Eine Applikation mit dem Inkarnationsname \"" + inkarnationsName + "\" konnte nicht gefunden werden");
	}

	public Applikation stoppeApplikation(String inkarnationsName) throws StartStoppException {
		// TODO Auto-generated method stub
		Applikation applikation = getApplikation(inkarnationsName);
		if( applikation != null) {
			return applikation;
		}

		throw new StartStoppException("Eine Applikation mit dem Inkarnationsname \"" + inkarnationsName + "\" konnte nicht gefunden werden");
	}

	public void stopp() {
		stopped = true;
		synchronized (lock) {
			lock.notify();
		}
	}

	public boolean isSkriptRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSkriptStopped() {
		// TODO Auto-generated method stub
		return false;
	}

}
