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

package de.bsvrz.sys.startstopp.process.dav;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.Usv;
import de.bsvrz.sys.startstopp.process.ProzessManager;
import de.bsvrz.sys.startstopp.process.ProzessManager.StartStoppMode;
import de.bsvrz.sys.startstopp.startstopp.StartStopp;

public class UsvHandler implements ClientReceiverInterface {

	private static final Debug LOGGER = Debug.getLogger();
	private ProzessManager prozessManager;
	private ClientDavConnection connection;
	private Usv usv;
	private SystemObject usvObject;
	private DataDescription usvDesc;

	public UsvHandler(ProzessManager prozessManager) {
		this.prozessManager = prozessManager;
		this.usv = prozessManager.getUsv();
	}

	public void reconnect(ClientDavConnection newConnection) {

		if( usv != null) {
			disconnectUsv();
		}
		
		this.connection = newConnection;
		this.usv = prozessManager.getUsv();
		connectUsv();
	}

	private void connectUsv() {
		if( connection != null && usv != null && usv.getPid() != null) {
			usvObject = connection.getDataModel().getObject(usv.getPid());
			if( usvObject != null) {
				
				if( usvObject.isOfType("typ.usv")) {
					AttributeGroup atg = connection.getDataModel().getAttributeGroup("atg.usvZustandKritisch");
					Aspect asp = connection.getDataModel().getAspect("asp.zustand");
					usvDesc = new DataDescription(atg, asp);
					connection.subscribeReceiver(this, usvObject, usvDesc, ReceiveOptions.normal(), ReceiverRole.receiver());
				} else {
					LOGGER.error("Das Objekt mit der PID: " + usv.getPid() + " ist nicht vom Typ \"typ.usv\"");
					usvObject = null;
				}
			}
		}
	}

	private void disconnectUsv() {
		if( connection != null && usvObject != null && usvDesc != null) {
			connection.unsubscribeReceiver(this, usvObject, usvDesc);
		}
	}

	@Override
	public void update(ResultData[] results) {
		for( ResultData result : results) {
			if( result.hasData()) {
				int zustand = result.getData().getUnscaledValue("KritischerZustand").intValue();
				if( zustand == 1) {
					StartStopp.getInstance().stoppStartStoppApplikation(StartStoppMode.SKRIPT);
					return;
				}
			}
		}
	}
}
