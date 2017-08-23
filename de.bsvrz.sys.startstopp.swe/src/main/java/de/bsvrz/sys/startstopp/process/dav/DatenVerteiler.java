package de.bsvrz.sys.startstopp.process.dav;

import java.util.LinkedHashSet;
import java.util.Set;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.Data.Array;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.config.StartStoppException;

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
		// TODO Status auswerten
	}

	@Override
	public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
		return true;
	}
}