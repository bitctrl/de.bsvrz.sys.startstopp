package de.bsvrz.sys.startstopp.data;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.JSONObject;

public interface StartStoppConfigurationElement {
	
	JSONObject getJson();
	void initFromJson(JSONObject json);
	
	void writeXml(XMLStreamWriter destination) throws XMLStreamException;
}
