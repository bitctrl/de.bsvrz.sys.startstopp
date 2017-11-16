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

import de.bsvrz.sys.startstopp.console.ui.JaNeinDialog;

class EditorCloseAction implements Runnable {

	private SkriptEditor editor;

	EditorCloseAction(SkriptEditor editor) {
		this.editor = editor;
	}

	@Override
	public void run() {
		if (editor.checkForChanges()) {
			JaNeinDialog dialog = new JaNeinDialog("Editor verlassen",
					"Soll der Editor wirklich verlassen werden?\nEventuell wurden Änderungen noch nicht versioniert!");
			if (!dialog.display()) {
				return;
			}
		} 
		editor.close();
	}

	@Override
	public String toString() {
		return "Verlassen";
	}
}
