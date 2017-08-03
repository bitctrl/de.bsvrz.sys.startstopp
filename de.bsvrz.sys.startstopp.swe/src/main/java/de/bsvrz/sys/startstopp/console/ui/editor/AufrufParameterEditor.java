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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.input.KeyStroke;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;

public class AufrufParameterEditor extends DialogWindow {

	private List<String> result;
	private Button okButton;
	private Button cancelButton;
	private List<String> parameter = new ArrayList<>();
	private Panel parameterPanel;

	public AufrufParameterEditor(Inkarnation inkarnation) {
		super("StartStopp - Editor: Inkarnation: ");
		this.parameter.addAll(inkarnation.getAufrufParameter());
		setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.FIT_TERMINAL_WINDOW));
		setCloseWindowWithEscape(true);
		initUI();
	}

	private void initUI() {
		Panel buttonPanel = new Panel();
		buttonPanel.setLayoutManager(new GridLayout(2).setHorizontalSpacing(1));
		okButton = new Button("OK", new Runnable() {

			@Override
			public void run() {
				result = parameter;
				close();
			}
		});
		buttonPanel.addComponent(okButton);
		cancelButton = new Button("Abbrechen", new Runnable() {

			@Override
			public void run() {
				close();
			}
		});
		buttonPanel.addComponent(cancelButton);

		parameterPanel = new Panel();
		parameterPanel.setLayoutManager(new GridLayout(1));

		for (String param : parameter) {
			TextBox box = new TextBox(param) {
				@Override
				protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
					int row = getBoxLine(this);
					if (row >= 0) {
						parameter.remove(row);
						parameter.add(row, getText());
					}
					super.afterLeaveFocus(direction, nextInFocus);
				}
			};
			parameterPanel.addComponent(box, GridLayout.createHorizontallyFilledLayoutData(1));
		}

		Panel mainPanel = new Panel();
		mainPanel.setLayoutManager(new GridLayout(1).setLeftMarginSize(1).setRightMarginSize(1));

		mainPanel.addComponent(parameterPanel);
		mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));

		buttonPanel.setLayoutData(
				GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER, false, false))
				.addTo(mainPanel);

		setComponent(mainPanel);
	}

	protected int getBoxLine(TextBox textBox) {
		int row = 0;
		for (Component child : parameterPanel.getChildren()) {
			if (child == textBox) {
				return row;// TODO Auto-generated method stub
			}
			row++;
		}
		return -1;
	}

	private int getSelectedLine() {
		int line = 0;
		for (Component child : parameterPanel.getChildren()) {
			if (child instanceof TextBox) {
				if (((TextBox) child).isFocused()) {
					return line;
				}
			}
			line++;
		}
		return -1;
	}

	@Override
	public List<String> showDialog(WindowBasedTextGUI textGUI) {
		super.showDialog(textGUI);
		return result;
	}

	@Override
	public boolean handleInput(KeyStroke key) {
		if (Util.isInsertAfterKey(key)) {

			int row = getSelectedLine();
			if (row >= 0) {
				parameter.add(row + 1, "Neuer Parameter");
			} else {
				parameter.add("Neuer Parameter");
			}

			TextBox box = new TextBox("Neuer Parameter") {
				@Override
				protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
					int row = getBoxLine(this);
					if (row >= 0) {
						parameter.remove(row);
						parameter.add(row, getText());
					}
					super.afterLeaveFocus(direction, nextInFocus);
				}
			};
			parameterPanel.addComponent(box, GridLayout.createHorizontallyFilledLayoutData(1));

			int count = 0;
			for (Component component : parameterPanel.getChildren()) {
				if (component instanceof TextBox) {
					((TextBox) component).setText(parameter.get(count++));
				}
			}

			return true;
		} else if (Util.isInsertBeforeKey(key)) {
			int row = getSelectedLine();
			if (row >= 0) {
				parameter.add(row, "Neuer Parameter");
			} else {
				parameter.add("Neuer Parameter");
			}

			TextBox box = new TextBox("Neuer Parameter") {
				@Override
				protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
					int row = getBoxLine(this);
					if (row >= 0) {
						parameter.remove(row);
						parameter.add(row, getText());
					}
					super.afterLeaveFocus(direction, nextInFocus);
				}
			};
			parameterPanel.addComponent(box, GridLayout.createHorizontallyFilledLayoutData(1));

			int count = 0;
			for (Component component : parameterPanel.getChildren()) {
				if (component instanceof TextBox) {
					((TextBox) component).setText(parameter.get(count++));
				}
			}

			return true;
		} else if (Util.isDeleteKey(key)) {
			int row = getSelectedLine();
			if (row >= 0) {
				Interactable toDelete = (Interactable) parameterPanel.getChildren().iterator().next();
				parameterPanel.removeComponent(toDelete);
				parameter.remove(row);
				int count = 0;
				for (Component component : parameterPanel.getChildren()) {
					if (component instanceof TextBox) {
						((TextBox) component).setText(parameter.get(count++));
					}
				}
				setFocusedInteractable(okButton);
			}

			return true;
		}
		System.err.println("AufrufParameterEditor: " + key);
		// TODO Auto-generated method stub
		return super.handleInput(key);
	}

}
