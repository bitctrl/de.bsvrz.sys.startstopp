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

package de.bsvrz.sys.startstopp.console.ui.editor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;

import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.console.StartStoppConsole;
import de.bsvrz.sys.startstopp.console.ui.InfoDialog;
import de.bsvrz.sys.startstopp.console.ui.JaNeinDialog;

class EditorSichernAction implements Runnable {

	private final StartStoppSkript skript;

	EditorSichernAction(StartStoppSkript skript) {
		this.skript = skript;
	}

	@Override
	public void run() {

		FileDialogBuilder fileDialogBuilder = new FileDialogBuilder();
		fileDialogBuilder.setTitle("Zieldatei auswählen");
		fileDialogBuilder.setActionLabel("Sichern");
		File selectedFile = fileDialogBuilder.build().showDialog(StartStoppConsole.getGui());
		if (selectedFile != null) {

			if (selectedFile.exists()) {
				JaNeinDialog dialog = new JaNeinDialog("Sichern",
						"Soll die bestehende Datei überschrieben werden?");
				if (!dialog.display()) {
					return;
				}
			}

			ObjectMapper mapper = new ObjectMapper();
			try (OutputStream stream = new FileOutputStream(selectedFile);
					Writer writer = new OutputStreamWriter(stream, "UTF-8")) {
				mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
				mapper.writeValue(writer, skript);
			} catch (IOException e) {
				new InfoDialog("FEHLER", e.getLocalizedMessage()).display();
			}
		}
	}

	@Override
	public String toString() {
		return "Sichern";
	}
}
