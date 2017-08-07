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
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartArt;
import de.bsvrz.sys.startstopp.api.jsonschema.StartFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppFehlerVerhalten;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.console.ui.GuiComponentFactory;
import de.bsvrz.sys.startstopp.console.ui.JaNeinDialog;

public class InkarnationTable extends Table<Object> {

	@Inject
	GuiComponentFactory factory;

	private WindowBasedTextGUI gui;

	private StartStoppSkript skript;

	@Inject
	public InkarnationTable(WindowBasedTextGUI gui, @Assisted StartStoppSkript skript) throws StartStoppException {
		super("Name", "Typ", "Startart");

		this.gui = gui;
		this.skript = skript;

		for (Inkarnation inkarnation : skript.getInkarnationen()) {
			getTableModel().addRow(inkarnation.getInkarnationsName(), inkarnation.getInkarnationsTyp(),
					inkarnation.getStartArt().getOption());
		}


	}
	
	@Inject
	@PostConstruct
	private void initUI() {
		setSelectAction(new Runnable() {
			@Override
			public void run() {
				int row = getSelectedRow();
				Inkarnation inkarnation = skript.getInkarnationen().get(row);
				InkarnationEditor editor = factory.createInkarnationEditor(skript, inkarnation);
				if( editor.showDialog(gui)) {
					int idx = skript.getInkarnationen().indexOf(inkarnation);
					skript.getInkarnationen().remove(idx);
					skript.getInkarnationen().add(idx, editor.getElement());
				}
			}
		});
	}

	public Inkarnation getSelectedOnlineInkarnation() {
		int row = getSelectedRow();
		if ((row < 0) || (row >= skript.getInkarnationen().size())) {
			return null;
		}

		return skript.getInkarnationen().get(row);
	}

	@Override
	public Result handleKeyStroke(KeyStroke keyStroke) {

		if (keyStroke.getKeyType() == KeyType.Character) {
			switch (keyStroke.getCharacter()) {
			case '+':
				int row = getSelectedRow() + 1;
				Inkarnation inkarnation = new Inkarnation().withInkarnationsName("NeueInkarnation")
						.withApplikation("java").withStartArt(new StartArt())
						.withStartFehlerVerhalten(new StartFehlerVerhalten())
						.withStoppFehlerVerhalten(new StoppFehlerVerhalten());
				InkarnationEditor editor = factory.createInkarnationEditor(skript, inkarnation);
				if( editor.showDialog(gui)) {
					skript.getInkarnationen().add(row, editor.getElement());
					getTableModel().insertRow(row, Arrays.asList(inkarnation.getInkarnationsName(),
							inkarnation.getInkarnationsTyp(), inkarnation.getStartArt().getOption()));
				}
				break;
			case '-':
				JaNeinDialog dialog = factory.createJaNeinDialog("Löschen",
						"Soll die ausgewählte Inkarnation wirklich gelöscht werden?");
				if (dialog.display()) {
					int deleteRow = getSelectedRow();
					skript.getInkarnationen().remove(deleteRow);
					getTableModel().removeRow(deleteRow);
				}
				break;
			default:
			}
		}

		return super.handleKeyStroke(keyStroke);
	}

}