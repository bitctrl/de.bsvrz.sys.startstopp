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

import java.io.File;

import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

public class StartStoppOptions {

	private static final Debug LOGGER = Debug.getLogger();
	private static final String PARAM_STARTSTOPP_KONFIGURATION = "-startStoppKonfiguration=.";
	private static final String PARAM_BENUTZER_KONFIGURATION = "-benutzerKonfiguration";
	private static final String PARAM_AUTHENTIFIZIERUNG = "-authentifizierung";
	private static final String PARAM_HTTPS_PORT = "-port=3000";
	private static final String PARAM_HTTP_PORT = "-httpport=0";
	private static final String PARAM_INKARNATIONSNAME = "-inkarnationsName=StartStopp";
	private static final String PARAM_RECHNER_PID = "-rechner";
	private static final String PARAM_MASTER = "-master";

	private final String skriptDir;
	private final int httpsPort;
	private final int httpPort;
	private final String inkarnationsName;
	private String userConfigurationName;
	private String authentifizierung;
	private String rechnerPid;

	private String masterHost;
	private int masterPort = 3000;

	public StartStoppOptions(String ... args) {
		ArgumentList argumentList = new ArgumentList(args);
		skriptDir = argumentList.fetchArgument(PARAM_STARTSTOPP_KONFIGURATION).asString();
		httpsPort = argumentList.fetchArgument(PARAM_HTTPS_PORT).intValue();
		httpPort = argumentList.fetchArgument(PARAM_HTTP_PORT).intValue();
		inkarnationsName = argumentList.fetchArgument(PARAM_INKARNATIONSNAME).asString();

		if (argumentList.hasArgument(PARAM_BENUTZER_KONFIGURATION)) {
			userConfigurationName = argumentList.fetchArgument(PARAM_BENUTZER_KONFIGURATION).asString();
		}

		if (argumentList.hasArgument(PARAM_AUTHENTIFIZIERUNG)) {
			authentifizierung = argumentList.fetchArgument(PARAM_AUTHENTIFIZIERUNG).asString();
		}

		if (argumentList.hasArgument(PARAM_RECHNER_PID)) {
			rechnerPid = argumentList.fetchArgument(PARAM_RECHNER_PID).asString();
		}

		if (argumentList.hasArgument(PARAM_MASTER)) {
			String masterStr = argumentList.fetchArgument(PARAM_MASTER).asString();
			String[] parts = masterStr.trim().split(":");
			if (parts.length > 0) {
				masterHost = parts[0];
			}
			if (parts.length > 1) {
				try {
					masterPort = Integer.parseInt(parts[1]);
				} catch (NumberFormatException e) {
					LOGGER.warning("Port konnte nicht interpretiert werden: \"" + parts[1] + "\"!: "
							+ e.getLocalizedMessage());
				}
			}
		}
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

	public String getMasterHost() {
		return masterHost;
	}

	public int getMasterPort() {
		return masterPort;
	}

	public File getPasswdFile() {
		if (authentifizierung != null) {
			File file = new File(authentifizierung);
			if (file.exists() && file.isFile()) {
				return file;
			}
		}

		return null;
	}

	public String getRechnerPid() {
		return rechnerPid;
	}

	public String getSkriptDir() {
		return skriptDir;
	}

	public File getUserManagementFile() {
		if (userConfigurationName != null) {
			File file = new File(userConfigurationName);
			if (file.exists() && file.isFile()) {
				return file;
			}
		}

		return null;
	}

}
