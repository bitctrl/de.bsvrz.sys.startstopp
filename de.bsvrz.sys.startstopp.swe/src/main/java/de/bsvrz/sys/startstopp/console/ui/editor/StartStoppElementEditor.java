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
import java.util.LinkedHashMap;
import java.util.Map;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import de.bsvrz.sys.startstopp.api.jsonschema.MakroDefinition;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.console.ui.HasHotkey;
import de.bsvrz.sys.startstopp.console.ui.StartStoppButton;

public abstract class StartStoppElementEditor<T> extends DialogWindow {

	private boolean okPressed;
	private StartStoppSkript skript;
	private Map<Character, HasHotkey> hotkeys = new LinkedHashMap<>();

	public StartStoppElementEditor(StartStoppSkript skript, String title) {
		super(title);
		this.skript = skript;
		setHints(Arrays.asList(new Hint[] { Hint.CENTERED, Hint.FIT_TERMINAL_WINDOW }));
	}

	private void initUI() {
		Panel buttonPanel = new Panel();
		buttonPanel.setLayoutManager(new GridLayout(2).setHorizontalSpacing(1));
		Button okButton = new StartStoppButton("OK", () -> {
			okPressed = true;
			close();
		});
		buttonPanel.addComponent(okButton);
		Button cancelButton = new StartStoppButton("Abbrechen", () -> close());
		buttonPanel.addComponent(cancelButton);

		Panel mainPanel = new Panel();
		mainPanel.setLayoutManager(new GridLayout(1).setLeftMarginSize(1).setRightMarginSize(1));
		initComponents(mainPanel);

		mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));
		
		buttonPanel.setLayoutData(
				GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER, false, false))
				.addTo(mainPanel);

		collectHotkeys(mainPanel);
		setComponent(mainPanel);
	}

	private void collectHotkeys(Container mainPanel) {
		for (Component component : mainPanel.getChildren()) {
			if (component instanceof HasHotkey) {
				Character hotkey = ((HasHotkey) component).getHotkey();
				hotkeys.put(Character.toLowerCase(hotkey), (HasHotkey) component);
			}
			if (component instanceof Container) {
				collectHotkeys((Container) component);
			}
		}
	}

	@Override
	public Boolean showDialog(WindowBasedTextGUI textGUI) {
		initUI();
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

		if (key.getKeyType() == KeyType.Character && key.isAltDown()) {
			HasHotkey component = hotkeys.get(Character.toLowerCase(key.getCharacter()));
			if (component != null) {
				component.takeFocus();
				return true;
			}
		}

		return super.handleInput(key);
	}

	public StartStoppSkript getSkript() {
		return skript;
	}
	
	@Override
	public WindowBasedTextGUI getTextGUI() {
		return super.getTextGUI();
	}

}
