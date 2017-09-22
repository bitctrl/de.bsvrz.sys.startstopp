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

import java.util.LinkedHashSet;
import java.util.Set;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.Data.Array;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.StartStoppException;

class DatenVerteiler implements ClientReceiverInterface, ClientSenderInterface {

	private static final Debug LOGGER = Debug.getLogger();
	private DataDescription applikationenDesc;
	private SystemObject datenVerteilerObj;
	private ClientDavConnection dav;
	private Set<SystemObject> applikationen = new LinkedHashSet<>();
	private DataDescription terminierungsDesc;
	private boolean subscription;

	DatenVerteiler(ClientDavConnection dav, SystemObject dvObj) {

		this.dav = dav;
		this.datenVerteilerObj = dvObj;

		DataModel dataModel = dav.getDataModel();
		AttributeGroup atg = dataModel.getAttributeGroup("atg.angemeldeteApplikationen");
		Aspect asp = dataModel.getAspect("asp.standard");
		applikationenDesc = new DataDescription(atg, asp);
		dav.subscribeReceiver(this, datenVerteilerObj, applikationenDesc, ReceiveOptions.normal(),
				ReceiverRole.receiver());

		atg = dataModel.getAttributeGroup("atg.terminierung");
		asp = dataModel.getAspect("asp.anfrage");
		terminierungsDesc = new DataDescription(atg, asp);
		try {
			dav.subscribeSender(this, datenVerteilerObj, terminierungsDesc, SenderRole.sender());
			subscription = true;
		} catch (OneSubscriptionPerSendData e) {
			LOGGER.warning(e.getLocalizedMessage());
		}
	}

	public void disconnect() {
		if (subscription) {
			dav.unsubscribeSender(this, datenVerteilerObj, terminierungsDesc);
		}
		dav.unsubscribeReceiver(this, datenVerteilerObj, applikationenDesc);
	}

	@Override
	public void update(ResultData[] results) {
		for (ResultData result : results) {
			if (result.hasData()) {
				Array appArray = result.getData().getArray("angemeldeteApplikation");
				for (int idx = 0; idx < appArray.getLength(); idx++) {
					applikationen.add(appArray.getItem(idx).getReferenceValue("applikation").getSystemObject());
				}
			} else {
				applikationen.clear();
			}
		}
	}

	public boolean sendeTerminierung(SystemObject appObj) throws StartStoppException {

		if (terminierungsDesc == null) {
			throw new StartStoppException("Datenverteiler " + datenVerteilerObj
					+ " ist noch nicht bereit zum Empfang von Terminierungsmeldungen");
		}

		for (SystemObject applikation : applikationen) {
			if (applikation.equals(appObj)) {
				Data data = dav.createData(terminierungsDesc.getAttributeGroup());
				data.getReferenceArray("Applikationen").setLength(1);
				data.getReferenceArray("Applikationen").getReferenceValue(0).setSystemObject(appObj);
				try {
					dav.sendData(new ResultData(datenVerteilerObj, terminierungsDesc, dav.getTime(), data));
					return true;
				} catch (DataNotSubscribedException | SendSubscriptionNotConfirmed e) {
					throw new StartStoppException(e.getLocalizedMessage());
				}
			}
		}
		return false;
	}

	@Override
	public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
		// wird nicht ausgewertet
	}

	@Override
	public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
		return false;
	}
}