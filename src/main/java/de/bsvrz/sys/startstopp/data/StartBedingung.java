package de.bsvrz.sys.startstopp.data;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.JSONObject;

public class StartBedingung implements StartStoppConfigurationElement {
	
	public static enum WarteArt {
		BEGINN("beginn"),
		ENDE("ende");
		
		private String externalName;

		private WarteArt(String externalName) {
			this.externalName = externalName;
		}
		
		public static WarteArt getWarteArt(String externalName) {
			for( WarteArt warteArt : values()) {
				if( warteArt.externalName.equals(externalName)) {
					return warteArt;
				}
			}
			
			throw new IllegalArgumentException("Die Startbedingunswarteart " + externalName + " wird nicht unterstützt");
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
