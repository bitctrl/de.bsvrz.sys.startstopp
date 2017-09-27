package de.bsvrz.sys.startstopp.process.dav;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.Data.Array;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation.Status;
import de.bsvrz.sys.startstopp.api.jsonschema.StartArt.Option;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung.Warteart;
import de.bsvrz.sys.startstopp.api.jsonschema.StartFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;
import de.bsvrz.sys.startstopp.process.OnlineApplikation;
import de.bsvrz.sys.startstopp.process.ProzessManager;
import de.bsvrz.sys.startstopp.startstopp.StartStopp;

public class StartStoppKonfigurationProvider implements ClientSenderInterface {

	private static final Debug LOGGER = Debug.getLogger();

	private ProzessManager prozessManager;
	private ClientDavConnection connection;
	private SystemObject rechner;

	private DataDescription procInfoDesc;
	private DataDescription startStoppInfoDesc;

	private String prefix = "";

	public void reconnect(ProzessManager prozessManager, ClientDavConnection connection, SystemObject rechner) {

		disconnect();

		prefix = StartStopp.getInstance().getInkarnationsPrefix();
		prefix = prefix.substring(0, prefix.length() - 1);

		this.prozessManager = prozessManager;
		this.connection = connection;
		this.rechner = rechner;

		if (rechner == null) {
			procInfoDesc = null;
			startStoppInfoDesc = null;
			LOGGER.warning(
					"Der lokale Rechner konnte nicht ermittelt werden. Es werden keine Prozessinformationen publiziert!");
		} else {

			DataModel dataModel = connection.getDataModel();
			AttributeGroup atg = dataModel.getAttributeGroup("atg.prozessInfo");
			Aspect asp = dataModel.getAspect("asp.zustand");

			if ((atg != null) && (asp != null)) {
				procInfoDesc = new DataDescription(atg, asp);
				try {
					connection.subscribeSender(this, rechner, procInfoDesc, SenderRole.source());
				} catch (OneSubscriptionPerSendData e) {
					LOGGER.fine(e.getLocalizedMessage());
				}
			} else {
				procInfoDesc = null;
				LOGGER.warning(
						"Die Attributgruppe zum Versand von Prozessinformationen ist nicht ermittelbar. Es werden keine Prozessinformationen publiziert!");
			}

			atg = dataModel.getAttributeGroup("atg.startStoppInfo");
			asp = dataModel.getAspect("asp.zustand");

			if ((atg != null) && (asp != null)) {
				startStoppInfoDesc = new DataDescription(atg, asp);
				try {
					connection.subscribeSender(this, rechner, startStoppInfoDesc, SenderRole.source());
				} catch (OneSubscriptionPerSendData e) {
					LOGGER.fine(e.getLocalizedMessage());
				}
			} else {
				procInfoDesc = null;
				LOGGER.warning(
						"Die Attributgruppe zum Versand von StartStopp-Informationen ist nicht ermittelbar. Es werden keine Prozessinformationen publiziert!");
			}
		}
	}

	private void disconnect() {
		if ((connection != null) && (rechner != null)) {
			if (procInfoDesc != null) {
				connection.unsubscribeSender(this, rechner, procInfoDesc);
			}
			if (startStoppInfoDesc != null) {
				connection.unsubscribeSender(this, rechner, startStoppInfoDesc);
			}
		}
	}

	public void update(Collection<OnlineApplikation> applikationen) {
		CompletableFuture.runAsync(() -> sendInfo(new ArrayList<>(applikationen)));
	}

	private Object sendInfo(Collection<OnlineApplikation> applikationen) {

		sendProzessInfo(applikationen);
		sendStartStoppInfo(applikationen);

		return true;
	}

	private void sendProzessInfo(Collection<OnlineApplikation> applikationen) {

		if (procInfoDesc == null) {
			return;
		}

		Data infoData = connection.createData(procInfoDesc.getAttributeGroup());
		Array prozessArray = infoData.getArray("Prozesse");
		prozessArray.setLength(applikationen.size());

		int idx = 0;
		for (OnlineApplikation applikation : applikationen) {
			fuelleInformationen(prozessArray.getItem(idx++), applikation);
		}

		if (infoData.isDefined()) {
			try {
				connection.sendData(new ResultData(rechner, procInfoDesc, connection.getTime(), infoData));
			} catch (DataNotSubscribedException | SendSubscriptionNotConfirmed e) {
				LOGGER.fine(e.getLocalizedMessage());
			}
		} else {
			LOGGER.fine("Prozessdaten sind unvollständig: " + infoData);
		}
	}

