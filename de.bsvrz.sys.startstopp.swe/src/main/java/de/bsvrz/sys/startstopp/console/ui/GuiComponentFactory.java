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

package de.bsvrz.sys.startstopp.console.ui;

import com.google.inject.assistedinject.Assisted;
import com.googlecode.lanterna.gui2.Window;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.MakroDefinition;
import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.api.jsonschema.StartArt;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StartFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppFehlerVerhalten;
import de.bsvrz.sys.startstopp.console.ui.editor.AufrufParameterEditor;
import de.bsvrz.sys.startstopp.console.ui.editor.EditorCloseAction;
import de.bsvrz.sys.startstopp.console.ui.editor.EditorSichernAction;
import de.bsvrz.sys.startstopp.console.ui.editor.EditorVersionierenAction;
import de.bsvrz.sys.startstopp.console.ui.editor.InkarnationEditor;
import de.bsvrz.sys.startstopp.console.ui.editor.InkarnationSelektor;
import de.bsvrz.sys.startstopp.console.ui.editor.InkarnationTable;
import de.bsvrz.sys.startstopp.console.ui.editor.KernsystemEditor;
import de.bsvrz.sys.startstopp.console.ui.editor.MakroEditor;
import de.bsvrz.sys.startstopp.console.ui.editor.MakroTable;
import de.bsvrz.sys.startstopp.console.ui.editor.RechnerEditor;
import de.bsvrz.sys.startstopp.console.ui.editor.RechnerTable;
import de.bsvrz.sys.startstopp.console.ui.editor.SkriptEditor;
import de.bsvrz.sys.startstopp.console.ui.editor.StartArtEditor;
import de.bsvrz.sys.startstopp.console.ui.editor.StartBedingungEditor;
import de.bsvrz.sys.startstopp.console.ui.editor.StartFehlerVerhaltenEditor;
import de.bsvrz.sys.startstopp.console.ui.editor.StoppBedingungEditor;
import de.bsvrz.sys.startstopp.console.ui.editor.StoppFehlerVerhaltenEditor;
import de.bsvrz.sys.startstopp.console.ui.editor.UsvEditor;
import de.bsvrz.sys.startstopp.console.ui.editor.ZugangDavEditor;
import de.bsvrz.sys.startstopp.console.ui.online.ApplikationDetailAction;
import de.bsvrz.sys.startstopp.console.ui.online.ApplikationRestartAction;
import de.bsvrz.sys.startstopp.console.ui.online.ApplikationStartAction;
import de.bsvrz.sys.startstopp.console.ui.online.ApplikationStoppAction;
import de.bsvrz.sys.startstopp.console.ui.online.StartStoppExitAction;
import de.bsvrz.sys.startstopp.console.ui.online.StartStoppRestartAction;
import de.bsvrz.sys.startstopp.console.ui.online.StartStoppStoppAction;

public interface GuiComponentFactory {

	SkriptEditor createSkriptEditor(StartStoppSkript skript);

	EditorVersionierenAction createVersionierenAction(StartStoppSkript skript);

	EditorCloseAction createEditorCloseAction(Window window);

	ApplikationStartAction createApplikationStartAction(Applikation applikation);

	ApplikationRestartAction createApplikationRestartAction(Applikation applikation);

	ApplikationStoppAction createApplikationStoppAction(Applikation applikation);

	ApplikationDetailAction createApplikationDetailAction(Applikation applikation);

	StartStoppStoppAction createStartStoppStoppAction();

	StartStoppRestartAction createStartStoppRestartAction();

	StartStoppExitAction createStartStoppExitAction();

	TerminalCloseAction createTerminalCloseAction();

	InfoDialog createInfoDialog(@Assisted("title") String title, @Assisted("message") String message);

	JaNeinDialog createJaNeinDialog(@Assisted("title") String title, @Assisted("message") String message);

	InkarnationTable createInkarnationTable(StartStoppSkript skript);

	InkarnationEditor createInkarnationEditor(StartStoppSkript skript, Inkarnation inkarnation);

	AufrufParameterEditor createAufrufParameterEditor(StartStoppSkript skript, Inkarnation inkarnation);

	KernsystemEditor createKernsystemEditor(StartStoppSkript skript);

	MakroEditor createMakroEditor(StartStoppSkript skript, MakroDefinition makroDefinition);

	MakroTable createMakroTable(StartStoppSkript skript);

	RechnerEditor createRechnerEditor(StartStoppSkript skript, Rechner rechner);

	RechnerTable createRechnerTable(StartStoppSkript skript);

	StartBedingungEditor createStartBedingungEditor(StartStoppSkript skript, StartBedingung startBedingung);

	StartFehlerVerhaltenEditor createStartFehlerVerhaltenEditor(StartStoppSkript skript,
			StartFehlerVerhalten startFehlerVerhalten);

	StoppBedingungEditor createStoppBedingungEditor(StartStoppSkript skript, StoppBedingung stoppBedingung);

	StoppFehlerVerhaltenEditor createStoppFehlerVerhaltenEditor(StartStoppSkript skript,
			StoppFehlerVerhalten stoppFehlerVerhalten);

	UsvEditor createUsvEditor(StartStoppSkript skript);

	ZugangDavEditor createZugangDavEditor(StartStoppSkript skript);

	StartArtEditor createStartArtEditor(StartStoppSkript skript, StartArt startArt);

	InkarnationSelektor createInkarnationSelektor(StartStoppSkript skript);

	MakroTextInputDialog createMakroTextInputDialog(StartStoppSkript skript, @Assisted("title") String title,
			@Assisted("description") String description, @Assisted("content") String content);

	EditorSichernAction createSichernAction(StartStoppSkript skript);
}
