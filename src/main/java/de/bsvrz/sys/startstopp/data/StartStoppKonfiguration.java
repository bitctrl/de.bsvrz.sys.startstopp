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

package de.bsvrz.sys.startstopp.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.bsvrz.sys.startstopp.data.StartArt.StartArtOption;
import de.bsvrz.sys.startstopp.data.StartFehlerVerhalten.StartFehlerVerhaltenOption;
import de.bsvrz.sys.startstopp.data.StoppFehlerVerhalten.StoppFehlerVerhaltenOption;

public class StartStoppKonfiguration implements StartStoppConfigurationElement {

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
		private Inkarnation currentInkarnation;

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
				currentInkarnation.addAufrufParameter(attributes.getValue("wert"));
				break;
			case global:
				break;
			case inkarnation:
				currentInkarnation = new Inkarnation(attributes.getValue("name"));
				break;
			case kernsystem:
				KernSystem kernSystem = new KernSystem(attributes.getValue("inkarnationsname"));
				if (attributes.getValue("wartezeit") != null) {
					kernSystem.setWarteZeit(attributes.getValue("wartezeit"));
				}
				if (attributes.getValue("mitInkarnationsname") != null) {
					kernSystem.setMitInkarnationsName(attributes.getValue("mitInkarnationsname").equals("ja"));
				}
				destination.global.addKernSystem(kernSystem);
				break;
			case konfiguration:
				break;
			case makrodefinition:
				destination.global.addMakroDefinition(attributes.getValue("name"), attributes.getValue("wert"));
				break;
			case protokolldatei:
				break;
			case rechner:
				destination.global.addRechner(attributes.getValue("name"), attributes.getValue("tcpAdresse"));
				break;
			case standardAusgabe:
				break;
			case standardFehlerAusgabe:
				break;
			case startart:
				if (attributes.getValue("option") != null) {
					currentInkarnation.getStartArt()
							.setOption(StartArtOption.getStartArtOption(attributes.getValue("option")));
				}
				if (attributes.getValue("neustart") != null) {
					currentInkarnation.getStartArt().setNeuStart(attributes.getValue("neustart").equals("ja"));
				}
				if (attributes.getValue("intervall") != null) {
					currentInkarnation.getStartArt().setIntervall(attributes.getValue("intervall"));
				}
				break;
			case startFehlerverhalten:
				if (attributes.getValue("option") != null) {
					currentInkarnation.getStartFehlerVerhalten().setOption(
							StartFehlerVerhaltenOption.getStartFehlerVerhaltenOption(attributes.getValue("option")));
				}
				if (attributes.getValue("wiederholungen") != null) {
					currentInkarnation.getStartFehlerVerhalten()
							.setWiederholungen(Integer.parseInt(attributes.getValue("wiederholungen")));
				}
				break;
			case startStopp:
				destination.setVersionsNummer(attributes.getValue("Versionsnummer"));
				destination.setErstelltAm(attributes.getValue("ErstelltAm"));
				destination.setErstelltDurch(attributes.getValue("ErstelltDurch"));
				destination.setAenderungsGrund(attributes.getValue("Aenderungsgrund"));
				break;
			case stoppFehlerverhalten:
				if (attributes.getValue("option") != null) {
					currentInkarnation.getStoppFehlerVerhalten().setOption(
							StoppFehlerVerhaltenOption.getStoppFehlerVerhaltenOption(attributes.getValue("option")));
				}
				if (attributes.getValue("wiederholungen") != null) {
					currentInkarnation.getStoppFehlerVerhalten()
							.setWiederholungen(Integer.parseInt(attributes.getValue("wiederholungen")));
				}
				break;
			case usv:
				destination.global.setUsv(attributes.getValue("pid"));
				break;
			case zugangdav:
				destination.global.getZugangDav().setAdresse(attributes.getValue("adresse"));
				destination.global.getZugangDav().setPort(attributes.getValue("port"));
				destination.global.getZugangDav().setUserName(attributes.getValue("username"));
				destination.global.getZugangDav().setPassWord(attributes.getValue("passwort"));
				break;
			case startbedingung:
				StartBedingung startBedingung = new StartBedingung(attributes.getValue("vorgaenger"));
				if (attributes.getValue("warteart") != null) {
					startBedingung.setWarteArt(StartBedingung.WarteArt.getWarteArt(attributes.getValue("warteart")));
				}
				if (attributes.getValue("rechner") != null) {
					startBedingung.setRechner(attributes.getValue("rechner"));
				}
				if (attributes.getValue("wartezeit") != null) {
					startBedingung.setWarteZeit(attributes.getValue("wartezeit"));
				}
				currentInkarnation.addStartBedingung(startBedingung);
				break;
			case stoppbedingung:
				StoppBedingung stoppBedingung = new StoppBedingung(attributes.getValue("nachfolger"));
				if (attributes.getValue("rechner") != null) {
					stoppBedingung.setRechner(attributes.getValue("rechner"));
				}
				if (attributes.getValue("wartezeit") != null) {
					stoppBedingung.setWarteZeit(attributes.getValue("wartezeit"));
				}
				currentInkarnation.addStoppBedingung(stoppBedingung);
				break;
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
					destination.addInkarnation(currentInkarnation);
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

