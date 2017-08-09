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

import com.googlecode.lanterna.input.KeyStroke;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartArt;
import de.bsvrz.sys.startstopp.api.jsonschema.StartFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppFehlerVerhalten;
import de.bsvrz.sys.startstopp.console.ui.JaNeinDialog;

public class InkarnationTable extends EditableTable<Inkarnation> {

	private StartStoppSkript skript;

	public InkarnationTable(StartStoppSkript skript) {
		super(skript.getInkarnationen(), "Name", "Typ", "Startart");
		this.skript = skript;

		initUI();
	}

	private void initUI() {

		setSelectAction(new Runnable() {
			@Override
			public void run() {
				Inkarnation inkarnation = getSelectedElement();
				InkarnationEditor editor = new InkarnationEditor(skript, inkarnation);
				if (editor.showDialog(getTextGUI())) {
					replaceCurrentElementWith(editor.getElement());
				}
			}
		});
	}

	@Override
	public Result handleKeyStroke(KeyStroke keyStroke) {

		if (SkriptEditor.isInsertAfterKey(keyStroke)) {
			int row = getSelectedRow() + 1;
			Inkarnation inkarnation = new Inkarnation().withInkarnationsName("NeueInkarnation").withApplikation("java")
					.withStartArt(new StartArt()).withStartFehlerVerhalten(new StartFehlerVerhalten())
					.withStoppFehlerVerhalten(new StoppFehlerVerhalten());
			InkarnationEditor editor = new InkarnationEditor(skript, inkarnation);
			if (editor.showDialog(getTextGUI())) {
				addElement(row, editor.getElement());
			}
			return Result.HANDLED;
		}

		if (SkriptEditor.isDeleteKey(keyStroke)) {
			JaNeinDialog dialog = new JaNeinDialog("Löschen",
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

	@Override
	protected Inkarnation requestNewElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Inkarnation editElement(Inkarnation oldElement) {
		// TODO Auto-generated method stub
		return null;
	}
}