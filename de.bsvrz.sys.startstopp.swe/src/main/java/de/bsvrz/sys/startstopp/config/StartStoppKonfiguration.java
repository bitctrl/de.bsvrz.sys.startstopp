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

package de.bsvrz.sys.startstopp.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.MakroDefinition;
import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StartFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkriptStatus;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkriptStatus.Status;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.ZugangDav;
import de.bsvrz.sys.startstopp.process.StartStoppInkarnation;

public class StartStoppKonfiguration {

	private StartStoppSkript skript;
	private StartStoppSkriptStatus skriptStatus = new StartStoppSkriptStatus();
	private String checkSumme = "";

	StartStoppKonfiguration(StartStoppSkript skript) {
		this.skript = skript;
		skriptStatus.getMessages().addAll(pruefeVollstaendigkeit());
		skriptStatus.getMessages().addAll(pruefeZirkularitaet());

		if (skriptStatus.getMessages().isEmpty()) {
			skriptStatus.setStatus(StartStoppSkriptStatus.Status.INITIALIZED);
		} else {
			skriptStatus.setStatus(StartStoppSkriptStatus.Status.FAILURE);
		}
	}

	private Collection<String> pruefeZirkularitaet() {

		Collection<String> result = new ArrayList<>();

		try {
			getResolvedMakros();
		} catch (StartStoppException e) {
			result.add(e.getLocalizedMessage());
		}

		for (Inkarnation inkarnation : skript.getInkarnationen()) {
			try {
				checkStartRules(inkarnation);
			} catch (StartStoppException e) {
				result.add(e.getLocalizedMessage());
			}
			try {
				checkStopRules(inkarnation);
			} catch (StartStoppException e) {
				result.add(e.getLocalizedMessage());
			}
		}
		return result;
	}

	private StartStoppInkarnation getInkarnation(String name) throws StartStoppException {
		for (Inkarnation inkarnation : skript.getInkarnationen()) {
			if (name.equals(inkarnation.getInkarnationsName())) {
				return new StartStoppInkarnation(this, inkarnation);
			}
		}
		throw new StartStoppException("Ein referenziertes Skript mit dem Name \"" + name + "\" ist nicht definiert");
	}

	private void checkStartRules(Inkarnation inkarnation) throws StartStoppException {
		Set<String> usedInkarnations = new LinkedHashSet<>();
		usedInkarnations.add(inkarnation.getInkarnationsName());

		Inkarnation currentInkarnation = inkarnation;

		while (currentInkarnation != null) {
			StartBedingung startBedingung = currentInkarnation.getStartBedingung();
			if (startBedingung == null) {
				return;
			}
			String rechnerName = startBedingung.getRechner();
			if (rechnerName != null && !rechnerName.trim().isEmpty()) {
				checkRechner(currentInkarnation, rechnerName.trim());
				return;
			}
			currentInkarnation = getInkarnation(startBedingung.getVorgaenger());
			if (!usedInkarnations.add(currentInkarnation.getInkarnationsName())) {
				throw new StartStoppException(
						"Regeln für \"" + inkarnation.getInkarnationsName() + "\" sind rekursiv!");
			}
		}
	}

	private void checkRechner(Inkarnation inkarnation, String name) throws StartStoppException {
		for( Rechner rechner : getResolvedRechner()) {
			if (rechner.getName().equals(name)) {
				return;
			}
		}
		
		throw new StartStoppException("Der in der Inkarnation \"" + inkarnation.getInkarnationsName() + "\" referenzierte Rechner \"" + name + "\" ist nicht in der Konfiguration definiert");
	}

	private void checkStopRules(Inkarnation inkarnation) throws StartStoppException {
		Set<String> usedInkarnations = new LinkedHashSet<>();
		usedInkarnations.add(inkarnation.getInkarnationsName());

		Inkarnation currentInkarnation = inkarnation;

		while (currentInkarnation != null) {
			StoppBedingung stoppBedingung = currentInkarnation.getStoppBedingung();
			if (stoppBedingung == null) {
				return;
			}
			String rechner = stoppBedingung.getRechner();
			if (rechner != null && !rechner.trim().isEmpty()) {
				checkRechner(currentInkarnation, rechner.trim());
				return;
			}
			currentInkarnation = getInkarnation(stoppBedingung.getNachfolger());
			if (usedInkarnations.add(currentInkarnation.getInkarnationsName())) {
				throw new StartStoppException(
						"Regeln für \"" + inkarnation.getInkarnationsName() + "\" sind rekursiv!");
			}
		}
	}

