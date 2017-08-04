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

import java.util.regex.Pattern;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import de.bsvrz.sys.startstopp.api.jsonschema.MakroDefinition;
import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;

public class RechnerEditor extends StartStoppElementEditor<Rechner> {
	
	private Rechner rechner;
	private StartStoppSkript skript;

	@Inject
	public RechnerEditor(@Assisted StartStoppSkript skript, @Assisted Rechner rechner) {
		super("Rechner: " + rechner.getName());
		this.skript = skript;
		this.rechner = (Rechner) Util.cloneObject(rechner);
	}
	
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
		addresseField.setText(rechner.getTcpAdresse());
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
		portField.setValidationPattern(Pattern.compile("\\d*"));

		mainPanel.addComponent(portField, GridLayout.createHorizontallyFilledLayoutData(1));
	}

	@Override
	public boolean handleInput(KeyStroke key) {
		
		System.err.println("RechnerEditor: " + key);
		if(key.isAltDown()) {
			if( key.getKeyType() == KeyType.Character) {
				switch(key.getCharacter()) {
				case 'm':
				case 'M':
					MakroSelektor selektor = new MakroSelektor(getTextGUI(), skript);
					MakroDefinition selectedMakroDefinition = selektor.getSelectedMakroDefinition();
					if( selectedMakroDefinition != null) {
						Interactable interactable = getFocusedInteractable();
						if( interactable instanceof TextBox) {
							TextBox textBox = (TextBox) interactable;
							int position = textBox.getCursorLocation().getColumn();
							StringBuilder oldText = new StringBuilder(textBox.getText());
							oldText.insert(position, "%" + selectedMakroDefinition.getName() + "%");
							textBox.setText(oldText.toString());
						}
					}
					break;
				}
			}
			return true;
		}
		
		// TODO Auto-generated method stub
		return super.handleInput(key);
	}

	@Override
	public Rechner getElement() {
		return rechner;
	}
}
