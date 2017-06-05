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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.JSONArray;
import org.json.JSONObject;

public class GlobalData implements StartStoppConfigurationElement {
	
	private final Map<String, String> makrodefinitionen = new LinkedHashMap<>();
	private final List<KernSystem> kernsysteme = new ArrayList<>(); 
	private ZugangDav zugangDav = new ZugangDav();
	private String usv;
	private final Map<String, String> rechner = new LinkedHashMap<>();
	
	public void addKernSystem(KernSystem kernsystem) {
		kernsysteme.add(kernsystem);
	}

	public void addMakroDefinition(String name, String wert) {
		makrodefinitionen.put(name, wert);
	}

	public void addRechner(String name, String adresse) {
		rechner.put(name, adresse);
	}
	
	public ZugangDav getZugangDav() {
		return zugangDav;
	}

	public String getUsv() {
		return usv;
	}

	public void setUsv(String usv) {
		this.usv = usv;
	}

	@Override
	public JSONObject getJson() {

		JSONObject result = new JSONObject();
		
		JSONObject makros = new JSONObject();
		for( Entry<String, String> makro : makrodefinitionen.entrySet()) {
			makros.put(makro.getKey(), makro.getValue());
		}
		result.put("makrodefinitionen", makros);

		JSONArray kernsystems = new JSONArray();
		for( KernSystem kernsystem : kernsysteme) {
			kernsystems.put(kernsystem.getJson());
		}
		result.put("kernsysteme", kernsystems);

		result.put("zugangDav", zugangDav.getJson());
		result.put("usv", usv);
		
		JSONObject rechners = new JSONObject();
		for( Entry<String, String> item : rechner.entrySet()) {
			rechners.put(item.getKey(), item.getValue());
		}
		result.put("rechner", rechners);

		return result;
	}

	@Override
	public void initFromJson(JSONObject json) {
		
		JSONObject makros = json.optJSONObject("makrodefinitionen");
		if( makros != null) {
			for( String key : makros.keySet()) {
				makrodefinitionen.put(key, makros.getString(key));
			}
		}
	
		JSONArray kernsystems = json.optJSONArray("kernsysteme");
		for( int idx = 0; idx < kernsystems.length(); idx++) {
			KernSystem kernsystem = new KernSystem(kernsystems.optJSONObject(idx));
			kernsysteme.add(kernsystem);
		}

		zugangDav.initFromJson(json.optJSONObject("zugangDav"));
		usv = json.optString("usv", null);
		
		JSONObject rechners = json.optJSONObject("rechner");
		if( rechners != null) {
			for( String key : rechners.keySet()) {
				rechner.put(key, rechners.getString(key));
			}
		}
	}

	@Override
	public void writeXml(XMLStreamWriter destination) throws XMLStreamException {
		
		destination.writeStartElement("global");
		
		for(Entry<String,String> makroDefinition : makrodefinitionen.entrySet()) {
			destination.writeStartElement("makrodefinition");
			destination.writeAttribute("name", makroDefinition.getKey());
			destination.writeAttribute("wert", makroDefinition.getValue());
			destination.writeEndElement();
		}
		
		for(KernSystem kernSystem : kernsysteme) {
			kernSystem.writeXml(destination);
		}

		zugangDav.writeXml(destination);
		
		if( usv != null) {
			destination.writeStartElement("usv");
			destination.writeAttribute("pid", usv);
			destination.writeEndElement();
		}
		
		for(Entry<String,String> rechnerItem : rechner.entrySet()) {
			destination.writeStartElement("rechner");
			destination.writeAttribute("name", rechnerItem.getKey());
			destination.writeAttribute("wert", rechnerItem.getValue());
			destination.writeEndElement();
		}

		destination.writeEndElement();
	}
}
