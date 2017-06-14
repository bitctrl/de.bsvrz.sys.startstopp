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

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.JSONArray;
import org.json.JSONObject;

public class Inkarnation implements StartStoppConfigurationElement {
	private String inkarnationsName;

	private String applikation;
	private List<String> aufrufParameter = new ArrayList<>();
	private final StartArt startArt = new StartArt();
	private List<StartBedingung> startBedingungen = new ArrayList<>();
	private List<StoppBedingung> stoppBedingungen = new ArrayList<>();
	private StartFehlerVerhalten startFehlerVerhalten = new StartFehlerVerhalten();
	private StoppFehlerVerhalten stoppFehlerVerhalten = new StoppFehlerVerhalten();

	public Inkarnation() {
	}

	public Inkarnation(String inkarnationsName) {
		this.inkarnationsName = inkarnationsName;
	}
	
	public String getInkarnationsName() {
		return inkarnationsName;
	}

	public String getApplikation() {
		return applikation;
	}

	public void setApplikation(String applikation) {
		this.applikation = applikation;
	}

	public void addAufrufParameter(String value) {
		aufrufParameter.add(value);
	}

	public StartArt getStartArt() {
		return startArt;
	}

	public void addStoppBedingung( StoppBedingung bedingung) {
		stoppBedingungen.add(bedingung);
	}
	
	public List<StoppBedingung> getStoppBedingungen() {
		return stoppBedingungen;
	}

	public StartFehlerVerhalten getStartFehlerVerhalten() {
		return startFehlerVerhalten;
	}

	public StoppFehlerVerhalten getStoppFehlerVerhalten() {
		return stoppFehlerVerhalten;
	}

	public void addStartBedingung( StartBedingung bedingung) {
		startBedingungen.add(bedingung);
	}
	
	public List<StartBedingung> getStartBedingungen() {
		return startBedingungen;
	}

	@Override
	public JSONObject getJson() {
		
		JSONObject result = new JSONObject();

		result.put("inkarnationsName", inkarnationsName);
		result.put("applikation", applikation);
		result.put("aufrufParameter", new JSONArray(aufrufParameter));
		result.put("startArt", startArt.getJson());
		
		JSONArray jsonArray = new JSONArray();
		for( StartBedingung bedingung : startBedingungen) {
			jsonArray.put(bedingung.getJson());
		}
		result.put("startBedingungen", jsonArray);

		jsonArray = new JSONArray();
		for( StoppBedingung bedingung : stoppBedingungen) {
			jsonArray.put(bedingung.getJson());
		}
		result.put("stoppBedingungen", jsonArray);

		result.put("startFehlerVerhalten", startFehlerVerhalten.getJson());
		result.put("stoppFehlerVerhalten", stoppFehlerVerhalten.getJson());

		return result;
	}

	@Override
	public void initFromJson(JSONObject json) {
		
		inkarnationsName = json.optString("inkarnationsName");
		applikation = json.optString("applikation");
		
		JSONArray jsonArray = json.optJSONArray("aufrufParameter");
		if( jsonArray != null) {
			for( int idx = 0; idx < jsonArray.length(); idx++) {
				aufrufParameter.add(jsonArray.getString(idx));
			}
		}

		startArt.initFromJson(json.optJSONObject("startArt"));
		
		jsonArray = json.optJSONArray("startBedingungen");
		if( jsonArray != null) {
			for( int idx = 0; idx < jsonArray.length(); idx++) {
				StartBedingung bedingung = new StartBedingung(jsonArray.getJSONObject(idx));
				startBedingungen.add(bedingung);
			}
		}

		jsonArray = json.optJSONArray("stoppBedingungen");
		if( jsonArray != null) {
			for( int idx = 0; idx < jsonArray.length(); idx++) {
				StoppBedingung bedingung = new StoppBedingung(jsonArray.getJSONObject(idx));
				stoppBedingungen.add(bedingung);
			}
		}
		
		startFehlerVerhalten.initFromJson(json.optJSONObject("startFehlerVerhalten"));
		stoppFehlerVerhalten.initFromJson(json.optJSONObject("stoppFehlerVerhalten"));
	}

	@Override
	public void writeXml(XMLStreamWriter destination) throws XMLStreamException {
		// TODO Ausgabe als XML
//		<!ELEMENT inkarnation (applikation, aufrufparameter*, startart?,
//				   startbedingung*, stoppbedingung*, standardAusgabe?, standardFehlerAusgabe?,
//				   startFehlerverhalten?, stoppFehlerverhalten?)>

		destination.writeStartElement("inkarnation");
		destination.writeAttribute("name", inkarnationsName);

		destination.writeStartElement("applikation");
		destination.writeAttribute("name", applikation);
		destination.writeEndElement();

		for( String parameter : aufrufParameter) {
			destination.writeStartElement("aufrufparameter");
			destination.writeAttribute("wert", parameter);
			destination.writeEndElement();
		}
		
		startArt.writeXml(destination);
		
		for( StartBedingung bedingung : startBedingungen) {
			bedingung.writeXml(destination);
		}

		for( StoppBedingung bedingung : stoppBedingungen) {
			bedingung.writeXml(destination);
		}
		
		startFehlerVerhalten.writeXml(destination);
		stoppFehlerVerhalten.writeXml(destination);
		
		destination.writeEndElement();
		
		
	}
}