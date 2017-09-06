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

package de.bsvrz.sys.startstopp.console;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkriptStatus;
import de.bsvrz.sys.startstopp.config.StartStoppKonfiguration;
import de.bsvrz.sys.startstopp.util.StartStoppXMLParser;

public class StartStoppKonverter {

	private File inputFile;
	private File outputFile;

	public StartStoppKonverter(File inputFile, File outputFile) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}

	private void convert() throws StartStoppException {
		StartStoppXMLParser parser = new StartStoppXMLParser();
		StartStoppSkript skript = parser.getSkriptFromFile(inputFile);

		StartStoppKonfiguration konfiguration = new StartStoppKonfiguration(skript);
		if (konfiguration.getSkriptStatus().getStatus() != StartStoppSkriptStatus.Status.INITIALIZED) {
			System.out.println("Konfigurationsfehler");
			System.out.println("====================\n");
			for (String line : konfiguration.getSkriptStatus().getMessages()) {
				System.out.println(line);
			}
		}

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

		try (FileOutputStream fileStream = new FileOutputStream(outputFile);
				Writer writer = new OutputStreamWriter(fileStream, "UTF-8")) {
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			mapper.writeValue(writer, skript);
		} catch (IOException e) {
			throw new StartStoppException(e);
		}
	}

	public static void main(String[] args) throws StartStoppException {

		ArgumentList argumentList = new ArgumentList(args);
		String input = argumentList.fetchArgument("-input=").asNonEmptyString();
		String output = argumentList.fetchArgument("-output=").asNonEmptyString();

		if (input.trim().isEmpty()) {
			System.out.println("Es wurde keine XML-Quelle angegeben (Argument: -input=<file>)!");
			System.exit(-1);
		}

		if (output.trim().isEmpty()) {
			System.out.println("Es wurde keine Zieldatei angegeben (Argument: -output=<file>)!");
			System.exit(-1);
		}

		File inputFile = new File(input);
		if (inputFile.isDirectory() || !inputFile.exists() || !inputFile.canRead()) {
			System.out.println("Die Quelldatei existiert nicht oder ist nicht lesbar!");
			System.exit(-1);
		}

		File outputFile = new File(output);
		if (outputFile.exists() && !argumentList.hasArgument("-force")) {
			System.out.println("Die Zieldatei existiert bereits, zum Überschreiben muss \"-force\" übergeben werden!");
			System.exit(-1);
		}

		StartStoppKonverter startStoppKonverter = new StartStoppKonverter(inputFile, outputFile);
		startStoppKonverter.convert();
	}
}
