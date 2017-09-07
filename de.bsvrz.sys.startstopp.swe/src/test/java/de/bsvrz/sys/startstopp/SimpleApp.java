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

package de.bsvrz.sys.startstopp;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientDavParameters;
import de.bsvrz.dav.daf.main.config.management.UserAdministration;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

public class SimpleApp implements StandardApplication {

	@Override
	public void parseArguments(ArgumentList argumentList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initialize(ClientDavInterface connection) throws Exception {
		// TODO Auto-generated method stub

	}
	
	public static void main(String[] args) throws Exception {
//		StandardApplicationRunner.run(new SimpleApp(), args);
		
		ClientDavParameters parameters = new ClientDavParameters();
		ClientDavConnection connection = new ClientDavConnection(parameters);
		connection.connect();
		connection.login("Tester", "geheim");
		
		System.err.println("Verbunden: " + connection.isConnected());
		System.err.println("Angemeldet: " + connection.isLoggedIn());
		
		UserAdministration userAdministration = connection.getDataModel().getUserAdministration();
//		userAdministration.changeUserPassword("Tester", "geheim", "TestDatenverteilerBenutzer" , "geheim");
		
		boolean userAdmin = userAdministration.isUserAdmin("TestDatenverteilerBenutzer", "geheim", "TestDatenverteilerBenutzer");
		System.err.println("Admin: " + userAdmin);
		
		System.exit(0);
	}

}