	private Collection<String> pruefeVollstaendigkeit() {

		// TODO Prüfung der Vollständigkeit implementieren

		Collection<String> result = new ArrayList<>();
		// result.add("Vollständigkeitsprüfung noch nicht implementiert");
		return result;
	}

	public StartStoppSkript getSkript() {
		return skript;
	}

	public StartStoppSkriptStatus getSkriptStatus() {
		return skriptStatus;
	}


	public Collection<StartStoppInkarnation> getInkarnationen() throws StartStoppException {
		if (skriptStatus.getStatus() != StartStoppSkriptStatus.Status.INITIALIZED) {
			throw new StartStoppException("Das geladene StartStoppSkript ist nicht korrekt versioniert!");
		}

		Collection<StartStoppInkarnation> result = new ArrayList<>();
		for (Inkarnation inkarnation : skript.getInkarnationen()) {
			StartStoppInkarnation startStoppInkarnation = new StartStoppInkarnation(this, inkarnation);
			result.add(startStoppInkarnation);
		}
		return result;
	}

	public Map<String, String> getResolvedMakros() throws StartStoppException {

		Pattern pattern = Pattern.compile("%.*?%");
		Map<String, String> result = new LinkedHashMap<>();

		for (MakroDefinition makroDefinition : skript.getGlobal().getMakrodefinitionen()) {
			String name = makroDefinition.getName();
			String wert = makroDefinition.getWert();
			Set<String> usedMakros = new LinkedHashSet<>();

			do {
				boolean replaced = false;
				Matcher matcher = pattern.matcher(wert);
				while (matcher.find()) {
					String part = matcher.group();
					String key = part.substring(1, part.length() - 1);
					if (!usedMakros.add(key)) {
						throw new StartStoppException("Makros können wegen einer Rekursion nicht aufgelöst werden!");
					}
					String replacement = getMakroValueFor(key);
					wert = wert.replaceAll(part, replacement);
					replaced = true;
				}
				if (!replaced) {
					result.put(name, wert);
					break;
				}
			} while (true);
		}

		return result;
	}

	private String getMakroValueFor(String key) throws StartStoppException {
		for (MakroDefinition makroDefinition : skript.getGlobal().getMakrodefinitionen()) {
			if (key.equals(makroDefinition.getName())) {
				return makroDefinition.getWert();
			}
		}

		throw new StartStoppException("Für das Makro \"" + key + "\" wurde kein Wert definiert");
	}

	public Collection<Rechner> getResolvedRechner() throws StartStoppException {

		Map<String, String> resolvedMakros = getResolvedMakros();
		Pattern pattern = Pattern.compile("%.*?%");
		Collection<Rechner> result = new ArrayList<>();

		for (Rechner rechner : getSkript().getGlobal().getRechner()) {
			Rechner resolvedRechner = new Rechner();
			resolvedRechner.setName(rechner.getName());

			String adresse = rechner.getTcpAdresse();
			Matcher matcher = pattern.matcher(adresse);
			while (matcher.find()) {
				String part = matcher.group();
				String key = part.substring(1, part.length() - 1);
				String replacement = resolvedMakros.get(key);
				if (replacement == null) {
					throw new StartStoppException("Das Makro " + key + "ist nicht definiert!");
				}
				adresse = adresse.replaceAll(part, replacement);
			}
			resolvedRechner.setTcpAdresse(adresse);

			String port = rechner.getPort();
			matcher = pattern.matcher(port);
			while (matcher.find()) {
				String part = matcher.group();
				String key = part.substring(1, part.length() - 1);
				String replacement = resolvedMakros.get(key);
				if (replacement == null) {
					throw new StartStoppException("Das Makro " + key + "ist nicht definiert!");
				}
				port = port.replaceAll(part, replacement);
			}
			resolvedRechner.setPort(port);

			result.add(resolvedRechner);
		}
		return result;
	}

