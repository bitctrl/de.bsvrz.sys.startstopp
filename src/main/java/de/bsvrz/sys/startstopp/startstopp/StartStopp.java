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

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.startstopp.api.ApiServer;
import de.bsvrz.sys.startstopp.config.ConfigurationManager;
import de.bsvrz.sys.startstopp.data.StartStoppKonfiguration;
import de.bsvrz.sys.startstopp.process.ProcessManager;

public class StartStopp  {

	public static void main(String[] args) {
		StartStoppOptions options = new StartStoppOptions(args);
		ConfigurationManager configurationManager = new ConfigurationManager(options);
		
		ProcessManager processManager = new ProcessManager(options, configurationManager);
		processManager.start();
		
		ApiServer apiServer = new ApiServer(options, processManager);
		apiServer.start();
	}
	
}
