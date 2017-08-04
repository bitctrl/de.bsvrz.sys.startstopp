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
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;

import de.bsvrz.sys.startstopp.api.jsonschema.Util;
import de.bsvrz.sys.startstopp.api.jsonschema.ZugangDav;

public class ZugangDavEditor extends StartStoppElementEditor<ZugangDav> {

	private ZugangDav zugangDav;

	@Inject
	public ZugangDavEditor(@Assisted ZugangDav zugangDav) {
		super("Zugang Datenverteiler");

		if (zugangDav == null) {
			this.zugangDav = new ZugangDav();
		} else {
			this.zugangDav = (ZugangDav) Util.cloneObject(zugangDav);
		}
	}

	protected void initComponents(Panel mainPanel) {
		mainPanel.setLayoutManager(new GridLayout(1).setLeftMarginSize(1).setRightMarginSize(1));

		mainPanel.addComponent(new Label("Adresse:"));
		TextBox box = new TextBox(zugangDav.getAdresse()) {
			@Override
			protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
				zugangDav.setAdresse(getText());
			}
		};
		mainPanel.addComponent(box, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new Label("Port:"));
		box = new TextBox(zugangDav.getPort()) {
			@Override
			protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
				zugangDav.setPort(getText());
			}
		};
		mainPanel.addComponent(box, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new Label("Nutzername:"));
		box = new TextBox(zugangDav.getUserName()) {
			@Override
			protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
				zugangDav.setUserName(getText());
			}
		};
		mainPanel.addComponent(box, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new Label("Passwort:"));
		box = new TextBox(zugangDav.getPassWord()) {
			@Override
			protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
				zugangDav.setPassWord(getText());
			}
		}.setMask('*');
		mainPanel.addComponent(box, GridLayout.createHorizontallyFilledLayoutData(1));
	}

	public ZugangDav getElement() {
		return zugangDav;
	}
}
