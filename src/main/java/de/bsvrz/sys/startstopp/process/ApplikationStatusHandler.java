package de.bsvrz.sys.startstopp.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

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

public class ApplikationStatusHandler
		implements DynamicObjectCreatedListener, InvalidationListener, ClientReceiverInterface {

	private class ApplikationStatus {

		private String name;
		private SystemObject appObj;
		private boolean fertig;

		public ApplikationStatus(String name, SystemObject appObj, boolean fertig) {
			this.name = name;
			this.appObj = appObj;
			this.fertig = fertig;
		}

		@Override
		public String toString() {
			return "ApplikationStatus [name=" + name + ", appObj=" + appObj + ", fertig=" + fertig + "]";
		}
	}

	private ProcessManager processManager;
	private DynamicObjectType applikationTyp = null;
	private DataDescription applikationsFertigMeldungDesc;
	private ClientDavConnection dav;
	private Map<String, ApplikationStatus> applikationStatus = new LinkedHashMap<>();

	public ApplikationStatusHandler(ProcessManager processManager) {
		this.processManager = processManager;
	}

	public void reconnect(Optional<ClientDavConnection> connection) {

		Collection<ApplikationStatus> statusValues = new ArrayList<>(applikationStatus.values());
		applikationStatus.clear();
		for (ApplikationStatus status : statusValues) {
			disconnectApplikation(status.appObj);
		}

		if (connection.isPresent()) {
			dav = connection.get();
			DataModel dataModel = dav.getDataModel();
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
		dav.subscribeReceiver(this, appObj, applikationsFertigMeldungDesc, ReceiveOptions.normal(),
				ReceiverRole.receiver());
		ResultData resultData = dav.getData(appObj, applikationsFertigMeldungDesc, 0);
		if (resultData.hasData()) {
			updateApplikationStatus(appObj, resultData.getData());
		}
	}

	@Override
	public void invalidObject(DynamicObject dynamicObject) {
		disconnectApplikation(dynamicObject);
	}

	private void disconnectApplikation(SystemObject appObj) {
		dav.unsubscribeReceiver(this, appObj, applikationsFertigMeldungDesc);
		Iterator<ApplikationStatus> iterator = applikationStatus.values().iterator();
		while (iterator.hasNext()) {
			ApplikationStatus status = iterator.next();
			if (status.appObj.equals(appObj)) {
				iterator.remove();
			}
		}
	}

	@Override
	public void objectCreated(DynamicObject createdObject) {
		connectApplikation(createdObject);
	}

	@Override
	public void update(ResultData[] results) {
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

		if (!name.isEmpty()) {
			ApplikationStatus status = new ApplikationStatus(name, appObj, fertig);
			applikationStatus.put(name, status);
			if (name.startsWith(processManager.getInkarnationsPrefix())) {
				processManager.updateFromDav(name.substring(processManager.getInkarnationsPrefix().length()),
						status.fertig);
			}
		}
	}
}
