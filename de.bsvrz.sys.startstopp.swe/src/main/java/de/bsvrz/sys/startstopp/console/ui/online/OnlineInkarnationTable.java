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

package de.bsvrz.sys.startstopp.console.ui.online;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.lanterna.gui2.dialogs.ActionListDialog;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.console.ui.EditableTable;

class OnlineInkarnationTable extends EditableTable<Applikation> {

	OnlineInkarnationTable(List<Applikation> applikationen) {
		super(applikationen, "Inkarnation", "Art", "Status", "Meldung");
		setTableCellRenderer(new OnlineTableCellRenderer());
	}

	public void updateApplikationen(List<Applikation> applikationen) {
		clearTable();
		for(Applikation applikation : applikationen) {
			addElement(applikation);
		}
	}

	@Override
	protected Applikation requestNewElement() {
		return null;
	}

	@Override
	protected Applikation editElement(Applikation applikation) {
		ActionListDialogBuilder builder;
		if (applikation != null) {
			builder = new ActionListDialogBuilder().setTitle("Applikation").setCanCancel(false);
			builder.addAction(new ApplikationStartAction(applikation));
			builder.addAction(new ApplikationRestartAction(applikation));
			builder.addAction(new ApplikationStoppAction(applikation));
			builder.addAction(new ApplikationDetailAction(applikation));
			builder.addAction(new ApplikationLogAction(applikation));
			builder.setDescription("ESC - Abbrechen");
			ActionListDialog dialog = builder.build();
			dialog.setCloseWindowWithEscape(true);
			dialog.showDialog(getTextGUI());
		}
		return null;
	}

	@Override
	protected List<String> getStringsFor(Applikation applikation) {
		List<String> result = new ArrayList<>();
		result.add(applikation.getInkarnation().getInkarnationsName());
		result.add(getStartArtKurzname(applikation));
		result.add(applikation.getStatus().toString());
		result.add(applikation.getStartMeldung());
		return result;
	}

	private String getStartArtKurzname(Applikation applikation) {

		String result;
		
		switch(applikation.getInkarnation().getStartArt().getOption()) {
		case AUTOMATISCH:
			result = "AUT";
			break;
		case INTERVALLABSOLUT:
			result = "ABS";
			break;
		case INTERVALLRELATIV:
			result = "REL";
			break;
		case MANUELL:
			result = "MAN";
			break;
		default:
			result = "???";
			break;
		}

		return result;
	}
}