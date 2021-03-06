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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;

import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.console.StartStoppConsole;
import de.bsvrz.sys.startstopp.console.ui.InfoDialog;

class EditorLadenAction implements Runnable {

	private SkriptEditor editor;

	EditorLadenAction(SkriptEditor editor) {
		this.editor = editor;
	}

	@Override
	public void run() {
		FileDialogBuilder fileDialogBuilder = new FileDialogBuilder();
		fileDialogBuilder.setTitle("StartStopp-Konfiguration auswählen");
		fileDialogBuilder.setActionLabel("Laden");
		File selectedFile = fileDialogBuilder.build().showDialog(StartStoppConsole.getGui());
		if ((selectedFile != null) && selectedFile.exists()) {
			try (InputStream stream = new FileInputStream(selectedFile)) {
				ObjectMapper mapper = new ObjectMapper();
				StartStoppSkript skript = mapper.readValue(stream, StartStoppSkript.class);
				editor.updateSkript(skript);
			} catch (IOException e) {
				new InfoDialog("FEHLER", e.getLocalizedMessage()).display();
			}
		}
	}

	@Override
	public String toString() {
		return "Datei laden";
	}
}
