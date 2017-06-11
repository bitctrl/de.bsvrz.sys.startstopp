package de.bsvrz.sys.startstopp.api;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.bsvrz.sys.startstopp.api.jsonschema.Global;
import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnationen;
import de.bsvrz.sys.startstopp.api.jsonschema.Kernsysteme;
import de.bsvrz.sys.startstopp.api.jsonschema.Makrodefinitionen;
import de.bsvrz.sys.startstopp.api.jsonschema.MetaDaten;
import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.api.jsonschema.StartArt;
import de.bsvrz.sys.startstopp.api.jsonschema.StartFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppKonfiguration;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.ZugangDav;
import de.bsvrz.sys.startstopp.data.StartArt.StartArtOption;
import de.bsvrz.sys.startstopp.data.StartFehlerVerhalten.StartFehlerVerhaltenOption;

public class StartStoppKonfigurationParser {

	private static class StartStoppParserHandler extends DefaultHandler {

		private enum Tags {
			tagIsUndefined, konfiguration, startStopp, global, makrodefinition, kernsystem, zugangdav, rechner,
			protokolldatei, applikationen, inkarnation, applikation, aufrufparameter, startart, standardAusgabe,
			standardFehlerAusgabe, startFehlerverhalten, stoppFehlerverhalten, usv, startbedingung, stoppbedingung;

			static Tags getTag(String tagStr) {
				for (Tags tag : values()) {
					if (tag.name().equalsIgnoreCase(tagStr)) {
						return tag;
					}
				}
				return Tags.tagIsUndefined;
			}
		};

		private StartStoppKonfiguration destination;
		private Inkarnationen currentInkarnation;

