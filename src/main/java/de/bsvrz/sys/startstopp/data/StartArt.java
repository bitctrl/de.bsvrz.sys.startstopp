package de.bsvrz.sys.startstopp.data;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.JSONObject;

public class StartArt implements StartStoppConfigurationElement {

	public enum StartArtOption {
		AUTOMATISCH("automatisch"), MANUELL("manuell"), INTERVALL_REL("intervallrelativ"), INTERVALL_ABS(
				"intervallabsolut");

		private String externalName;

		private StartArtOption(String externalName) {
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
