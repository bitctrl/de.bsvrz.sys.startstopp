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
