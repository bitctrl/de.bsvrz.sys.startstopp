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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.DynamicObjectType.DynamicObjectCreatedListener;
import de.bsvrz.dav.daf.main.config.InvalidationListener;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.process.ProzessManager;
import de.bsvrz.sys.startstopp.startstopp.StartStopp;

class ApplikationStatusHandler implements DynamicObjectCreatedListener, InvalidationListener, ClientReceiverInterface {

	private static class ApplikationStatus {

		private String name;
		private SystemObject appObj;
		private boolean fertig;

		ApplikationStatus(String name, SystemObject appObj, boolean fertig) {
			this.name = name;
			this.appObj = appObj;
			this.fertig = fertig;
		}

		@Override
		public String toString() {
			return "ApplikationStatus [name=" + name + ", appObj=" + appObj + ", fertig=" + fertig + "]";
		}
	}

	private ProzessManager processManager;
	private DynamicObjectType applikationTyp;
	private DataDescription applikationsFertigMeldungDesc;
	private ClientDavConnection dav;
	private Map<String, ApplikationStatus> applikationStatus = new LinkedHashMap<>();
	private Set<DatenVerteiler> datenVerteiler = new LinkedHashSet<>();
	private String inkarnationsPrefix;

	ApplikationStatusHandler(ProzessManager processManager) {
		this(StartStopp.getInstance(), processManager);
	}

	ApplikationStatusHandler(StartStopp startstopp, ProzessManager processManager) {
		this.processManager = processManager;
		this.inkarnationsPrefix = startstopp.getInkarnationsPrefix();
	}

	public void terminiereAppPerDav(String name) throws StartStoppException {
		ApplikationStatus status = applikationStatus.get(name);
		if (status == null) {
			throw new StartStoppException("Die Applikation \"" + name + "\" ist nicht bekannt!");
		}

		for (DatenVerteiler dv : datenVerteiler) {
			if (dv.sendeTerminierung(status.appObj)) {
				return;
			}
		}

		throw new StartStoppException(
				"Es konnte kein Datenverteiler zum Versenden der Terminierungsnachricht an die Applikation \"" + name
						+ "\" ermittelt werden!");
	}

	public void reconnect(ClientDavConnection connection) {

		Collection<ApplikationStatus> statusValues = new ArrayList<>(applikationStatus.values());
		applikationStatus.clear();
		statusValues.forEach(app -> disconnectApplikation(app.appObj));

		datenVerteiler.forEach(datenverteiler -> datenverteiler.disconnect());
		datenVerteiler.clear();

		if (connection != null) {
			dav = connection;
			DataModel dataModel = dav.getDataModel();

			SystemObjectType datenVerteilerTyp = dataModel.getType("typ.datenverteiler");
			datenVerteilerTyp.getElements().forEach(dvObj -> datenVerteiler.add(new DatenVerteiler(dav, dvObj)));

			applikationTyp = (DynamicObjectType) dataModel.getType("typ.applikation");
			AttributeGroup atg = dataModel.getAttributeGroup("atg.applikationsFertigmeldung");
			Aspect asp = dataModel.getAspect("asp.standard");
			applikationsFertigMeldungDesc = new DataDescription(atg, asp);

			applikationTyp.addObjectCreationListener(this);
			applikationTyp.addInvalidationListener(this);

			for (SystemObject appObj : applikationTyp.getElements()) {
				connectApplikation(appObj);
			}
		}
	}

	private void connectApplikation(SystemObject appObj) {
		if (dav == null) {
			Debug.getLogger().warning("Es besteht keine Datenverteilerverbindung! Applikation " + appObj
					+ " kann nicht registriert werden!");
		} else if (applikationsFertigMeldungDesc == null) {
			Debug.getLogger().warning(
					"Die Datenbeschreibung für Applikationsfertigmeldungen wurde nicht initialisiert! Applikation "
							+ appObj + " kann nicht registriert werden!");
		} else {
			dav.subscribeReceiver(this, appObj, applikationsFertigMeldungDesc, ReceiveOptions.normal(),
					ReceiverRole.receiver());
		}
	}

	@Override
	public void invalidObject(DynamicObject dynamicObject) {
		disconnectApplikation(dynamicObject);
	}

	private void disconnectApplikation(SystemObject appObj) {
		if (dav == null) {
			Debug.getLogger().warning("Es besteht keine Datenverteilerverbindung! Applikation " + appObj
					+ " kann nicht abgemeldet werden!");
		} else if (applikationsFertigMeldungDesc == null) {
			Debug.getLogger().warning(
					"Die Datenbeschreibung für Applikationsfertigmeldungen wurde nicht initialisiert! Applikation "
							+ appObj + " kann nicht abgemeldet werden!");
		} else {
			dav.unsubscribeReceiver(this, appObj, applikationsFertigMeldungDesc);
			Iterator<ApplikationStatus> iterator = applikationStatus.values().iterator();
			while (iterator.hasNext()) {
				ApplikationStatus status = iterator.next();
				if (status.appObj.equals(appObj)) {
					iterator.remove();
				}
			}
		}
	}

	@Override
	public void objectCreated(DynamicObject createdObject) {
		connectApplikation(createdObject);
	}

	@Override
	public void update(ResultData[] results) {

		if (applikationsFertigMeldungDesc == null) {
			Debug.getLogger()
					.warning("Die Datenbeschreibung für Applikationsfertigmeldungen wurde nicht initialisiert!");
			return;
		}

		for (ResultData resultData : results) {
			Data data = resultData.getData();
			if (data == null) {
				continue;
			}

			if (resultData.getDataDescription().getAttributeGroup()
					.equals(applikationsFertigMeldungDesc.getAttributeGroup())) {
				SystemObject appObj = resultData.getObject();
				updateApplikationStatus(appObj, data);
			}
		}
	}

	private void updateApplikationStatus(SystemObject appObj, Data data) {
		String name = data.getTextValue("Inkarnationsname").getText();
		boolean fertig = data.getUnscaledValue("InitialisierungFertig").intValue() == 1;

		Debug.getLogger().info("Aktualisierung vom Dav: " + appObj + ": \"" + name + "\" ist fertig: " + fertig);

		if (!name.isEmpty()) {
			ApplikationStatus status = new ApplikationStatus(name, appObj, fertig);
			applikationStatus.put(name, status);
			if (name.startsWith(inkarnationsPrefix)) {
				String processMgrInkarnation = name.substring(inkarnationsPrefix.length());
				Debug.getLogger().info("Aktualisiere Prozessmanager: " + processMgrInkarnation);
				processManager.updateFromDav(processMgrInkarnation, status.fertig);
			}
		}
	}

	public void disconnect() {
		for( String name : applikationStatus.keySet()) {
			if (name.startsWith(inkarnationsPrefix)) {
				String processMgrInkarnation = name.substring(inkarnationsPrefix.length());
				Debug.getLogger().info("Aktualisiere Prozessmanager: " + processMgrInkarnation);
				processManager.updateFromDav(processMgrInkarnation, false);
			}
		}
		reconnect(null);
	}
}
