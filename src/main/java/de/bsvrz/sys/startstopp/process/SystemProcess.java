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

import de.bsvrz.sys.funclib.debug.Debug;

public class SystemProcess implements InkarnationsProzessIf {

	@Override
	public void addProzessListener(InkarnationsProzessListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeProzessListener(InkarnationsProzessListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLogger(Debug logger) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getLastExitCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getStartFehler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InkarnationsProzessStatus getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void stopAusgabeUmlenkung() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopp() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getProzessAusgabe() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProgramm() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProgramm(String command) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getProgrammArgumente() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProgrammArgumente(String args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getInkarnationsName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInkarnationsName(String command) {
		// TODO Auto-generated method stub
		
	}

}
