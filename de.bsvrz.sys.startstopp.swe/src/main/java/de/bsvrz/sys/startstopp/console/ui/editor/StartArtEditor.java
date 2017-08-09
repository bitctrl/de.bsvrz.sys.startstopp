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

import com.googlecode.lanterna.gui2.CheckBox;
import com.googlecode.lanterna.gui2.ComboBox;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;

import de.bsvrz.sys.startstopp.api.jsonschema.StartArt;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;

class StartArtEditor extends StartStoppElementEditor<StartArt> {

	private StartArt startArt;
	
	StartArtEditor(StartStoppSkript skript, StartArt startArt) {
		super(skript, "Startart-Editor");
		this.startArt = (StartArt) Util.cloneObject(startArt);
	}

	protected void initComponents(Panel mainPanel) {

		mainPanel.addComponent(new Label("Option:"));
		ComboBox<StartArt.Option> optionSelektor = new ComboBox<>(StartArt.Option.values());
		for( int idx = 0; idx < optionSelektor.getItemCount(); idx++) {
			if( optionSelektor.getItem(idx) == startArt.getOption()) {
				optionSelektor.setSelectedIndex(idx);
			}
		}
		optionSelektor.addListener(new ComboBox.Listener() {
			@Override
			public void onSelectionChanged(int selectedIndex, int previousSelection) {
				startArt.setOption(optionSelektor.getItem(selectedIndex));
			}
		});
		mainPanel.addComponent(optionSelektor, GridLayout.createHorizontallyFilledLayoutData(1));
		
		CheckBox neustartField = new CheckBox("Neustart");
		neustartField.addListener(new CheckBox.Listener() {
			@Override
			public void onStatusChanged(boolean checked) {
				startArt.setNeuStart(checked);
			}
		});
		neustartField.setChecked(startArt.getNeuStart());
		mainPanel.addComponent(neustartField, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new Label("Intervall:"));
		TextBox intervallField = new TextBox(startArt.getIntervall() == null ? "" : startArt.getIntervall()) {
			@Override
			protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
				startArt.setIntervall(getText());
				super.afterLeaveFocus(direction, nextInFocus);
			}
		};
		mainPanel.addComponent(intervallField, GridLayout.createHorizontallyFilledLayoutData(1));
	}
	
	public StartArt getElement() {
		return startArt;
	}
}
