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

package de.bsvrz.sys.startstopp.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.bsvrz.sys.startstopp.api.jsonschema.Global;
import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.KernSystem;
import de.bsvrz.sys.startstopp.api.jsonschema.MakroDefinition;
import de.bsvrz.sys.startstopp.api.jsonschema.MetaDaten;
import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.api.jsonschema.StartArt;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StartFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.Usv;
import de.bsvrz.sys.startstopp.api.jsonschema.ZugangDav;
import de.bsvrz.sys.startstopp.config.StartStoppException;

public class StartStoppXMLParser {

	private enum Tags {
		tagIsUndefined,
		konfiguration,
		startStopp,
		global,
		makrodefinition,
		kernsystem,
		zugangdav,
		rechner,
		protokolldatei,
		applikationen,
		inkarnation,
		applikation,
		aufrufparameter,
		startart,
		standardAusgabe,
		standardFehlerAusgabe,
		startFehlerverhalten,
		stoppFehlerverhalten,
		usv,
		startbedingung,
		stoppbedingung;

		static Tags getTag(String tagStr) {
			for (Tags tag : values()) {
				if (tag.name().equalsIgnoreCase(tagStr)) {
					return tag;
				}
			}
			return Tags.tagIsUndefined;
		}
	}

	private Set<String> suppressInkarnationsName = new LinkedHashSet<>();

	private class StartStoppParserHandler extends DefaultHandler {

		private StartStoppSkript destination;
		private Inkarnation currentInkarnation;

