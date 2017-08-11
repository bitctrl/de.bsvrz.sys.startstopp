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

import java.util.Collections;
import java.util.List;

import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.CheckBox;
import com.googlecode.lanterna.gui2.ComboBox;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;
import de.bsvrz.sys.startstopp.console.ui.EditableTable;

class StartBedingungEditor extends StartStoppElementEditor<StartBedingung> {

	private class VorgaengerTable extends EditableTable<String> {

		VorgaengerTable(List<String> dataList, String ... columnName) {
			super(dataList, columnName);
		}

		@Override
		protected String requestNewElement() {
			InkarnationSelektor selektor = new InkarnationSelektor(skript);
			Inkarnation inkarnation = selektor.getInkarnation(getTextGUI());
			if( inkarnation == null) {
				return null;
			}
			return inkarnation.getInkarnationsName();
		}

		@Override
		protected String editElement(String oldElement) {
			return null;
		}

		@Override
		protected List<String> getStringsFor(String element) {
			return Collections.singletonList(element);
		}
	}

	private static final String KEIN_RECHNER = "<kein Rechner>";

	private StartBedingung startBedingung;
	private StartStoppSkript skript;
	private boolean bedingungUsed = false;

	StartBedingungEditor(StartStoppSkript skript, Inkarnation inkarnation) {
		super(skript, "Startbedingung");

		this.skript = skript;

		if (inkarnation.getStartBedingung() == null) {
			bedingungUsed = false;
			this.startBedingung = new StartBedingung();
		} else {
			bedingungUsed = true;
			this.startBedingung = (StartBedingung) Util.cloneObject(inkarnation.getStartBedingung());
		}

	}

	protected void initComponents(Panel mainPanel) {
		mainPanel.setLayoutManager(new GridLayout(1).setLeftMarginSize(1).setRightMarginSize(1));

		CheckBox bedingungsCheck = new CheckBox("Startbedingung prüfen");
		bedingungsCheck.setChecked(bedingungUsed);
		bedingungsCheck.addListener(new CheckBox.Listener() {
			@Override
			public void onStatusChanged(boolean checked) {
				bedingungUsed = checked;
			}
		});
		mainPanel.addComponent(bedingungsCheck);

		VorgaengerTable table = new VorgaengerTable(startBedingung.getVorgaenger(), "Vorgänger");
		mainPanel.addComponent(table.withBorder(Borders.singleLine("Vorgänger")), GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new Label("Warteart:"));
		ComboBox<StartBedingung.Warteart> warteArtSelektor = new ComboBox<>(StartBedingung.Warteart.values());
		for (int idx = 0; idx < warteArtSelektor.getItemCount(); idx++) {
			if (warteArtSelektor.getItem(idx) == startBedingung.getWarteart()) {
				warteArtSelektor.setSelectedIndex(idx);
				break;
			}
		}
		warteArtSelektor.addListener(new ComboBox.Listener() {
			@Override
			public void onSelectionChanged(int selectedIndex, int previousSelection) {
				startBedingung.setWarteart(warteArtSelektor.getItem(selectedIndex));
			}
		});
		mainPanel.addComponent(warteArtSelektor, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new Label("Rechner:"));
		ComboBox<String> rechnerSelektor = new ComboBox<>(KEIN_RECHNER);
		for (Rechner rechner : skript.getGlobal().getRechner()) {
			rechnerSelektor.addItem(rechner.getName().trim());
		}
		String rechnerName = startBedingung.getRechner();
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
					startBedingung.setRechner(null);
				} else {
					startBedingung.setRechner(name);
				}
			}
		});
		mainPanel.addComponent(rechnerSelektor, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new Label("Wartezeit:"));
		TextBox warteZeitField = new TextBox(
				startBedingung.getWartezeit() == null ? "" : startBedingung.getWartezeit()) {
			@Override
			protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
				String neueWarteZeit = getText();
				startBedingung.setWartezeit(neueWarteZeit);
				super.afterLeaveFocus(direction, nextInFocus);
			}
		};
		mainPanel.addComponent(warteZeitField, GridLayout.createHorizontallyFilledLayoutData(1));
	}

	@Override
	public StartBedingung getElement() {
		if (bedingungUsed) {
			return startBedingung;
		}

		return null;
	}
}
