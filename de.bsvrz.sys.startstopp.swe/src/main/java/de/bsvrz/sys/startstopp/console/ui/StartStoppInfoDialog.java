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

package de.bsvrz.sys.startstopp.console.ui;

import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;

import de.bsvrz.sys.startstopp.api.jsonschema.Util;
import de.bsvrz.sys.startstopp.console.StartStoppConsole;

public class StartStoppInfoDialog {

	private MessageDialog dialog;
	private WindowBasedTextGUI gui = StartStoppConsole.getGui();

	public StartStoppInfoDialog() {
		MessageDialogBuilder msgBuilder = new MessageDialogBuilder();
		msgBuilder.setTitle("StartStopp-Info");
		
		StringBuilder message = new StringBuilder();
		message.append(StartStoppConsole.getVersionInfo());
		message.append("\n\n");
		message.append("Client: ");
		message.append(StartStoppConsole.getClient().getPort());
		message.append(" Port: ");
		message.append(StartStoppConsole.getClient().getPort());
		
		msgBuilder.setText(Util.wrapText(gui.getScreen().getTerminalSize().getColumns(), message.toString()));
		dialog = msgBuilder.build();
	}
	
	public void display() {
		dialog.showDialog(gui);
	}
	
	
}