		StartStoppParserHandler(StartStoppSkript destination) {
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
				currentInkarnation = new Inkarnation();
				currentInkarnation.setInkarnationsName(attributes.getValue("name"));
				break;
			case kernsystem:
				KernSystem kernsystem = new KernSystem();
				kernsystem.setInkarnationsName(attributes.getValue("inkarnationsname"));
				if (attributes.getValue("mitInkarnationsname") != null) {
					boolean mitInkarnationsName = attributes.getValue("mitInkarnationsname").equals("ja");
					if (!mitInkarnationsName) {
						suppressInkarnationsName.add(kernsystem.getInkarnationsName());
					}
				} else {
					suppressInkarnationsName.add(kernsystem.getInkarnationsName());
				}
				if (destination.getGlobal() == null) {
					destination.setGlobal(new Global());
				}
				destination.getGlobal().getKernsysteme().add(kernsystem);
				break;
			case konfiguration:
				break;
			case makrodefinition:
				MakroDefinition makrodefinition = new MakroDefinition();
				makrodefinition.setName(attributes.getValue("name"));
				makrodefinition.setWert(attributes.getValue("wert"));
				if (destination.getGlobal() == null) {
					destination.setGlobal(new Global());
				}
				destination.getGlobal().getMakrodefinitionen().add(makrodefinition);
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
				if (currentInkarnation.getStartArt() == null) {
					currentInkarnation.setStartArt(new StartArt());
				}

				String startArtOptionValue = attributes.getValue("option");
				if (startArtOptionValue != null) {
					if ("intervall".equals(startArtOptionValue)) {
						currentInkarnation.getStartArt().setOption(StartArt.Option.INTERVALLABSOLUT);
					} else {
						currentInkarnation.getStartArt().setOption(StartArt.Option.fromValue(startArtOptionValue));
					}
				}
				if (attributes.getValue("neustart") != null) {
					currentInkarnation.getStartArt().setNeuStart(attributes.getValue("neustart").equals("ja"));
				}
				if (attributes.getValue("intervall") != null) {
					currentInkarnation.getStartArt().setIntervall(attributes.getValue("intervall"));
				}
				break;
			case startFehlerverhalten:
				if (currentInkarnation.getStartFehlerVerhalten() == null) {
					currentInkarnation.setStartFehlerVerhalten(new StartFehlerVerhalten());
				}
				
				String startFehlerOptionValue = attributes.getValue("option");
				if (startFehlerOptionValue != null) {
					currentInkarnation.getStartFehlerVerhalten()
							.setOption(StartFehlerVerhalten.Option.fromValue(startFehlerOptionValue));
				}
				if (attributes.getValue("wiederholungen") != null) {
					currentInkarnation.getStartFehlerVerhalten()
							.setWiederholungen(attributes.getValue("wiederholungen"));
				}
				break;
			case startStopp:
				if (destination.getMetaDaten() == null) {
					destination.setMetaDaten(new MetaDaten());
				}
				destination.getMetaDaten().setVersionsNummer(attributes.getValue("Versionsnummer"));
				destination.getMetaDaten().setErstelltAm(attributes.getValue("ErstelltAm"));
				destination.getMetaDaten().setErstelltDurch(attributes.getValue("ErstelltDurch"));
				destination.getMetaDaten().setAenderungsGrund(attributes.getValue("Aenderungsgrund"));
				break;
			case stoppFehlerverhalten:
				if (currentInkarnation.getStoppFehlerVerhalten() == null) {
					currentInkarnation.setStoppFehlerVerhalten(new StoppFehlerVerhalten());
				}

				String stoppFehlerOptionValue = attributes.getValue("option");
				if (stoppFehlerOptionValue != null) {
					currentInkarnation.getStoppFehlerVerhalten()
							.setOption(StoppFehlerVerhalten.Option.fromValue(stoppFehlerOptionValue));
				}
				if (attributes.getValue("wiederholungen") != null) {
					currentInkarnation.getStoppFehlerVerhalten()
							.setWiederholungen(attributes.getValue("wiederholungen"));
				}
				break;
			case usv:
				Usv usv = new Usv();
				usv.setPid(attributes.getValue("pid"));
				destination.getGlobal().setUsv(usv);
				break;
			case zugangdav:
				if (destination.getGlobal() == null) {
					destination.setGlobal(new Global());
				}
				if (destination.getGlobal().getZugangDav() == null) {
					destination.getGlobal().setZugangDav(new ZugangDav());
				}
				destination.getGlobal().getZugangDav().setAdresse(attributes.getValue("adresse"));
				destination.getGlobal().getZugangDav().setPort(attributes.getValue("port"));
				destination.getGlobal().getZugangDav().setUserName(attributes.getValue("username"));
				destination.getGlobal().getZugangDav().setPassWord(attributes.getValue("passwort"));
				break;
			case startbedingung:
				StartBedingung startBedingung = new StartBedingung();

				if (attributes.getValue("vorgaenger") != null) {
					String[] vorgaengerStr = attributes.getValue("vorgaenger").split("\\s");
					for (String vorgaenger : vorgaengerStr) {
						if (!vorgaenger.trim().isEmpty()) {
							startBedingung.getVorgaenger().add(vorgaenger.trim());
						}
					}
				}

				if (attributes.getValue("warteart") != null) {
					startBedingung.setWarteart(StartBedingung.Warteart.fromValue(attributes.getValue("warteart")));
				}
				if (attributes.getValue("rechner") != null) {
					startBedingung.setRechner(attributes.getValue("rechner"));
				}
				if (attributes.getValue("wartezeit") != null) {
					startBedingung.setWartezeit(attributes.getValue("wartezeit"));
				}
				currentInkarnation.setStartBedingung(startBedingung);
				break;
			case stoppbedingung:
				StoppBedingung stoppBedingung = new StoppBedingung();

				if (attributes.getValue("nachfolger") != null) {
					String[] nachFolgerStr = attributes.getValue("nachfolger").split("\\s");
					for (String nachfolger : nachFolgerStr) {
						if (!nachfolger.trim().isEmpty()) {
							stoppBedingung.getNachfolger().add(nachfolger.trim());
						}
					}
				}
				if (attributes.getValue("rechner") != null) {
					stoppBedingung.setRechner(attributes.getValue("rechner"));
				}
				if (attributes.getValue("wartezeit") != null) {
					stoppBedingung.setWartezeit(attributes.getValue("wartezeit"));
				}
				currentInkarnation.setStoppBedingung(stoppBedingung);
				break;
			case tagIsUndefined:
				throw new IllegalArgumentException("Tag not supported: " + qName);
			default:
				break;

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
			default:
				break;
			}
		}
	}

	public StartStoppSkript getKonfigurationFromRessource(String resourceName) throws StartStoppException {

		try (InputStream stream = StartStoppXMLParser.class.getResourceAsStream(resourceName)) {
			return parseStream(stream);
		} catch (IOException e) {
			throw new StartStoppException(e);
		}
	}

	public StartStoppSkript getSkriptFromFile(File file) throws StartStoppException {
		return parseFile(file);
	}

	private StartStoppSkript parseFile(File file) throws StartStoppException {

		StartStoppSkript konfiguration = new StartStoppSkript();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			saxParser.parse(file, new StartStoppParserHandler(konfiguration));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new StartStoppException(e);
		}

		suppressInkarnationName(konfiguration);
		fuelleStandardWerte(konfiguration);

		return konfiguration;
	}

	private StartStoppSkript parseStream(InputStream stream) throws StartStoppException {

		StartStoppSkript konfiguration = new StartStoppSkript();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			saxParser.parse(stream, new StartStoppParserHandler(konfiguration));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new StartStoppException(e);
		}

		suppressInkarnationName(konfiguration);
		fuelleStandardWerte(konfiguration);

		return konfiguration;
	}

	private void fuelleStandardWerte(StartStoppSkript konfiguration) {
		for( Inkarnation inkarnation : konfiguration.getInkarnationen()) {
			StartArt startArt = inkarnation.getStartArt();
			if( startArt == null) {
				inkarnation.setStartArt(new StartArt());
			}
		}
	}

	private void suppressInkarnationName(StartStoppSkript konfiguration) {
		for (String item : suppressInkarnationsName) {
			for (Inkarnation inkarnation : konfiguration.getInkarnationen()) {
				if (item.equals(inkarnation.getInkarnationsName())) {
					inkarnation.setMitInkarnationsName(false);
				}
			}
		}
	}

}
