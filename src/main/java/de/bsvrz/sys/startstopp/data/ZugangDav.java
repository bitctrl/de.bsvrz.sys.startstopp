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

public class ZugangDav implements StartStoppConfigurationElement {
	private String adresse;
	private String port;
	private String userName;
	private String passWord;

	public String getAdresse() {
		return adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	@Override
	public JSONObject getJson() {
		JSONObject result = new JSONObject();
		result.put("adresse", adresse);
		result.put("port", port);
		result.put("userName", userName);
		result.put("passWord", passWord);
		return result;
	}

	@Override
	public void initFromJson(JSONObject json) {
		adresse = json.optString("adresse");
		port = json.optString("port");
		userName = json.optString("userName");
		passWord = json.optString("passWord");
	}

	@Override
	public void writeXml(XMLStreamWriter destination) throws XMLStreamException {

		destination.writeStartElement("zugangdav");
		destination.writeAttribute("adresse", adresse);
		destination.writeAttribute("port", port);
		destination.writeAttribute("username", userName);
		destination.writeAttribute("passwort", passWord);
		destination.writeEndElement();
	}
}
