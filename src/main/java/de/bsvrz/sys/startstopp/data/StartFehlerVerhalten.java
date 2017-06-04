package de.bsvrz.sys.startstopp.data;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.JSONObject;

public class StartFehlerVerhalten implements StartStoppConfigurationElement {

	public enum StartFehlerVerhaltenOption {
		OPTION("option"), BEENDEN("beenden"), ABBRUCH("abbruch"), IGNORIEREN("ignorieren");

		private String externalName;

		private StartFehlerVerhaltenOption(String externalName) {
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
