package de.bsvrz.sys.startstopp.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.JSONArray;
import org.json.JSONObject;

public class GlobalData implements StartStoppConfigurationElement {
	
	private final Map<String, String> makrodefinitionen = new LinkedHashMap<>();
	private final List<KernSystem> kernsysteme = new ArrayList<>(); 
	private ZugangDav zugangDav = new ZugangDav();
	private String usv;
	private final Map<String, String> rechner = new LinkedHashMap<>();
	
	public void addKernSystem(KernSystem kernsystem) {
		kernsysteme.add(kernsystem);
	}

	public void addMakroDefinition(String name, String wert) {
		makrodefinitionen.put(name, wert);
	}

	public void addRechner(String name, String adresse) {
		rechner.put(name, adresse);
	}
	
	public ZugangDav getZugangDav() {
		return zugangDav;
	}

	public String getUsv() {
		return usv;
	}

	public void setUsv(String usv) {
		this.usv = usv;
	}

	@Override
	public JSONObject getJson() {

		JSONObject result = new JSONObject();
		
		JSONObject makros = new JSONObject();
		for( Entry<String, String> makro : makrodefinitionen.entrySet()) {
			makros.put(makro.getKey(), makro.getValue());
		}
		result.put("makrodefinitionen", makros);

		JSONArray kernsystems = new JSONArray();
		for( KernSystem kernsystem : kernsysteme) {
			kernsystems.put(kernsystem.getJson());
		}
		result.put("kernsysteme", kernsystems);

		result.put("zugangDav", zugangDav.getJson());
		result.put("usv", usv);
		
		JSONObject rechners = new JSONObject();
		for( Entry<String, String> item : rechner.entrySet()) {
			rechners.put(item.getKey(), item.getValue());
		}
		result.put("rechner", rechners);

		return result;
	}

	@Override
	public void initFromJson(JSONObject json) {
		
		JSONObject makros = json.optJSONObject("makrodefinitionen");
		if( makros != null) {
			for( String key : makros.keySet()) {
				makrodefinitionen.put(key, makros.getString(key));
			}
		}
	
		JSONArray kernsystems = json.optJSONArray("kernsysteme");
		for( int idx = 0; idx < kernsystems.length(); idx++) {
			KernSystem kernsystem = new KernSystem(kernsystems.optJSONObject(idx));
			kernsysteme.add(kernsystem);
		}

		zugangDav.initFromJson(json.optJSONObject("zugangDav"));
		usv = json.optString("usv", null);
		
		JSONObject rechners = json.optJSONObject("rechner");
		if( rechners != null) {
			for( String key : rechners.keySet()) {
				rechner.put(key, rechners.getString(key));
			}
		}
	}

	@Override
	public void writeXml(XMLStreamWriter destination) throws XMLStreamException {
		
		destination.writeStartElement("global");
		
		for(Entry<String,String> makroDefinition : makrodefinitionen.entrySet()) {
			destination.writeStartElement("makrodefinition");
			destination.writeAttribute("name", makroDefinition.getKey());
			destination.writeAttribute("wert", makroDefinition.getValue());
			destination.writeEndElement();
		}
		
		for(KernSystem kernSystem : kernsysteme) {
			kernSystem.writeXml(destination);
		}

		zugangDav.writeXml(destination);
		
		if( usv != null) {
			destination.writeStartElement("usv");
			destination.writeAttribute("pid", usv);
			destination.writeEndElement();
		}
		
		for(Entry<String,String> rechnerItem : rechner.entrySet()) {
			destination.writeStartElement("rechner");
			destination.writeAttribute("name", rechnerItem.getKey());
			destination.writeAttribute("wert", rechnerItem.getValue());
			destination.writeEndElement();
		}

		destination.writeEndElement();
	}
}