		StartStoppParserHandler(StartStoppKonfiguration destination) {
			this.destination = destination;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {

			switch (Tags.getTag(qName)) {
			case applikation:
				currentInkarnation.setApplikation(attributes.getValue("name"));
				break;
			case applikationen:
				break;
			case aufrufparameter:
				currentInkarnation.getAufrufParameter().add(attributes.getValue("wert"));
				break;
			case global:
				break;
			case inkarnation:
				currentInkarnation = new Inkarnationen();
				currentInkarnation.setInkarnationsName(attributes.getValue("name"));
				break;
			case kernsystem:
				Kernsysteme kernsysteme = new Kernsysteme();
				kernsysteme.setInkarnationsName(attributes.getValue("inkarnationsname"));
				if (attributes.getValue("wartezeit") != null) {
					// TODO Schema vervollständigen
					// kernsysteme.setWarteZeit(attributes.getValue("wartezeit"));
				}
				if (attributes.getValue("mitInkarnationsname") != null) {
					kernsysteme.setMitInkarnationsName(attributes.getValue("mitInkarnationsname").equals("ja"));
				}
				if( destination.getGlobal() == null) {
					destination.setGlobal(new Global());
				}
				destination.getGlobal().getKernsysteme().add(kernsysteme);
				break;
			case konfiguration:
				break;
			case makrodefinition:
				Makrodefinitionen makrodefinitionen = new Makrodefinitionen();
				makrodefinitionen.setName(attributes.getValue("name"));
				makrodefinitionen.setWert(attributes.getValue("wert"));
				break;
			case protokolldatei:
				break;
			case rechner:
				Rechner rechner = new Rechner();
				rechner.setName(attributes.getValue("name"));
				rechner.setTcpAdresse(attributes.getValue("tcpAdresse"));
				destination.getGlobal().getRechner().add(rechner);
				break;
			case standardAusgabe:
				break;
			case standardFehlerAusgabe:
				break;
			case startart:
				if( currentInkarnation.getStartArt() == null) {
					currentInkarnation.setStartArt(new StartArt());
				}
				
				if (attributes.getValue("option") != null) {
					currentInkarnation.getStartArt()
							.setOption(StartArt.Option.fromValue(attributes.getValue("option")));
				}
				if (attributes.getValue("neustart") != null) {
					currentInkarnation.getStartArt().setNeuStart(attributes.getValue("neustart").equals("ja"));
				}
				if (attributes.getValue("intervall") != null) {
					currentInkarnation.getStartArt().setIntervall(attributes.getValue("intervall"));
				}
				break;
			case startFehlerverhalten:
				if( currentInkarnation.getStartFehlerVerhalten() == null) {
					currentInkarnation.setStartFehlerVerhalten(new StartFehlerVerhalten());
				}
				if (attributes.getValue("option") != null) {
					currentInkarnation.getStartFehlerVerhalten().setOption(StartFehlerVerhalten.Option.valueOf(attributes.getValue("option")));
				}
				if (attributes.getValue("wiederholungen") != null) {
					currentInkarnation.getStartFehlerVerhalten()
							.setWiederholungen(Double.parseDouble(attributes.getValue("wiederholungen")));
				}
				break;
			case startStopp:
				if( destination.getMetaDaten() == null) {
					destination.setMetaDaten(new MetaDaten());
				}
				destination.getMetaDaten().setVersionsNummer(attributes.getValue("Versionsnummer"));
				destination.getMetaDaten().setErstelltAm(attributes.getValue("ErstelltAm"));
				destination.getMetaDaten().setErstelltDurch(attributes.getValue("ErstelltDurch"));
				destination.getMetaDaten().setAenderungsGrund(attributes.getValue("Aenderungsgrund"));
				break;
			case stoppFehlerverhalten:
				if( currentInkarnation.getStoppFehlerVerhalten() == null) {
					currentInkarnation.setStoppFehlerVerhalten(new StoppFehlerVerhalten());
				}
				
				if (attributes.getValue("option") != null) {
					currentInkarnation.getStoppFehlerVerhalten().setOption(StoppFehlerVerhalten.Option.valueOf(attributes.getValue("option")));
				}
				if (attributes.getValue("wiederholungen") != null) {
					currentInkarnation.getStoppFehlerVerhalten()
							.setWiederholungen(Double.parseDouble(attributes.getValue("wiederholungen")));
				}
				break;
			case usv:
				// TODO Im Schema ergänzen
				// destination.getGlobal().setUsv(attributes.getValue("pid"));
				break;
			case zugangdav:
				if( destination.getGlobal() == null) {
					destination.setGlobal(new Global());
				}
				if( destination.getGlobal().getZugangDav() == null) {
					destination.getGlobal().setZugangDav(new ZugangDav());
				}
				destination.getGlobal().getZugangDav().setAdresse(attributes.getValue("adresse"));
				destination.getGlobal().getZugangDav().setPort(attributes.getValue("port"));
				destination.getGlobal().getZugangDav().setUserName(attributes.getValue("username"));
				destination.getGlobal().getZugangDav().setPassWord(attributes.getValue("passwort"));
				break;
			case startbedingung:
				// TODO Schema ergänzen
//				StartBedingung startBedingung = new StartBedingung(attributes.getValue("vorgaenger"));
//				if (attributes.getValue("warteart") != null) {
//					startBedingung.setWarteArt(StartBedingung.WarteArt.getWarteArt(attributes.getValue("warteart")));
//				}
//				if (attributes.getValue("rechner") != null) {
//					startBedingung.setRechner(attributes.getValue("rechner"));
//				}
//				if (attributes.getValue("wartezeit") != null) {
//					startBedingung.setWarteZeit(attributes.getValue("wartezeit"));
//				}
//				currentInkarnation.addStartBedingung(startBedingung);
				break;
			case stoppbedingung:
				// TODO Schema ergänzen
//				StoppBedingung stoppBedingung = new StoppBedingung(attributes.getValue("nachfolger"));
//				if (attributes.getValue("rechner") != null) {
//					stoppBedingung.setRechner(attributes.getValue("rechner"));
//				}
//				if (attributes.getValue("wartezeit") != null) {
//					stoppBedingung.setWarteZeit(attributes.getValue("wartezeit"));
//				}
//				currentInkarnation.addStoppBedingung(stoppBedingung);
//				break;
			case tagIsUndefined:
				throw new IllegalArgumentException("Tag not supported: " + qName);

			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			switch (Tags.getTag(qName)) {
			case applikation:
				break;
			case applikationen:
				break;
			case aufrufparameter:
				break;
			case global:
				break;
			case inkarnation:
				if (currentInkarnation != null) {
					destination.getInkarnationen().add(currentInkarnation);
					currentInkarnation = null;
				}
				break;
			case kernsystem:
				break;
			case konfiguration:
				break;
			case makrodefinition:
				break;
			case protokolldatei:
				break;
			case rechner:
				break;
			case standardAusgabe:
				break;
			case standardFehlerAusgabe:
				break;
			case startart:
				break;
			case startFehlerverhalten:
				break;
			case startStopp:
				break;
			case stoppFehlerverhalten:
				break;
			case tagIsUndefined:
				break;
			case usv:
				break;
			case zugangdav:
				break;
			case startbedingung:
				break;
			case stoppbedingung:
				break;
			}
		}
	}
	
	public static StartStoppKonfiguration getKonfigurationFrom(String resourceName) {
	
		StartStoppKonfiguration startStoppKonfiguration = new StartStoppKonfiguration();

		try (InputStream stream = StartStoppKonfigurationParser.class.getResourceAsStream(resourceName)) {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			saxParser.parse(stream, new StartStoppParserHandler(startStoppKonfiguration));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return startStoppKonfiguration;
	}
}
