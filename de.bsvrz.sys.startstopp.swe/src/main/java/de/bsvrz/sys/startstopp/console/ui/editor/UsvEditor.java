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

import java.util.Arrays;

import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;

import de.bsvrz.sys.startstopp.api.jsonschema.Usv;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;

public class UsvEditor extends DialogWindow {

	private Usv usv;
	private boolean okPressed;

	public UsvEditor(Usv usv) {
		super("StartStopp - Editor: Inkarnation: ");

		if( usv == null) {
			this.usv = new Usv("");
		} else {
			this.usv = (Usv) Util.cloneObject(usv);
		}
		
		
		setHints(Arrays.asList(Window.Hint.CENTERED));
		setCloseWindowWithEscape(true);
		
		initUI();
	}
	
	private void initUI() {
		Panel buttonPanel = new Panel();
		buttonPanel.setLayoutManager(new GridLayout(2).setHorizontalSpacing(1));
		Button okButton = new Button("OK", new Runnable() {


			@Override
			public void run() {
				okPressed = true;
				close();
			}
		});
		buttonPanel.addComponent(okButton);
		Button cancelButton = new Button("Abbrechen", new Runnable() {

			@Override
			public void run() {
				close();
			}
		});
		buttonPanel.addComponent(cancelButton);

		Panel mainPanel = new Panel();
		mainPanel.setLayoutManager(new GridLayout(1).setLeftMarginSize(1).setRightMarginSize(1));

		// TODO Inhalt für USV

		mainPanel.addComponent(buttonPanel);
		setComponent(mainPanel);
	}
	

	@Override
	public Boolean showDialog(WindowBasedTextGUI textGUI) {
		// TODO Auto-generated method stub
		super.showDialog(textGUI);
		return okPressed;
	}

	public Usv getUsv() {
		// TODO Auto-generated method stub
		return usv;
	}
	
}
