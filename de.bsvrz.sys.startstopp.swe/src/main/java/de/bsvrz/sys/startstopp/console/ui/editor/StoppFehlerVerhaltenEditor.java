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

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.googlecode.lanterna.gui2.ComboBox;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;

import de.bsvrz.sys.startstopp.api.jsonschema.StoppFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;

public class StoppFehlerVerhaltenEditor extends StartStoppElementEditor<StoppFehlerVerhalten> {

	private StoppFehlerVerhalten stoppFehlerVerhalten;

	@Inject
	public StoppFehlerVerhaltenEditor(@Assisted StoppFehlerVerhalten stoppFehlerVerhalten) {
		super("StartStopp - Editor: Inkarnation: ");


		if (stoppFehlerVerhalten == null) {
			this.stoppFehlerVerhalten = new StoppFehlerVerhalten();
		} else {
			this.stoppFehlerVerhalten = (StoppFehlerVerhalten) Util.cloneObject(stoppFehlerVerhalten);
		}
	}

	protected void initComponents(Panel mainPanel) {
		mainPanel.setLayoutManager(new GridLayout(1).setLeftMarginSize(1).setRightMarginSize(1));

		mainPanel.addComponent(new Label("Option:"));
		ComboBox<StoppFehlerVerhalten.Option> optionSelektor = new ComboBox<>(StoppFehlerVerhalten.Option.values());
		for (int idx = 0; idx < optionSelektor.getItemCount(); idx++) {
			if (optionSelektor.getItem(idx) == stoppFehlerVerhalten.getOption()) {
				optionSelektor.setSelectedIndex(idx);
				break;
			}
		}
		optionSelektor.addListener(new ComboBox.Listener() {
			@Override
			public void onSelectionChanged(int selectedIndex, int previousSelection) {
				stoppFehlerVerhalten.setOption(optionSelektor.getItem(selectedIndex));
			}
		});
		mainPanel.addComponent(optionSelektor, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new Label("Wiederholungen:"));
		TextBox warteZeitField = new TextBox(
				stoppFehlerVerhalten.getWiederholungen() == null ? "" : stoppFehlerVerhalten.getWiederholungen()) {
			@Override
			protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
				stoppFehlerVerhalten.setWiederholungen(getText());
				super.afterLeaveFocus(direction, nextInFocus);
			}
		};
		mainPanel.addComponent(warteZeitField, GridLayout.createHorizontallyFilledLayoutData(1));
	}

	public StoppFehlerVerhalten getElement() {
		return stoppFehlerVerhalten;
	}
}
