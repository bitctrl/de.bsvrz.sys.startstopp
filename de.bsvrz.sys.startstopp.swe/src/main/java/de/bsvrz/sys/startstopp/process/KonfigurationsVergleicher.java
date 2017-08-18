package de.bsvrz.sys.startstopp.process;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.config.StartStoppKonfiguration;

public class KonfigurationsVergleicher {

	private Map<String, InkarnationsAenderung> geanderteInkarnationen = new LinkedHashMap<>();
	private List<String> entfernteInkarnationen = new ArrayList<>();
	private boolean kernSystemGeaendert;

	public void vergleiche(StartStoppKonfiguration letzteKonfiguration, StartStoppKonfiguration neueKonfiguration) throws StartStoppException {

		Map<String, StartStoppInkarnation> letzteInkarnationen = new LinkedHashMap<>();
		for( StartStoppInkarnation inkarnation : letzteKonfiguration.getInkarnationen()) {
			letzteInkarnationen.put(inkarnation.getInkarnationsName(), inkarnation);
		}

		for( StartStoppInkarnation inkarnation : neueKonfiguration.getInkarnationen()) {
			StartStoppInkarnation letzteInkarnation = letzteInkarnationen.remove(inkarnation.getInkarnationsName());
			if( letzteInkarnation == null) {
				kernSystemGeaendert = kernSystemGeaendert || inkarnation.isKernSystem();
				continue;
			}
			
			InkarnationsAenderung aenderung = new InkarnationsAenderung(inkarnation, letzteInkarnation);
			if( !aenderung.getAenderungen().isEmpty()) {
				geanderteInkarnationen.put(inkarnation.getInkarnationsName(), aenderung);
				kernSystemGeaendert = kernSystemGeaendert || inkarnation.isKernSystem() || letzteInkarnation.isKernSystem();
			}
		}

		for( StartStoppInkarnation inkarnation : letzteInkarnationen.values()) {
			entfernteInkarnationen.add(inkarnation.getInkarnationsName());
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
