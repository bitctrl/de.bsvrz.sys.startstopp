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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;


public class InkarnationsAenderung {

	enum Typ {
		APP, ARGUMENTLIST, STARTART, STARTBEDINGUNG, STOPPBEDINGUNG;
	}

	private Set<Typ> aenderungen = new LinkedHashSet<>();

	public InkarnationsAenderung(Inkarnation inkarnation, Inkarnation letzteInkarnation) {
		if (!inkarnation.getApplikation().equals(letzteInkarnation.getApplikation())) {
			aenderungen.add(Typ.APP);
		}

		List<String> letzteArgumente = letzteInkarnation.getAufrufParameter();
		List<String> neueArgumente = inkarnation.getAufrufParameter();
		if(!(letzteArgumente.containsAll(neueArgumente) && neueArgumente.containsAll(letzteArgumente))) {
			aenderungen.add(Typ.APP);
		}
		
		if( !Objects.deepEquals(inkarnation.getStartArt(), letzteInkarnation.getStartArt())) {
			aenderungen.add(Typ.STARTART);
		}

		if( !Objects.deepEquals(inkarnation.getStartBedingung(), letzteInkarnation.getStartBedingung())) {
			aenderungen.add(Typ.STARTBEDINGUNG);
		}

		if( !Objects.deepEquals(inkarnation.getStoppBedingung(), letzteInkarnation.getStoppBedingung())) {
			aenderungen.add(Typ.STOPPBEDINGUNG);
		}
		
	}

	public Set<Typ> getAenderungen() {
		return aenderungen;
	}
}
