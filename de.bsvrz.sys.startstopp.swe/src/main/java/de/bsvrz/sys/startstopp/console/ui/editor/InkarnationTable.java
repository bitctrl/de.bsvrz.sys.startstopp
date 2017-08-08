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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartArt;
import de.bsvrz.sys.startstopp.api.jsonschema.StartFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppFehlerVerhalten;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.console.ui.GuiComponentFactory;
import de.bsvrz.sys.startstopp.console.ui.JaNeinDialog;
import de.bsvrz.sys.startstopp.console.ui.StartStoppTable;

public class InkarnationTable extends StartStoppTable<Inkarnation> {

	@Inject
	GuiComponentFactory factory;

	private WindowBasedTextGUI gui;

	private StartStoppSkript skript;

	@Inject
	public InkarnationTable(WindowBasedTextGUI gui, @Assisted StartStoppSkript skript) throws StartStoppException {
		super(skript.getInkarnationen(), "Name", "Typ", "Startart");
		this.gui = gui;
		this.skript = skript;
	}
	
	@Inject
	@PostConstruct
	private void initUI() {
		
		setSelectAction(new Runnable() {
			@Override
			public void run() {
				Inkarnation inkarnation = getSelectedElement();
				InkarnationEditor editor = factory.createInkarnationEditor(skript, inkarnation);
				if( editor.showDialog(gui)) {
					replaceCurrentElementWith(editor.getElement());
				}
			}
		});
	}

	@Override
	public Result handleKeyStroke(KeyStroke keyStroke) {

		if (SkriptEditor.isInsertAfterKey(keyStroke)) {
			int row = getSelectedRow() + 1;
			Inkarnation inkarnation = new Inkarnation().withInkarnationsName("NeueInkarnation")
						.withApplikation("java").withStartArt(new StartArt())
						.withStartFehlerVerhalten(new StartFehlerVerhalten())
						.withStoppFehlerVerhalten(new StoppFehlerVerhalten());
			InkarnationEditor editor = factory.createInkarnationEditor(skript, inkarnation);
			if( editor.showDialog(gui)) {
				addElement(row, editor.getElement());
			}
			return Result.HANDLED;
		}
		
		if( SkriptEditor.isDeleteKey(keyStroke)) {
			JaNeinDialog dialog = factory.createJaNeinDialog("Löschen",
						"Soll die ausgewählte Inkarnation wirklich gelöscht werden?");
			if (dialog.display()) {
				removeCurrentElement();
			}
			return Result.HANDLED;
		}

		return super.handleKeyStroke(keyStroke);
	}

	@Override
	protected List<String> getStringsFor(Inkarnation inkarnation) {
		List<String> result = new ArrayList<>();
		result.add(inkarnation.getInkarnationsName());
		result.add(inkarnation.getInkarnationsTyp().toString());
		result.add(inkarnation.getStartArt().getOption().toString());
		return result;
	}
}