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

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.ComboBox;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;

import de.bsvrz.sys.startstopp.api.jsonschema.StartFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;

public class StartFehlerVerhaltenEditor extends DialogWindow {

	private StartFehlerVerhalten startFehlerVerhalten;
	private boolean okPressed = false;

	public StartFehlerVerhaltenEditor(StartFehlerVerhalten startFehlerVerhalten) {
		super("StartStopp - Editor: Inkarnation: ");


		if (startFehlerVerhalten == null) {
			this.startFehlerVerhalten = new StartFehlerVerhalten();
		} else {
			this.startFehlerVerhalten = (StartFehlerVerhalten) Util.cloneObject(startFehlerVerhalten);
		}

		setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.FIT_TERMINAL_WINDOW));
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
		TextBox warteZeitField = new TextBox(
				startFehlerVerhalten.getWiederholungen() == null ? "" : startFehlerVerhalten.getWiederholungen()) {
			@Override
			protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
				startFehlerVerhalten.setWiederholungen(getText());
				super.afterLeaveFocus(direction, nextInFocus);
			}
		};
		mainPanel.addComponent(warteZeitField, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));

		buttonPanel.setLayoutData(
				GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER, false, false))
				.addTo(mainPanel);

		setComponent(mainPanel);
	}

	@Override
	public Boolean showDialog(WindowBasedTextGUI textGUI) {
		super.showDialog(textGUI);
		return okPressed;
	}

	public StartFehlerVerhalten getStartFehlerVerhalten() {
		return startFehlerVerhalten;
	}
}
