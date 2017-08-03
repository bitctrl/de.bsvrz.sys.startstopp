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

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;

import de.bsvrz.sys.startstopp.api.jsonschema.Util;

public class JaNeinDialog {

	private MessageDialog dialog;
	private WindowBasedTextGUI gui;

	@Inject
	public JaNeinDialog(WindowBasedTextGUI gui, @Assisted("title") String title, @Assisted("message") String message) {

		this.gui = gui;

		MessageDialogBuilder msgBuilder = new MessageDialogBuilder();
		msgBuilder.setTitle(title);
		msgBuilder.setText(Util.wrapText(gui.getScreen().getTerminalSize().getColumns(), message));
		msgBuilder.addButton(MessageDialogButton.Yes);
		msgBuilder.addButton(MessageDialogButton.No);
		dialog = msgBuilder.build();
	}

	public boolean display() {
		return dialog.showDialog(gui) == MessageDialogButton.Yes;
	}
}
