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
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.googlecode.lanterna.gui2.table.TableModel;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;

class ApplikationenTableModell extends TableModel<Object> {

	@Inject
	ApplikationenTableModell() {
		super("Inkarnation", "Status", "Startzeit");
	}

	private final List<Applikation> applikationen = new ArrayList<>();

	public void setApplikationen(List<Applikation> applikationen) {

		while (getRowCount() > 0) {
			removeRow(0);
		}

		this.applikationen.clear();
		this.applikationen.addAll(applikationen);

		for (Applikation applikation : applikationen) {
			addRow(getValues(applikation));
		}
	}

	public void updateApplikationen(List<Applikation> applikationen) {
		for (int idx = 0; idx < applikationen.size(); idx++) {
			Applikation applikation = applikationen.get(idx);
			if (getRowCount() <= idx) {
				addRow(applikation.getInkarnation().getInkarnationsName(), applikation.getStatus(),
						applikation.getLetzteStartzeit());
			} else if (getCell(0, idx).equals(applikation.getInkarnation().getInkarnationsName())) {
				setCell(1, idx, applikation.getStatus());
				setCell(2, idx, applikation.getLetzteStartzeit());
			} else {
				insertRow(idx, getValues(applikation));
			}
		}

		while (getRowCount() > applikationen.size()) {
			removeRow(getRowCount() - 1);
		}
	}

	private Collection<Object> getValues(Applikation applikation) {
		Collection<Object> result = new ArrayList<>();
		result.add(applikation.getInkarnation().getInkarnationsName());
		result.add(applikation.getStatus());
		result.add(applikation.getLetzteStartzeit());
		return result;
	}

	public Applikation getApplikation(int row) {

		if ((row < 0) || (row >= applikationen.size())) {
			return null;
		}

		return applikationen.get(row);
	}
}