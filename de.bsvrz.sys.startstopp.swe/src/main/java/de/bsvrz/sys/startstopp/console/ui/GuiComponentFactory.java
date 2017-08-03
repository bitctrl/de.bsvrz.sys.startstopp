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
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.console.ui.editor.EditorCloseAction;
import de.bsvrz.sys.startstopp.console.ui.editor.EditorSaveAction;
import de.bsvrz.sys.startstopp.console.ui.editor.InkarnationTable;
import de.bsvrz.sys.startstopp.console.ui.editor.SkriptEditor;
import de.bsvrz.sys.startstopp.console.ui.online.ApplikationDetailAction;
import de.bsvrz.sys.startstopp.console.ui.online.ApplikationRestartAction;
import de.bsvrz.sys.startstopp.console.ui.online.ApplikationStartAction;
import de.bsvrz.sys.startstopp.console.ui.online.ApplikationStoppAction;
import de.bsvrz.sys.startstopp.console.ui.online.StartStoppExitAction;
import de.bsvrz.sys.startstopp.console.ui.online.StartStoppRestartAction;
import de.bsvrz.sys.startstopp.console.ui.online.StartStoppStoppAction;

public interface GuiComponentFactory {

	SkriptEditor createSkriptEditor(StartStoppSkript skript);

	EditorSaveAction createSaveAction(StartStoppSkript skript);

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
}
