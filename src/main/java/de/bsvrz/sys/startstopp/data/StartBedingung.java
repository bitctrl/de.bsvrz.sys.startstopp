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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.JSONObject;

public class StartBedingung implements StartStoppConfigurationElement {
	
	public enum WarteArt {
		BEGINN("beginn"),
		ENDE("ende");
		
		private String externalName;

		WarteArt(String externalName) {
			this.externalName = externalName;
		}
		
		public static WarteArt getWarteArt(String externalName) {
			for( WarteArt warteArt : values()) {
				if( warteArt.externalName.equals(externalName)) {
					return warteArt;
				}
			}
			
			throw new IllegalArgumentException("Die Warteart " + externalName + " wird nicht unterstützt");
		}
	}
	private String vorgaenger;
	private WarteArt warteArt = WarteArt.BEGINN;
	private String rechner;
	private String warteZeit;

	public StartBedingung(String vorgaenger) {
		super();
		this.vorgaenger = vorgaenger;
	}

	public StartBedingung(JSONObject json) {
		initFromJson(json);
	}

	public WarteArt getWarteArt() {
		return warteArt;
	}

	public void setWarteArt(WarteArt warteArt) {
		this.warteArt = warteArt;
	}

	public String getRechner() {
		return rechner;
	}

	public void setRechner(String rechner) {
		this.rechner = rechner;
	}

	public String getWarteZeit() {
		return warteZeit;
	}

	public void setWarteZeit(String warteZeit) {
		this.warteZeit = warteZeit;
	}

	public String getVorgaenger() {
		return vorgaenger;
	}

	@Override
	public JSONObject getJson() {

		JSONObject result = new JSONObject();
		
		result.put("vorgaenger", vorgaenger);
		result.put("warteArt", warteArt.externalName);
		result.put("rechner", rechner);
		result.put("warteZeit", warteZeit);
		
		return result;
	}
	
	@Override
	public void initFromJson(JSONObject json) {
		vorgaenger = json.optString("vorgaenger");
		warteArt = WarteArt.getWarteArt(json.optString("warteArt"));
		rechner = json.optString("rechner", null);
		warteZeit = json.optString("warteZeit", null);
	}
	
	@Override
	public void writeXml(XMLStreamWriter destination) throws XMLStreamException {
		destination.writeStartElement("startbedingung");
		destination.writeAttribute("vorgaenger", vorgaenger);
		destination.writeAttribute("warteart", warteArt.externalName);
		if( rechner != null) {
			destination.writeAttribute("rechner", rechner);
		}
		if( warteZeit != null) {
			destination.writeAttribute("wartezeit", warteZeit);
		}
		destination.writeEndElement();
	}
}
