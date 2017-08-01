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

import java.util.Collections;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Interactable.FocusChangeDirection;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.input.KeyStroke;

public class UrlasserDialog extends DialogWindow {

	private MessageDialogButton result = MessageDialogButton.Cancel;
	private TextBox grundField;
	private TextBox passwdField;
	private TextBox nameField;
	private Button okButton;
	private Button cancelButton;

	public UrlasserDialog(String title) {
		super(title);

		setHints(Collections.singleton(Hint.CENTERED));
		setCloseWindowWithEscape(true);
		
		Panel buttonPanel = new Panel();
		buttonPanel.setLayoutManager(new GridLayout(2).setHorizontalSpacing(1));
		okButton = new Button("OK", new Runnable() {
			@Override
			public void run() {
				result = MessageDialogButton.OK;
				close();
			}
		});
		buttonPanel.addComponent(okButton);
		cancelButton = new Button("Abbrechen", new Runnable() {
			@Override
			public void run() {
				result = MessageDialogButton.Cancel;
				close();
			}
		});
		buttonPanel.addComponent(cancelButton);

		Panel mainPanel = new Panel();
		mainPanel.setLayoutManager(new GridLayout(1).setLeftMarginSize(1).setRightMarginSize(1));

		mainPanel.addComponent(new Label("Name:"));
		nameField = new TextBox();
		mainPanel.addComponent(nameField, GridLayout.createHorizontallyFilledLayoutData(1));
		
		mainPanel.addComponent(new Label("Passwort:"));
		passwdField = new TextBox("");
		passwdField.setMask('*');
		mainPanel.addComponent(passwdField, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new Label("Grund:"));
		grundField = new TextBox();
		mainPanel.addComponent(grundField, GridLayout.createHorizontallyFilledLayoutData(1));
		mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));
		
		buttonPanel.setLayoutData(
				GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER, false, false))
				.addTo(mainPanel);

		setComponent(mainPanel);
	}

	@Override
	public MessageDialogButton showDialog(WindowBasedTextGUI textGUI) {
		result = MessageDialogButton.Cancel;
		super.showDialog(textGUI);
		return result;
	}

	public String getGrund() {
		return grundField.getText();
	}

	public String getVeranlasser() {
		return nameField.getText();
	}

	public String getPasswort() {
		return passwdField.getText();
	}
	
	@Override
	public boolean handleInput(KeyStroke key) {
		System.err.println("Urlasserdialog: " + key);
		if( key.isAltDown()) {
			switch(key.getCharacter()) {
			case 'a':
			case 'A':
				setFocusedInteractable(cancelButton, FocusChangeDirection.NEXT);
				break;
			case 'o':
			case 'O':
				setFocusedInteractable(okButton, FocusChangeDirection.NEXT);
				break;
			}
			return true;
		}
		return super.handleInput(key);
	}
}
