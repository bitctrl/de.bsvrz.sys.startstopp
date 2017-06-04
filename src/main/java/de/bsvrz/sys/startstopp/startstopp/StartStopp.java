package de.bsvrz.sys.startstopp.startstopp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.json.JSONObject;
import org.xml.sax.SAXException;

import de.bsvrz.sys.startstopp.data.StartStoppKonfiguration;

public class StartStopp {

	private final static String TEST_KONFIG = "testkonfigurationen/startStopp01_1.xml";

	public static void main(String[] args) {

		try (InputStream stream = StartStoppKonfiguration.class.getResourceAsStream(TEST_KONFIG)) {;
			StartStoppKonfiguration konfiguration = new StartStoppKonfiguration(stream);
			JSONObject json = konfiguration.getJson();
			
			StartStoppKonfiguration newKonfiguration = new StartStoppKonfiguration(json);
		//	System.err.println(newKonfiguration.getJson().toString(4));
			
			newKonfiguration.saveToXmlFile(new OutputStreamWriter(System.err));
			
		} catch (ParserConfigurationException | SAXException | IOException | XMLStreamException | TransformerFactoryConfigurationError | TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
