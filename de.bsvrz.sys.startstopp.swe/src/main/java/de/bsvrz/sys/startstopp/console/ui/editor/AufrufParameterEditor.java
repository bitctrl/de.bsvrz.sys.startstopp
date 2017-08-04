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
import java.util.List;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.input.KeyStroke;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;

public class AufrufParameterEditor extends StartStoppElementEditor<List<String>> {

	private List<String> parameter = new ArrayList<>();
	private Panel parameterPanel;

	@Inject
	public AufrufParameterEditor(@Assisted Inkarnation inkarnation) {
		super("Aufrufparameter");
		this.parameter.addAll(inkarnation.getAufrufParameter());
	}

	protected void initComponents(Panel mainPanel) {

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

		mainPanel.addComponent(parameterPanel);
	}

	private int getBoxLine(TextBox textBox) {
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
			}

			return true;
		}
		System.err.println("AufrufParameterEditor: " + key);
		// TODO Auto-generated method stub
		return super.handleInput(key);
	}

	@Override
	public List<String> getElement() {
		return parameter;
	}
}
