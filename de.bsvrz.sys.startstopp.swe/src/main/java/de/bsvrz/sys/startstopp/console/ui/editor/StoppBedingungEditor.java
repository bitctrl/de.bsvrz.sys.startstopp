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
import com.googlecode.lanterna.gui2.CheckBox;
import com.googlecode.lanterna.gui2.ComboBox;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;

import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;

public class StoppBedingungEditor extends StartStoppElementEditor<StoppBedingung> {

	private static final String KEIN_RECHNER = "<kein Rechner>";
	private StoppBedingung stoppBedingung;
	private StartStoppSkript skript;
	private boolean bedingungUsed = false;

	@Inject
	public StoppBedingungEditor(@Assisted StartStoppSkript skript, @Assisted StoppBedingung stoppBedingung) {
		super("StartStopp - Editor: Inkarnation: ");

		this.skript = skript;
		bedingungUsed = stoppBedingung.getNachfolger().isEmpty();
		this.stoppBedingung = (StoppBedingung) Util.cloneObject(stoppBedingung);
	}

	protected void initComponents (Panel mainPanel) {
		mainPanel.setLayoutManager(new GridLayout(1).setLeftMarginSize(1).setRightMarginSize(1));

		CheckBox bedingungsCheck = new CheckBox("Stoppbedingung prüfen");
		bedingungsCheck.setChecked(bedingungUsed);
		bedingungsCheck.addListener(new CheckBox.Listener() {
			@Override
			public void onStatusChanged(boolean checked) {
				bedingungUsed = checked;
			}
		});
		mainPanel.addComponent(bedingungsCheck);

		mainPanel.addComponent(new Label("Nachfolger:"));
		Panel nachfolgerPanel = new Panel(new GridLayout(1));
		// TODO Vorgänger eintragen
		mainPanel.addComponent(nachfolgerPanel);

		mainPanel.addComponent(new Label("Rechner:"));
		ComboBox<String> rechnerSelektor = new ComboBox<>(KEIN_RECHNER);
		for (Rechner rechner : skript.getGlobal().getRechner()) {
			rechnerSelektor.addItem(rechner.getName().trim());
		}
		String rechnerName = stoppBedingung.getRechner();
		if ((rechnerName == null) || rechnerName.trim().isEmpty()) {
			rechnerSelektor.setSelectedIndex(0);
		} else {
			for (int idx = 0; idx < rechnerSelektor.getItemCount(); idx++) {
				if (rechnerSelektor.getItem(idx).equals(rechnerName.trim())) {
					rechnerSelektor.setSelectedIndex(idx);
					break;
				}
			}
		}
		rechnerSelektor.addListener(new ComboBox.Listener() {
			@Override
			public void onSelectionChanged(int selectedIndex, int previousSelection) {
				String name = rechnerSelektor.getItem(selectedIndex);
				if (KEIN_RECHNER.equals(name)) {
					stoppBedingung.setRechner(null);
				} else {
					stoppBedingung.setRechner(name);
				}
			}
		});

		mainPanel.addComponent(new Label("Wartezeit:"));
		TextBox warteZeitField = new TextBox(
				stoppBedingung.getWartezeit() == null ? "" : stoppBedingung.getWartezeit()) {
			@Override
			protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
				stoppBedingung.setWartezeit(getText());
				super.afterLeaveFocus(direction, nextInFocus);
			}
		};
		mainPanel.addComponent(warteZeitField, GridLayout.createHorizontallyFilledLayoutData(1));
	}

	public StoppBedingung getElement() {
		if (bedingungUsed) {
			return stoppBedingung;
		}

		return null;
	}
}
