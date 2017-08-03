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
import java.util.List;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Button.Listener;
import com.googlecode.lanterna.gui2.CheckBox;
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
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation.InkarnationsTyp;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;

public class InkarnationEditor extends DialogWindow {

	private MessageDialogButton result;
	private TextBox nameField;
	private Inkarnation inkarnation;
	private TextBox applikationField;
	private StartStoppSkript skript;

	public InkarnationEditor(StartStoppSkript skript, Inkarnation inkarnation) {
		super("StartStopp - Editor: Inkarnation: ");

		this.skript = skript;
		this.inkarnation = (Inkarnation) Util.cloneObject(inkarnation);
		setHints(Arrays.asList(Window.Hint.CENTERED));
		setCloseWindowWithEscape(true);
		initUI();
	}

	private void initUI() {
		Panel buttonPanel = new Panel();
		buttonPanel.setLayoutManager(new GridLayout(2).setHorizontalSpacing(1).setTopMarginSize(1));
		Button okButton = new Button("OK", new Runnable() {

			@Override
			public void run() {
				result = MessageDialogButton.OK;
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
		mainPanel.setLayoutManager(new GridLayout(3).setLeftMarginSize(1).setRightMarginSize(1).setTopMarginSize(1));

		mainPanel.addComponent(new Label("Inkarnationsname:"));
		nameField = new TextBox() {
			@Override
			protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
				inkarnation.setInkarnationsName(nameField.getText());
				super.afterLeaveFocus(direction, nextInFocus);
			}
		};
		nameField.setText(inkarnation.getInkarnationsName());
		mainPanel.addComponent(nameField, GridLayout.createHorizontallyFilledLayoutData(2));

		mainPanel.addComponent(new Label("Applikation:"));
		applikationField = new TextBox("");
		applikationField.setText(inkarnation.getApplikation());
		mainPanel.addComponent(applikationField, GridLayout.createHorizontallyFilledLayoutData(2));

		mainPanel.addComponent(new Label("Parameter:"));
		List<String> aufrufParameter = inkarnation.getAufrufParameter();
		mainPanel.addComponent(new Label(aufrufParameter.isEmpty() ? "" : aufrufParameter.get(0) + ", ..."));
		Button parameterButton = new Button("Bearbeiten");
		parameterButton.addListener(new Listener() {
			@Override
			public void onTriggered(Button button) {
				AufrufParameterEditor editor = new AufrufParameterEditor(inkarnation);
				List<String> parameter = editor.showDialog(getTextGUI());
				if( parameter != null) {
					inkarnation.getAufrufParameter().clear();
					inkarnation.getAufrufParameter().addAll(parameter);
				}
			}
		});
		mainPanel.addComponent(parameterButton, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new Label("Typ:"));
		ComboBox<InkarnationsTyp> typSelektor = new ComboBox<InkarnationsTyp>(InkarnationsTyp.values());
		for( int idx = 0; idx < typSelektor.getItemCount(); idx++) {
			if( typSelektor.getItem(idx) == inkarnation.getInkarnationsTyp()) {
				typSelektor.setSelectedIndex(idx);
			}
		}
		typSelektor.addListener(new ComboBox.Listener() {
			@Override
			public void onSelectionChanged(int selectedIndex, int previousSelection) {
				inkarnation.setInkarnationsTyp(typSelektor.getItem(selectedIndex));
			}
		});
		mainPanel.addComponent(typSelektor, GridLayout.createHorizontallyFilledLayoutData(2));

		mainPanel.addComponent(new Label("Startart:"));
		mainPanel.addComponent(new Label(inkarnation.getStartArt().getOption().toString()));
		Button startArtButton = new Button("Bearbeiten");
		startArtButton.addListener(new Listener() {
			@Override
			public void onTriggered(Button button) {
				StartArtEditor editor = new StartArtEditor(inkarnation.getStartArt());
				if( editor.showDialog(getTextGUI())) {
					inkarnation.setStartArt(editor.getStartArt());
				}
			}
		});
		mainPanel.addComponent(startArtButton, GridLayout.createHorizontallyFilledLayoutData(1));

		CheckBox initCheckbox = new CheckBox("Initialisieren");
		initCheckbox.setChecked(inkarnation.getInitialize());
		initCheckbox.addListener(new CheckBox.Listener() {
			@Override
			public void onStatusChanged(boolean checked) {
				inkarnation.setInitialize(checked);
			}});
		mainPanel.addComponent(initCheckbox, GridLayout.createHorizontallyFilledLayoutData(1));

		CheckBox setInkarnationsNameCheckbox = new CheckBox("Setze Inkarnationsname");
		setInkarnationsNameCheckbox.setChecked(inkarnation.getMitInkarnationsName());
		setInkarnationsNameCheckbox.addListener(new CheckBox.Listener() {
			@Override
			public void onStatusChanged(boolean checked) {
				inkarnation.setMitInkarnationsName(checked);
			}});
		mainPanel.addComponent(setInkarnationsNameCheckbox, GridLayout.createHorizontallyFilledLayoutData(2));

		mainPanel.addComponent(new Label("Startbedingung:"));
		StartBedingung startBedingung = inkarnation.getStartBedingung();
		mainPanel.addComponent(new Label(startBedingung == null ? "Keine" : startBedingung.getVorgaenger().get(0)));
		Button startBedingungButton = new Button("Bearbeiten");
		startBedingungButton.addListener(new Listener() {
			@Override
			public void onTriggered(Button button) {
				StartBedingungEditor editor = new StartBedingungEditor(skript, inkarnation.getStartBedingung());
				if( editor.showDialog(getTextGUI())) {
					inkarnation.setStartBedingung(editor.getStartBedingung());
				}
			}
		});
		mainPanel.addComponent(startBedingungButton, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new Label("Startfehlerverhalten:"));
		mainPanel.addComponent(new Label(inkarnation.getStartFehlerVerhalten().getOption().toString()));
		Button startFehlerVerhaltenButton = new Button("Bearbeiten");
		startFehlerVerhaltenButton.addListener(new Listener() {
			@Override
			public void onTriggered(Button button) {
				StartFehlerVerhaltenEditor editor = new StartFehlerVerhaltenEditor(inkarnation.getStartFehlerVerhalten());
				if( editor.showDialog(getTextGUI())) {
					inkarnation.setStartFehlerVerhalten(editor.getStartFehlerVerhalten());
				}
			}
		});
		mainPanel.addComponent(startFehlerVerhaltenButton, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new Label("Stoppbedingung:"));
		StoppBedingung stoppBedingung = inkarnation.getStoppBedingung();
		mainPanel.addComponent(new Label(stoppBedingung == null ? "Keine" : stoppBedingung.getNachfolger().get(0)));
		Button stoppBedingungButton = new Button("Bearbeiten");
		stoppBedingungButton.addListener(new Listener() {
			@Override
			public void onTriggered(Button button) {
				StoppBedingungEditor editor = new StoppBedingungEditor(skript, inkarnation.getStoppBedingung());
				if( editor.showDialog(getTextGUI())) {
					inkarnation.setStoppBedingung(editor.getStoppBedingung());
				}
			}
		});
		mainPanel.addComponent(stoppBedingungButton, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new Label("Stoppfehlerverhalten:"));
		mainPanel.addComponent(new Label(inkarnation.getStoppFehlerVerhalten().getOption().toString()));
		Button stoppFehlerVerhaltenButton = new Button("Bearbeiten");
		stoppFehlerVerhaltenButton.addListener(new Listener() {
			@Override
			public void onTriggered(Button button) {
				StoppFehlerVerhaltenEditor editor = new StoppFehlerVerhaltenEditor(inkarnation.getStoppFehlerVerhalten());
				if( editor.showDialog(getTextGUI())) {
					inkarnation.setStoppFehlerVerhalten(editor.getStoppFehlerVerhalten());
				}
			}
		});
		mainPanel.addComponent(stoppFehlerVerhaltenButton, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));

		buttonPanel.setLayoutData(
				GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER, false, false))
				.addTo(mainPanel);

		setComponent(mainPanel);
	}

	@Override
	public Inkarnation showDialog(WindowBasedTextGUI textGUI) {
		super.showDialog(textGUI);
		if (result == MessageDialogButton.OK) {
			return inkarnation;
		}
		return null;
	}
}
