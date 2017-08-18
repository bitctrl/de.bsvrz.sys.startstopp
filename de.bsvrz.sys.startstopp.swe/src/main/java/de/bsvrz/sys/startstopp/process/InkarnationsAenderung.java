package de.bsvrz.sys.startstopp.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class InkarnationsAenderung {

	enum Typ {
		APP, ARGUMENTLIST, STARTART, STARTBEDINGUNG, STOPPBEDINGUNG;
	}

	private Set<Typ> aenderungen = new LinkedHashSet<>();

	public InkarnationsAenderung(StartStoppInkarnation inkarnation, StartStoppInkarnation letzteInkarnation) {
		if (!inkarnation.getApplikation().equals(letzteInkarnation.getApplikation())) {
			aenderungen.add(Typ.APP);
		}

		List<String> letzteArgumente = new ArrayList<>(letzteInkarnation.getAufrufParameter());
		List<String> neueArgumente = new ArrayList<>(inkarnation.getAufrufParameter());
		Collections.sort(letzteArgumente);
		Collections.sort(neueArgumente);
		if( !Objects.equals(letzteArgumente, neueArgumente)) {
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
