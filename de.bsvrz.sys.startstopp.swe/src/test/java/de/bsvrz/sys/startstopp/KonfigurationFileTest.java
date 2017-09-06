/*
 * Segment 10 System (Sys), SWE 10.1 StartStopp
 * Copyright (C) 2007-2017 BitCtrl Systems GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Contact Information:<br>
 * BitCtrl Systems GmbH<br>
 * Weißenfelser Straße 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.sys.startstopp;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.TransformerFactoryConfigurationError;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkriptStatus;
import de.bsvrz.sys.startstopp.config.StartStoppKonfiguration;
import de.bsvrz.sys.startstopp.util.StartStoppXMLParser;

public class KonfigurationFileTest {

	String[] files = { "startStopp01_1.xml", "startStopp01_2.xml", "startStopp02.xml", "startStopp03.xml",
			"startStopp04.xml", "startStopp05_1.xml", "startStopp05_2.xml", "startStopp05_3.xml", "startStopp06.xml",
			"startStopp07_1.xml", "startStopp07_2.xml", "startStopp07_3.xml", "startStopp07_4.xml",
			"startStopp07_5.xml", "startStopp07_6.xml", "startStopp07_7.xml", "startStopp08_1.xml",
			"startStopp08_2.xml", "startStopp09.xml", "startStopp10.xml" };

	@Test
	public void convertKonfiguration() throws IOException, TransformerFactoryConfigurationError, StartStoppException {
		for (String file : files) {
			StartStoppSkript skript = new StartStoppXMLParser().getSkriptFromFile(new File("../testsystem/startstopp", file));

			StartStoppKonfiguration konfiguration = new StartStoppKonfiguration(skript);
			if (konfiguration.getSkriptStatus().getStatus() != StartStoppSkriptStatus.Status.INITIALIZED) {
				System.err.println("\nKonfigurationsfehler: " + file);
				System.err.println("======================================================\n");
				for (String line : konfiguration.getSkriptStatus().getMessages()) {
					System.err.println(line);
				}
			}

			ObjectMapper objectMapper = new ObjectMapper();

			// configure Object mapper for pretty print
			objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

			// writing to console, can write to any output stream such as file
			StringWriter stringEmp = new StringWriter();
			objectMapper.writeValue(stringEmp, skript);

			// StartStoppKonfiguration konfiguration = new StartStoppKonfiguration(skript);
			// Assert.assertTrue(file + ": " +
			// konfiguration.getSkriptStatus().getMessages().toString(),
			// konfiguration.getSkriptStatus().getMessages().isEmpty());
			// Assert.assertEquals(konfiguration.getSkriptStatus().getStatus(),
			// StartStoppSkriptStatus.Status.INITIALIZED);
		}
	}
}
