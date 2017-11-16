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

import com.googlecode.lanterna.gui2.ComboBox;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.util.Util;

class StartFehlerVerhaltenEditor extends StartStoppElementEditor<StartFehlerVerhalten> {

	private StartFehlerVerhalten startFehlerVerhalten;

	StartFehlerVerhaltenEditor(StartStoppSkript skript, Inkarnation inkarnation) {
		super(skript, "Startfehlerverhalten");

		if (inkarnation.getStartFehlerVerhalten() == null) {
			this.startFehlerVerhalten = new StartFehlerVerhalten();
		} else {
			this.startFehlerVerhalten = (StartFehlerVerhalten) Util.cloneObject(inkarnation.getStartFehlerVerhalten());
		}
	}

	@Override
	protected void initComponents(Panel mainPanel) {
		mainPanel.setLayoutManager(new GridLayout(1).setLeftMarginSize(1).setRightMarginSize(1));

		mainPanel.addComponent(new Label("Option:"));
		ComboBox<StartFehlerVerhalten.Option> optionSelektor = new ComboBox<>(StartFehlerVerhalten.Option.values());
		for (int idx = 0; idx < optionSelektor.getItemCount(); idx++) {
			if (optionSelektor.getItem(idx) == startFehlerVerhalten.getOption()) {
				optionSelektor.setSelectedIndex(idx);
				break;
			}
		}
		optionSelektor.addListener(new ComboBox.Listener() {
			@Override
			public void onSelectionChanged(int selectedIndex, int previousSelection) {
				startFehlerVerhalten.setOption(optionSelektor.getItem(selectedIndex));
			}
		});
		mainPanel.addComponent(optionSelektor, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new Label("Wiederholungen:"));
		String wiederHolungStr;
		if (startFehlerVerhalten.getWiederholungen() == null) {
			wiederHolungStr = "";
		} else {
			wiederHolungStr = startFehlerVerhalten.getWiederholungen();
		}

		TextBox warteZeitField = new TextBox(wiederHolungStr) {
			@Override
			protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
				startFehlerVerhalten.setWiederholungen(getText());
				super.afterLeaveFocus(direction, nextInFocus);
			}
		};
		mainPanel.addComponent(warteZeitField, GridLayout.createHorizontallyFilledLayoutData(1));
	}

	@Override
	public StartFehlerVerhalten getElement() {
		return startFehlerVerhalten;
	}
}
