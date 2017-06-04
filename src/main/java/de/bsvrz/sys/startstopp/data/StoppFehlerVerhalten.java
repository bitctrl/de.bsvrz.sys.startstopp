package de.bsvrz.sys.startstopp.data;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.JSONObject;

public class StoppFehlerVerhalten implements StartStoppConfigurationElement {

	public enum StoppFehlerVerhaltenOption {
		STOPP("stopp"), ABBRUCH("abbruch"), IGNORIEREN("ignorieren");

		private String externalName;

		private StoppFehlerVerhaltenOption(String externalName) {
			this.externalName = externalName;
		}

		public static StoppFehlerVerhaltenOption getStoppFehlerVerhaltenOption(String externalName) {
			for (StoppFehlerVerhaltenOption option : values()) {
				if (option.externalName.equals(externalName)) {
					return option;
				}
			}

			throw new IllegalArgumentException(
					"Die StartFehlerVerhaltenOption " + externalName + " wird nicht unterstützt");
		}
	};

	private StoppFehlerVerhaltenOption option = StoppFehlerVerhaltenOption.IGNORIEREN;
	private int wiederholungen = 0;

	public StoppFehlerVerhaltenOption getOption() {
		return option;
	}

	public void setOption(StoppFehlerVerhaltenOption option) {
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
		option = StoppFehlerVerhaltenOption.getStoppFehlerVerhaltenOption(json.optString("option"));
	}
	
	@Override
	public void writeXml(XMLStreamWriter destination) throws XMLStreamException {
		destination.writeStartElement("stoppFehlerverhalten");
		destination.writeAttribute("option", option.externalName);
		if( wiederholungen > 0) {
			destination.writeAttribute("wiederholungen", Integer.toString(wiederholungen));
		}
	
		destination.writeEndElement();
	}
}