	private String versionsNummer = "unknown";
	private String erstelltAm = "unknown";
	private String erstelltDurch = "unknown";
	private String aenderungsGrund = "unknown";

	private GlobalData global = new GlobalData();
	private Map<String, Inkarnation> inkarnationen = new LinkedHashMap<>();

	
	public StartStoppKonfiguration(InputStream stream) throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(stream, new StartStoppParserHandler(this));
	}

	public StartStoppKonfiguration(JSONObject json) {
		initFromJson(json);
	}

	private void addInkarnation(Inkarnation inkarnation) {
		inkarnationen.put(inkarnation.getInkarnationsName(), inkarnation);
	}

	public Collection<Inkarnation> getInkarnationen() {
		return inkarnationen.values();
	}

	public void saveToXmlFile(OutputStreamWriter outputStreamWriter) throws XMLStreamException, IOException,
			TransformerFactoryConfigurationError, ParserConfigurationException, SAXException, TransformerException {
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", true);

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(102400)) {
			XMLStreamWriter writer = outputFactory.createXMLStreamWriter(outputStream, "UTF-8");

			writer.writeStartDocument("UTF-8", null);
			writeXml(writer);
			writer.flush();
			writer.close();

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(new StringReader(outputStream.toString("UTF-8"))));
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, new StreamResult(outputStreamWriter));
		}

	}

	@Override
	public void writeXml(XMLStreamWriter destination) throws XMLStreamException {
		destination.writeStartElement("konfiguration");
		destination.writeStartElement("startStopp");

		global.writeXml(destination);

		destination.writeStartElement("applikationen");
		for (Inkarnation inkarnation : inkarnationen.values()) {
			inkarnation.writeXml(destination);
		}

		destination.writeEndElement();

		destination.writeEndElement();
		destination.writeEndElement();
	}

	public String getVersionsNummer() {
		return versionsNummer;
	}

	public void setVersionsNummer(String versionsNummer) {
		if (versionsNummer == null) {
			this.versionsNummer = "unknown";
		} else {
			this.versionsNummer = versionsNummer;
		}
	}

	public String getErstelltAm() {
		return erstelltAm;
	}

	public void setErstelltAm(String erstelltAm) {
		if (erstelltAm == null) {
			this.erstelltAm = "unknown";
		} else {
			this.erstelltAm = erstelltAm;
		}
	}

	public String getErstelltDurch() {
		return erstelltDurch;
	}

	public void setErstelltDurch(String erstelltDurch) {
		if (erstelltDurch == null) {
			erstelltDurch = "unknown";
		} else {
			this.erstelltDurch = erstelltDurch;
		}
	}

	public String getAenderungsGrund() {
		return aenderungsGrund;
	}

	public void setAenderungsGrund(String aenderungsGrund) {
		if (aenderungsGrund == null) {
			this.aenderungsGrund = "unknown";
		} else {
			this.aenderungsGrund = aenderungsGrund;
		}
	}

	@Override
	public JSONObject getJson() {
		JSONObject result = new JSONObject();

		JSONObject metaDaten = new JSONObject();
		metaDaten.put("erstelltAm", getErstelltAm());
		metaDaten.put("erstelltDurch", getErstelltDurch());
		metaDaten.put("aenderungsGrund", getAenderungsGrund());
		metaDaten.put("versionsNummer", getVersionsNummer());

		result.put("metaDaten", metaDaten);
		result.put("global", global.getJson());

		JSONArray inkarnationsArray = new JSONArray();
		for (Inkarnation inkarnation : inkarnationen.values()) {
			inkarnationsArray.put(inkarnation.getJson());
		}
		result.put("inkarnationen", inkarnationsArray);

		return result;
	}

	@Override
	public void initFromJson(JSONObject json) {

		JSONObject metaDaten = json.optJSONObject("metaDaten");
		if (metaDaten != null) {
			setErstelltAm(metaDaten.optString("erstelltAm"));
			setErstelltDurch(metaDaten.optString("erstelltDurch"));
			setAenderungsGrund(metaDaten.optString("aenderungsGrund"));
			setVersionsNummer(metaDaten.optString("versionsNummer"));

		}

		global.initFromJson(json.optJSONObject("global"));

		JSONArray inkarnationsArray = json.optJSONArray("inkarnationen");
		if (inkarnationsArray != null) {
			inkarnationsArray.forEach(new Consumer<Object>() {

				@Override
				public void accept(Object element) {
					if (element instanceof JSONObject) {
						Inkarnation inkarnation = new Inkarnation();
						inkarnation.initFromJson((JSONObject) element);
						inkarnationen.put(inkarnation.getInkarnationsName(), inkarnation);
					}
				}
			});
		}
	}
}
