package de.bsvrz.sys.startstopp.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.json.JSONObject;
import org.junit.Test;
import org.xml.sax.SAXException;

public class KonfigurationTest {

	String[] files = { "testkonfigurationen/startStopp01_1.xml", "testkonfigurationen/startStopp01_2.xml",
			"testkonfigurationen/startStopp02.xml", "testkonfigurationen/startStopp03.xml",
			"testkonfigurationen/startStopp04.xml", "testkonfigurationen/startStopp05_1.xml",
			"testkonfigurationen/startStopp05_2.xml", "testkonfigurationen/startStopp05_3.xml",
			"testkonfigurationen/startStopp06.xml", "testkonfigurationen/startStopp07_1.xml",
			"testkonfigurationen/startStopp07_2.xml", "testkonfigurationen/startStopp07_3.xml",
			"testkonfigurationen/startStopp07_4.xml", "testkonfigurationen/startStopp07_5.xml",
			"testkonfigurationen/startStopp07_6.xml", "testkonfigurationen/startStopp07_7.xml",
			"testkonfigurationen/startStopp08_1.xml", "testkonfigurationen/startStopp08_2.xml",
			"testkonfigurationen/startStopp09.xml", "testkonfigurationen/startStopp10.xml" };

	@Test
	public void convertKonfiguration() throws ParserConfigurationException, SAXException, IOException,
			XMLStreamException, TransformerFactoryConfigurationError, TransformerException {
		for (String file : files) {
			try (InputStream stream = StartStoppKonfiguration.class.getResourceAsStream(file)) {

				StartStoppKonfiguration konfiguration = new StartStoppKonfiguration(stream);
				JSONObject json = konfiguration.getJson();

				StartStoppKonfiguration newKonfiguration = new StartStoppKonfiguration(json);
				newKonfiguration.saveToXmlFile(new OutputStreamWriter(System.err));
			}
		}
	}
}
