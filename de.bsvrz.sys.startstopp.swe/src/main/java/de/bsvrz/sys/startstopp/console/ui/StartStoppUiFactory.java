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

import javax.inject.Inject;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialog;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;

import de.bsvrz.sys.startstopp.console.ui.editor.SkriptEditor;

@Singleton
public class StartStoppUiFactory {
	
	@Inject
	Injector injector;

	public ActionListDialog createApplikationsMenue(String inkarnation) {
		ActionListDialogBuilder builder = new ActionListDialogBuilder().setTitle("Applikation");

		Class<?>[] actionClasses = {ApplikationStartAction.class, ApplikationRestartAction.class,
				ApplikationStoppAction.class, ApplikationDetailAction.class};
		
		
		for( Class<?> actionClass : actionClasses) {
			ApplikationAction action = (ApplikationAction) injector.getInstance(actionClass);
			action.setInkarnation(inkarnation);
			builder.addAction(action);
		}
		
		return builder.build();
	}

	public ActionListDialog createSystemMenue() {
		ActionListDialogBuilder builder = new ActionListDialogBuilder().setTitle("System");

		Class<?>[] actionClasses = {StartStoppStoppAction.class, StartStoppRestartAction.class,
				StartStoppExitAction.class, TerminalCloseAction.class};

		for( Class<?> actionClass : actionClasses) {
			Runnable action = (Runnable) injector.getInstance(actionClass);
			builder.addAction(action);
		}
		
		return builder.build();
	}

	public SkriptEditor getSkriptEditor() {
		return injector.getInstance(SkriptEditor.class);
	}
}
