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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.input.KeyStroke;

import de.bsvrz.sys.startstopp.api.jsonschema.MakroDefinition;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;

public abstract class StartStoppElementEditor<T> extends DialogWindow {

	private boolean okPressed;
	private StartStoppSkript skript;

	@Inject
	public StartStoppElementEditor(@Assisted StartStoppSkript skript, @Assisted String title) {
		super(title);
		this.skript = skript;
		setHints(Arrays.asList(new Hint[] { Hint.CENTERED, Hint.FIT_TERMINAL_WINDOW }));
	}

	@Inject
	@PostConstruct
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

		initComponents(mainPanel);

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

	protected abstract void initComponents(Panel mainPanel);

	public abstract T getElement();

	@Override
	public boolean handleInput(KeyStroke key) {

		if (SkriptEditor.isSelectMakroKey(key)) {
			MakroSelektor selektor = new MakroSelektor(getTextGUI(), skript);
			MakroDefinition selectedMakroDefinition = selektor.getSelectedMakroDefinition();
			if (selectedMakroDefinition != null) {
				Interactable interactable = getFocusedInteractable();
				if (interactable instanceof TextBox) {
					TextBox textBox = (TextBox) interactable;
					int position = textBox.getCursorLocation().getColumn();
					StringBuilder oldText = new StringBuilder(textBox.getText());
					oldText.insert(position, "%" + selectedMakroDefinition.getName() + "%");
					textBox.setText(oldText.toString());
				}
			}
			return true;
		}

		return super.handleInput(key);
	}

	public StartStoppSkript getSkript() {
		return skript;
	}

}