	private void sendStartStoppInfo(Collection<OnlineApplikation> applikationen) {

		if (startStoppInfoDesc == null) {
			return;
		}

		Data infoData = connection.createData(startStoppInfoDesc.getAttributeGroup());
		Array bloecke = infoData.getArray("StartStoppBloecke");
		bloecke.setLength(1);

		Data block = bloecke.getItem(0);

		String prefix = StartStopp.getInstance().getInkarnationsPrefix();
		prefix = prefix.substring(0, prefix.length() - 1);

		block.getTextValue("StartStoppID").setText(prefix);
		block.getUnscaledValue("Zustand").setText(getProzessManagerStatusText());
		block.getTextValue("StartZeitpunkt")
				.setText(DateFormat.getDateTimeInstance().format(prozessManager.getStartzeit()));

		Array inkarnationenArray = block.getArray("Inkarnationen");
		inkarnationenArray.setLength(applikationen.size());

		int idx = 0;
		for (OnlineApplikation applikation : applikationen) {
			inkarnationenArray.getItem(idx++).getTextValue("ProzessID").setText(prefix + "_" + applikation.getName());
		}

		if (infoData.isDefined()) {
			try {
				connection.sendData(new ResultData(rechner, startStoppInfoDesc, connection.getTime(), infoData));
			} catch (DataNotSubscribedException | SendSubscriptionNotConfirmed e) {
				LOGGER.fine(e.getLocalizedMessage());
			}
		} else {
			LOGGER.fine("StartStopp-Daten sind unvollständig: " + infoData);
		}
	}

	private String getProzessManagerStatusText() {
		String result = null;
		switch (prozessManager.getStatus()) {
		case CONFIGERROR:
			result = "Fehler";
			break;
		case RUNNING:
		case RUNNING_CANCELED:
			result = "gestartet";
			break;
		case SHUTDOWN:
		case STOPPED:
		case STOPPING:
		case STOPPING_CANCELED:
			result = "gestopped";
			break;
		case INITIALIZED:
		default:
			result = "angelegt";
			break;
		}
		return result;
	}

	private void fuelleInformationen(Data item, OnlineApplikation applikation) {
		item.getTextValue("ProzessID").setText(prefix + "_" + applikation.getName());
		item.getTextValue("Name").setText(applikation.getName());
		item.getTextValue("AusfuehrbareDatei").setText(applikation.getApplikation().getInkarnation().getApplikation());
		item.getUnscaledValue("SimulationsVariante").set(0);
		item.getUnscaledValue("Zustand").setText(getZustandsText(applikation.getStatus()));
		item.getTextValue("StartZeitpunkt")
				.setText(Util.nonEmptyString(applikation.getApplikation().getLetzteStartzeit()));
		item.getTextValue("StoppZeitpunkt")
				.setText(Util.nonEmptyString(applikation.getApplikation().getLetzteStoppzeit()));
		item.getTextValue("InitialisierungsZeitpunkt")
				.setText(Util.nonEmptyString(applikation.getApplikation().getLetzteInitialisierung()));
		item.getTextValue("NächsterStartZeitpunkt").setText(applikation.getNextStart());

		List<String> aufrufParameter = applikation.getApplikation().getInkarnation().getAufrufParameter();
		Array parameterArray = item.getArray("AufrufParameter");
		parameterArray.setLength(aufrufParameter.size());
		int idx = 0;
		for (String parameter : aufrufParameter) {
			parameterArray.getItem(idx++).getTextValue("AufrufparameterWert").setText(parameter);
		}

		item.getItem("StartArt").getUnscaledValue("OptionStart")
				.setText(getStartOptionText(applikation.getStartArtOption()));
		item.getItem("StartArt").getUnscaledValue("NeuStart")
				.setText(applikation.getApplikation().getInkarnation().getStartArt().getNeuStart() ? "Ja" : "Nein");
		item.getItem("StartArt").getTextValue("Intervall")
				.setText(applikation.getApplikation().getInkarnation().getStartArt().getIntervall());

		fuelleStartBedingung(item, applikation);
		fuelleStoppBedingung(item, applikation);

		item.getItem("StandardAusgabe").getTextValue("OptionStandardAusgabe").setText("ignorieren");
		item.getItem("StandardAusgabe").getTextValue("Datei").setText("");

		item.getItem("FehlerAusgabe").getTextValue("OptionFehlerAusgabe").setText("ignorieren");
		item.getItem("FehlerAusgabe").getTextValue("Datei").setText("");

		fuelleStartVerhaltenFehler(item, applikation);
		fuelleStoppVerhaltenFehler(item, applikation);

	}

	private void fuelleStartBedingung(Data item, OnlineApplikation applikation) {
		StartBedingung startBedingung = applikation.getStartBedingung();

		if (startBedingung != null) {
			Array bedingungen = item.getArray("StartBedingung");
			bedingungen.setLength(1);

			Data bedingung = bedingungen.getItem(0);
			bedingung.getTextValue("Vorgaenger").setText(String.join(",", startBedingung.getVorgaenger()));
			bedingung.getUnscaledValue("WarteArt").setText(getWarteArtText(startBedingung.getWarteart()));
			bedingung.getTextValue("Rechner").setText(Util.nonEmptyString(startBedingung.getRechner()));

			long warteZeit;
			try {
				warteZeit = Util.convertToWarteZeitInMsec(Util.nonEmptyString(startBedingung.getWartezeit(), "0"));
				bedingung.getTimeValue("WarteZeit").setMillis(warteZeit);
			} catch (@SuppressWarnings("unused") StartStoppException e) {
				bedingung.getTimeValue("WarteZeit").setMillis(0);
			}
		}
	}

