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

import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;

import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.util.Util;

class RechnerEditor extends StartStoppElementEditor<Rechner> {

	private Rechner rechner;

	RechnerEditor(StartStoppSkript skript, Rechner rechner) {
		super(skript, "Rechner: " + rechner.getName());
		this.rechner = (Rechner) Util.cloneObject(rechner);
	}

	@Override
	protected void initComponents(Panel mainPanel) {
		mainPanel.setLayoutManager(new GridLayout(1).setLeftMarginSize(1).setRightMarginSize(1));

		mainPanel.addComponent(new Label("Name:"));
		TextBox nameField = new TextBox() {
			@Override
			protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
				rechner.setName(getText());
				super.afterLeaveFocus(direction, nextInFocus);
			}
		};
		nameField.setText(rechner.getName());
		mainPanel.addComponent(nameField, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new Label("Adresse:"));
		TextBox addresseField = new TextBox("") {
			@Override
			protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
				rechner.setTcpAdresse(getText());
				super.afterLeaveFocus(direction, nextInFocus);
			}
		};
		String tcpAdresse = rechner.getTcpAdresse();
		addresseField.setText(tcpAdresse);
		mainPanel.addComponent(addresseField, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new Label("Port:"));
		TextBox portField = new TextBox() {
			@Override
			protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
				rechner.setPort(getText());
				super.afterLeaveFocus(direction, nextInFocus);
			}
		};
		portField.setText(rechner.getPort());
		mainPanel.addComponent(portField, GridLayout.createHorizontallyFilledLayoutData(1));
	}

	@Override
	public Rechner getElement() {
		return rechner;
	}
}
