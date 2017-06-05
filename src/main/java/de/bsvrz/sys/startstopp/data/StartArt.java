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

public class StartArt implements StartStoppConfigurationElement {

	public enum StartArtOption {
		AUTOMATISCH("automatisch"), MANUELL("manuell"), INTERVALL_REL("intervallrelativ"), INTERVALL_ABS(
				"intervallabsolut");

		private String externalName;

		StartArtOption(String externalName) {
			this.externalName = externalName;
		}

		public static StartArtOption getStartArtOption(String externalName) {
			for (StartArtOption option : values()) {
				if (option.externalName.equals(externalName)) {
					return option;
				}
			}

			throw new IllegalArgumentException("StartArtOption: " + externalName + " wird nicht unterstützt");
		}
	}

	private StartArtOption option = StartArtOption.AUTOMATISCH;
	private boolean neuStart = true;
	private String intervall;

	public StartArtOption getOption() {
		return option;
	}

	public void setOption(StartArtOption option) {
		this.option = option;
	}

	public boolean isNeuStart() {
		return neuStart;
	}

	public void setNeuStart(boolean neuStart) {
		this.neuStart = neuStart;
	}

	public String getIntervall() {
		return intervall;
	}

	public void setIntervall(String intervall) {
		this.intervall = intervall;
	}

	@Override
	public JSONObject getJson() {

		JSONObject result = new JSONObject();

		result.put("option", option.externalName);
		result.put("neuStart", neuStart);
		if (intervall != null) {
			result.put("intervall", intervall);
		}
		return result;
	}

	@Override
	public void initFromJson(JSONObject json) {
		intervall = json.optString("intervall", null);
		neuStart = json.getBoolean("neuStart");
		option = StartArtOption.getStartArtOption(json.getString("option"));
	}

	@Override
	public void writeXml(XMLStreamWriter destination) throws XMLStreamException {
		destination.writeStartElement("startart");
		destination.writeAttribute("option", option.externalName);
		destination.writeAttribute("neustart", neuStart ? "ja" : "nein");
		if (intervall != null) {
			destination.writeAttribute("intervall", intervall);
		}
		destination.writeEndElement();
	}
}