	public Rechner getResolvedRechner(String rechnerName) throws StartStoppException {

		for (Rechner rechner : getResolvedRechner()) {
			if (rechner.getName().equals(rechnerName)) {
				return rechner;
			}
		}

		throw new StartStoppException("Ein Rechner mit dem Name \"" + rechnerName + "\" ist nicht definiert!");
	}

	public String makroResolvedString(String wert) throws StartStoppException {
		
		if( wert == null) {
			return null;
		}
		
		Map<String, String> resolvedMakros = getResolvedMakros();
		Pattern pattern = Pattern.compile("%.*?%");
		Matcher matcher = pattern.matcher(wert);
		while (matcher.find()) {
			String part = matcher.group();
			String key = part.substring(1, part.length() - 1);
			String replacement = resolvedMakros.get(key);
			if (replacement == null) {
				throw new StartStoppException("Das Makro " + key + "ist nicht definiert!");
			}
			wert = wert.replaceAll(part, replacement);
		}

		return wert;
	}

	public ZugangDav getResolvedZugangDav() throws StartStoppException {
		ZugangDav zugangDav = getSkript().getGlobal().getZugangDav();
		ZugangDav result = new ZugangDav();
		result.setAdresse(makroResolvedString(zugangDav.getAdresse()));
		result.setPassWord(makroResolvedString(zugangDav.getPassWord()));
		result.setPort(makroResolvedString(zugangDav.getPort()));
		result.setUserName(makroResolvedString(zugangDav.getUserName()));
		return result;
	}

	public StartBedingung getResolvedStartBedingung(StartBedingung startBedingung) throws StartStoppException {

		if( startBedingung == null) {
			return null;
		}
		
		StartBedingung bedingung = new StartBedingung();
		bedingung.setVorgaenger(startBedingung.getVorgaenger());
		bedingung.setWarteart(startBedingung.getWarteart());
		bedingung.setRechner(startBedingung.getRechner());
		bedingung.setWartezeit(makroResolvedString(startBedingung.getWartezeit()));
		return bedingung;
	}

	public StartFehlerVerhalten getResolvedStartFehlerVerhalten(StartFehlerVerhalten startFehlerVerhalten) {

		if( startFehlerVerhalten == null) {
			return null;
		}
		
		StartFehlerVerhalten verhalten = new StartFehlerVerhalten();
		verhalten.setOption(startFehlerVerhalten.getOption());
		verhalten.setWiederholungen(startFehlerVerhalten.getWiederholungen());
		return verhalten;
	}

	public StoppBedingung getResolvedStoppBedingung(StoppBedingung stoppBedingung) throws StartStoppException {

		if( stoppBedingung == null) {
			return null;
		}
		
		StoppBedingung bedingung = new StoppBedingung();
		bedingung.setNachfolger(stoppBedingung.getNachfolger());
		bedingung.setRechner(stoppBedingung.getRechner());
		bedingung.setWartezeit(makroResolvedString(stoppBedingung.getWartezeit()));
		return bedingung;
	}

	public StoppFehlerVerhalten getResolvedStoppFehlerVerhalten(StoppFehlerVerhalten stoppFehlerVerhalten) {

		if( stoppFehlerVerhalten == null) {
			return null;
		}
		
		StoppFehlerVerhalten verhalten = new StoppFehlerVerhalten();
		verhalten.setOption(stoppFehlerVerhalten.getOption());
		verhalten.setWiederholungen(stoppFehlerVerhalten.getWiederholungen());
		return verhalten;
	}

	public void setSkriptStatus(Status status, String message) {
		skriptStatus.setStatus(status);
		skriptStatus.getMessages().add(message);
	}

	public String getCheckSumme() {
		return checkSumme;
	}

	public void setCheckSumme(String checkSumme) {
		this.checkSumme = checkSumme;
	}
}
