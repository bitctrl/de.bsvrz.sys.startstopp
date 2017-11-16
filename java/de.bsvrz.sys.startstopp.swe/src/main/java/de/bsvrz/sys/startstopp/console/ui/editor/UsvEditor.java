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

import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;

import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.Usv;
import de.bsvrz.sys.startstopp.api.util.Util;

class UsvEditor extends StartStoppElementEditor<Usv> {

	private Usv usv;

	UsvEditor(StartStoppSkript skript) {
		super(skript, "USV");

		if (skript.getGlobal().getUsv() == null) {
			this.usv = new Usv("");
		} else {
			this.usv = (Usv) Util.cloneObject(skript.getGlobal().getUsv());
		}
	}

	@Override
	protected void initComponents(Panel mainPanel) {
		mainPanel.setLayoutManager(new GridLayout(1).setLeftMarginSize(1).setRightMarginSize(1));

		mainPanel.addComponent(new Label("PID:"));
		TextBox box = new TextBox(Util.nonEmptyString(usv.getPid())) {
			@Override
			protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
				usv.setPid(getText());
			}
		};
		mainPanel.addComponent(box, GridLayout.createHorizontallyFilledLayoutData(1));
	}

	@Override
	public Usv getElement() {
		if (Util.nonEmptyString(usv.getPid().trim()).isEmpty()) {
			return null;
		}
		return usv;
	}

}
