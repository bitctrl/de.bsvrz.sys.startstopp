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

public class StartFehlerVerhalten implements StartStoppConfigurationElement {

	public enum StartFehlerVerhaltenOption {
		OPTION("option"), BEENDEN("beenden"), ABBRUCH("abbruch"), IGNORIEREN("ignorieren");

		private String externalName;

		StartFehlerVerhaltenOption(String externalName) {
			this.externalName = externalName;
		}

		public static StartFehlerVerhaltenOption getStartFehlerVerhaltenOption(String externalName) {
			for (StartFehlerVerhaltenOption option : values()) {
				if (option.externalName.equals(externalName)) {
					return option;
				}
			}

			throw new IllegalArgumentException(
					"Die StartFehlerVerhaltenOption " + externalName + " wird nicht unterstützt");
		}
	};

	private StartFehlerVerhaltenOption option = StartFehlerVerhaltenOption.IGNORIEREN;
	private int wiederholungen = 0;

	public StartFehlerVerhaltenOption getOption() {
		return option;
	}

	public void setOption(StartFehlerVerhaltenOption option) {
		this.option = option;
	}

	public int getWiederholungen() {
		return wiederholungen;
	}

	public void setWiederholungen(int wiederholungen) {
		this.wiederholungen = wiederholungen;
	}

	@Override
	public JSONObject getJson() {

		JSONObject result = new JSONObject();

		result.put("option", option.externalName);
		result.put("wiederholungen", wiederholungen);

		return result;
	}

	@Override
	public void initFromJson(JSONObject json) {
		wiederholungen = json.optInt("wiederholungen");
		option = StartFehlerVerhaltenOption.getStartFehlerVerhaltenOption(json.optString("option"));
	}

	@Override
	public void writeXml(XMLStreamWriter destination) throws XMLStreamException {
	
		destination.writeStartElement("startFehlerverhalten");
		destination.writeAttribute("option", option.externalName);
		if( wiederholungen > 0) {
			destination.writeAttribute("wiederholungen", Integer.toString(wiederholungen));
		}
	
		destination.writeEndElement();
	}

}