	private void fuelleStoppBedingung(Data item, OnlineApplikation applikation) {

		StoppBedingung stoppBedingung = applikation.getStoppBedingung();

		if (stoppBedingung != null) {
			Array bedingungen = item.getArray("StoppBedingung");
			bedingungen.setLength(1);

			Data bedingung = bedingungen.getItem(0);
			bedingung.getTextValue("Nachfolger").setText(String.join(",", stoppBedingung.getNachfolger()));
			bedingung.getTextValue("Rechner").setText(Util.nonEmptyString(stoppBedingung.getRechner()));

			long warteZeit;
			try {
				warteZeit = Util.convertToWarteZeitInMsec(Util.nonEmptyString(stoppBedingung.getWartezeit(), "0"));
				bedingung.getTimeValue("WarteZeit").setMillis(warteZeit);
			} catch (@SuppressWarnings("unused") StartStoppException e) {
				bedingung.getTimeValue("WarteZeit").setMillis(0);
			}
		}
	}

	private void fuelleStartVerhaltenFehler(Data item, OnlineApplikation applikation) {

		StartFehlerVerhalten startFehlerVerhalten = applikation.getApplikation().getInkarnation()
				.getStartFehlerVerhalten();

		if (startFehlerVerhalten != null) {
			item.getItem("StartVerhaltenFehler").getUnscaledValue("StartVerhaltenFehlerOption")
					.setText(getStartFehlerOptionText(startFehlerVerhalten.getOption()));
			item.getItem("StartVerhaltenFehler").getUnscaledValue("Wiederholrate")
					.set(Integer.parseInt(Util.nonEmptyString(startFehlerVerhalten.getWiederholungen(), "0")));
		}
	}

	private void fuelleStoppVerhaltenFehler(Data item, OnlineApplikation applikation) {

		StoppFehlerVerhalten stoppFehlerVerhalten = applikation.getApplikation().getInkarnation()
				.getStoppFehlerVerhalten();

		if (stoppFehlerVerhalten != null) {
			item.getItem("StoppVerhaltenFehler").getUnscaledValue("StoppVerhaltenFehlerOption")
					.setText(getStoppFehlerOptionText(stoppFehlerVerhalten.getOption()));
			item.getItem("StoppVerhaltenFehler").getUnscaledValue("Wiederholrate")
					.set(Integer.parseInt(Util.nonEmptyString(stoppFehlerVerhalten.getWiederholungen(), "0")));
		}
	}

	private String getZustandsText(Status status) {

		String result = null;

		switch (status) {
		case GESTARTET:
			result = "gestartet";
			break;
		case GESTOPPT:
			result = "gestoppt";
			break;
		case INITIALISIERT:
			result = "initialisiert";
			break;
		case INSTALLIERT:
			result = "angelegt";
			break;
		case STARTENWARTEN:
			result = "warte Startbedingung";
			break;
		case STOPPENWARTEN:
			result = "warte Stoppbedingung";
			break;
		default:
			result = "Fehler";
			break;
		}
		return result;
	}

	private String getStartOptionText(Option startArtOption) {

		String result = null;

		switch (startArtOption) {
		case INTERVALLABSOLUT:
		case INTERVALLRELATIV:
			result = "intervall";
			break;
		case MANUELL:
			result = "manuell";
			break;
		case AUTOMATISCH:
		default:
			result = "automatisch";
			break;
		}
		return result;
	}

	private String getWarteArtText(Warteart warteart) {
		String result = null;
		switch (warteart) {
		case BEGINN:
			result = "Beginn";
			break;
		case ENDE:
		default:
			result = "Ende";
			break;
		}
		return result;
	}

	private String getStartFehlerOptionText(StartFehlerVerhalten.Option option) {
		String result = null;
		switch (option) {
		case ABBRUCH:
			result = "Abbruch";
			break;
		case BEENDEN:
			result = "beenden";
			break;
		case IGNORIEREN:
		default:
			result = "ignorieren";
			break;
		}
		return result;
	}

	private String getStoppFehlerOptionText(StoppFehlerVerhalten.Option option) {
		String result = null;
		switch (option) {
		case ABBRUCH:
			result = "Abbruch";
			break;
		case STOPP:
			result = "Stopp";
			break;
		case IGNORIEREN:
		default:
			result = "ignorieren";
			break;
		}
		return result;
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
