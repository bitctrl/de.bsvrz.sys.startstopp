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
