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

package de.bsvrz.sys.startstopp.process;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.KernSystem;
import de.bsvrz.sys.startstopp.config.StartStoppKonfiguration;

public class KonfigurationsVergleicher {

	private Map<String, InkarnationsAenderung> geanderteInkarnationen = new LinkedHashMap<>();
	private List<String> entfernteInkarnationen = new ArrayList<>();
	private boolean kernSystemGeaendert;

	public void vergleiche(StartStoppKonfiguration letzteKonfiguration, StartStoppKonfiguration neueKonfiguration)
			throws StartStoppException {

		Map<String, OnlineInkarnation> letzteInkarnationen = new LinkedHashMap<>();
		for (OnlineInkarnation inkarnation : letzteKonfiguration.getInkarnationen()) {
			letzteInkarnationen.put(inkarnation.getName(), inkarnation);
		}

		kernSystemGeaendert = !letzteKonfiguration.getKernSysteme().equals(neueKonfiguration.getKernSysteme());

		for (OnlineInkarnation inkarnation : neueKonfiguration.getInkarnationen()) {
			OnlineInkarnation letzteInkarnation = letzteInkarnationen.remove(inkarnation.getName());
			if (letzteInkarnation == null) {
				kernSystemGeaendert = kernSystemGeaendert || inkarnation.isKernSystem();
				continue;
			}

			InkarnationsAenderung aenderung = new InkarnationsAenderung(inkarnation.getInkarnation(),
					letzteInkarnation.getInkarnation());
			if (!aenderung.getAenderungen().isEmpty()) {
				geanderteInkarnationen.put(inkarnation.getName(), aenderung);
				kernSystemGeaendert = kernSystemGeaendert || inkarnation.isKernSystem()
						|| letzteInkarnation.isKernSystem();
			}
		}

		for (OnlineInkarnation inkarnation : letzteInkarnationen.values()) {
			entfernteInkarnationen.add(inkarnation.getName());
			kernSystemGeaendert = kernSystemGeaendert || inkarnation.isKernSystem();
		}
	}

	public boolean isKernsystemGeandert() {
		return kernSystemGeaendert;
	}

	public List<String> getEntfernteInkarnationen() {
		return entfernteInkarnationen;
	}

	public Map<String, InkarnationsAenderung> getGeanderteInkarnationen() {
		return geanderteInkarnationen;
	}
}
