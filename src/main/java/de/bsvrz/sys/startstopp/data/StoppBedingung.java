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

public class StoppBedingung implements StartStoppConfigurationElement {
	private String nachfolger;
	private String rechner;
	private String warteZeit;

	public StoppBedingung(String nachfolger) {
		super();
		this.nachfolger = nachfolger;
	}

	public StoppBedingung(JSONObject json) {
		initFromJson(json);
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

	public String getNachfolger() {
		return nachfolger;
	}

	@Override
	public JSONObject getJson() {

		JSONObject result = new JSONObject();

		result.put("nachfolger", nachfolger);
		result.put("rechner", rechner);
		result.put("warteZeit", warteZeit);

		return result;
	}

	@Override
	public void initFromJson(JSONObject json) {
		nachfolger = json.optString("nachfolger");
		rechner = json.optString("rechner");
		warteZeit = json.optString("warteZeit", null);
	}
	
	@Override
	public void writeXml(XMLStreamWriter destination) throws XMLStreamException {

		destination.writeStartElement("stoppbedingung");
		destination.writeAttribute("nachfolger", nachfolger);
		if( rechner != null) {
			destination.writeAttribute("rechner", rechner);
		}
		if( warteZeit != null) {
			destination.writeAttribute("wartezeit", warteZeit);
		}
		destination.writeEndElement();
		
	}


}
