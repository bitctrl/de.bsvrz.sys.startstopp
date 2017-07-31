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

import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;

import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.console.StartStoppConsole;
import de.bsvrz.sys.startstopp.console.ui.UrlasserDialog;

public class EditorSaveAction implements Runnable {

	private StartStoppSkript skript;
	private WindowBasedTextGUI textGui;

	public EditorSaveAction(WindowBasedTextGUI textGui, StartStoppSkript skript) {
		this.skript = skript;
		this.textGui = textGui;
	}

	@Override
	public void run() {
		UrlasserDialog dialog = new UrlasserDialog("Versionieren");
		Object showDialog = dialog.showDialog(textGui);
		if (showDialog.equals(MessageDialogButton.OK)) {
			try {
				StartStoppConsole.getInstance().getClient().setCurrentSkript(dialog.getVeranlasser(), dialog.getPasswort(), null, dialog.getGrund(), skript);
			} catch (StartStoppException e) {

				StringBuilder text = new StringBuilder(e.getLocalizedMessage());
				for( String msg : e.getMessages()) {
					text.append('\n');
					text.append(msg);
				}
				
				MessageDialogBuilder builder = new MessageDialogBuilder();
				builder.setTitle("Fehler");
				builder.setText(text.toString());
				builder.build().showDialog(textGui);
			}
		}
	}

	@Override
	public String toString() {
		return "Sichern";
	}
}
