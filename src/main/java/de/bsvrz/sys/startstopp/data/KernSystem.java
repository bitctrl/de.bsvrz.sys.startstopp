package de.bsvrz.sys.startstopp.data;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.JSONObject;

public class KernSystem implements StartStoppConfigurationElement {
	private  String inkarnationsName;
	private String warteZeit;
	private boolean mitInkarnationsName;
	
	public KernSystem(String inkarnationsName) {
		this.inkarnationsName = inkarnationsName;
	}
	
	public KernSystem(JSONObject json) {
		initFromJson(json);
	}

	public String getWarteZeit() {
		return warteZeit;
	}
	public void setWarteZeit(String warteZeit) {
		this.warteZeit = warteZeit;
	}
	public boolean isMitInkarnationsName() {
		return mitInkarnationsName;
	}
	public void setMitInkarnationsName(boolean mitInkarnationsName) {
		this.mitInkarnationsName = mitInkarnationsName;
	}
	public String getInkarnationsName() {
		return inkarnationsName;
	}

	@Override
	public JSONObject getJson() {
		JSONObject result = new JSONObject();
		result.put("inkarnationsName", inkarnationsName);
		result.put("warteZeit", warteZeit);
		result.put("mitInkarnationsName", mitInkarnationsName);
		return result;
	}

	@Override
	public void initFromJson(JSONObject json) {
		inkarnationsName = json.optString("inkarnationsName");
		warteZeit = json.optString("warteZeit", null);
		mitInkarnationsName = json.optBoolean("mitInkarnationsName");
	}

	@Override
	public void writeXml(XMLStreamWriter destination) throws XMLStreamException {
		destination.writeStartElement("kernsystem");
		destination.writeAttribute("inkarnationsname", inkarnationsName);
		if( warteZeit != null) {
			destination.writeAttribute("warteZeit", warteZeit);
		}
		destination.writeAttribute("mitInkarnationsname", mitInkarnationsName ? "ja" : "nein");
		destination.writeEndElement();
	}

}
