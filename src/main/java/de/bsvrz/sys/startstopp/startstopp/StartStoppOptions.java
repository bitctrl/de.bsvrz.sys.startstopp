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

package de.bsvrz.sys.startstopp.startstopp;

import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

public class StartStoppOptions {

	private static final String PARAM_STARTSTOPP_KONFIGURATION = "-startStoppKonfiguration=.";
	private static final String PARAM_HTTPS_PORT= "-port=3000";
	private static final String PARAM_HTTP_PORT= "-httpport=0";
	private static final String PARAM_INKARNATIONSNAME = "-inkarnationsName=StartStopp";
	
	
	private final String skriptDir;
	private final int httpsPort;
	private final int httpPort;
	private final String inkarnationsName;

	public StartStoppOptions(String[] args) {
		ArgumentList argumentList = new ArgumentList(args);
		skriptDir = argumentList.fetchArgument(PARAM_STARTSTOPP_KONFIGURATION).asString();
		httpsPort = argumentList.fetchArgument(PARAM_HTTPS_PORT).intValue();
		httpPort = argumentList.fetchArgument(PARAM_HTTP_PORT).intValue();
		inkarnationsName = argumentList.fetchArgument(PARAM_INKARNATIONSNAME).asString();
	}

	public int getHttpPort() {
		return httpPort;
	}
	
	public int getHttpsPort() {
		return httpsPort;
	}
	
	public String getInkarnationsName() {
		return inkarnationsName;
	}

	public String  getSkriptDir() {
		return skriptDir;
	}
}
