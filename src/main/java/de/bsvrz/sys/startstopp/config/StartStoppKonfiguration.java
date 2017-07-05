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
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkriptStatus;
import de.bsvrz.sys.startstopp.process.StartStoppInkarnation;
import de.bsvrz.sys.startstopp.process.StartStoppApplikation;

public class StartStoppKonfiguration {

	private StartStoppSkript skript;
	private StartStoppSkriptStatus skriptStatus = new StartStoppSkriptStatus();
	
	public StartStoppKonfiguration(StartStoppSkript skript) {
		this.skript = skript;
		skriptStatus.getMessages().addAll(pruefeVollstaendigkeit());
		skriptStatus.getMessages().addAll(pruefeZirkularitaet());

		if( skriptStatus.getMessages().isEmpty()) {
			skriptStatus.setStatus(StartStoppSkriptStatus.Status.INITIALIZED);
		} else {
			skriptStatus.setStatus(StartStoppSkriptStatus.Status.FAILURE);
		}
	}

	private Collection<String> pruefeZirkularitaet() {
		
		// TODO Prüfung der Zirkularität implementieren
		
		Collection<String> result = new ArrayList<>();
//		result.add("Zirkularitätsprüfung noch nicht implementiert");
		return result;
	}

	private Collection<String> pruefeVollstaendigkeit() {

		// TODO Prüfung der Vollständigkeit implementieren
		
		Collection<String> result = new ArrayList<>();
//		result.add("Vollständigkeitsprüfung noch nicht implementiert");
		return result;
	}

	public StartStoppSkript getSkript() {
		return skript;
	}

	public StartStoppSkriptStatus getSkriptStatus() {
		return skriptStatus;
	}

	public void versionieren(String reason) throws StartStoppException {
		// TODO Auto-generated method stub
		
	}

	public Collection<StartStoppApplikation> getApplikationen() throws StartStoppException {
		if( skriptStatus.getStatus() != StartStoppSkriptStatus.Status.INITIALIZED) {
			throw new StartStoppException("Das geladene StartStoppSkript ist nicht korrekt versioniert!");
		}
		
		Collection<StartStoppApplikation> result = new ArrayList<>();
		for( Inkarnation inkarnation : skript.getInkarnationen()) {
			StartStoppApplikation applikation = new StartStoppApplikation(new StartStoppInkarnation(this, inkarnation));
			result.add(applikation);
		}
		return result;
	}

	public Map<String, String> getResolvedMakros() {
		
		Pattern pattern = Pattern.compile("%.*?%");
		Map<String, String> result = new LinkedHashMap<>();

		for (MakroDefinition makroDefinition : skript.getGlobal().getMakrodefinitionen()) {
			String name = makroDefinition.getName();
			String wert = makroDefinition.getWert();
			Set<String> usedMakros = new LinkedHashSet<>();
			
			do {
				boolean replaced = false;
				Matcher matcher = pattern.matcher(wert);
//				if( matcher.groupCount() == 0 ) {
//					result.put(name, wert);
//					break;
//				}
				while (matcher.find()) {
					String part = matcher.group();
					String key = part.substring(1, part .length() - 1);
					usedMakros.add(key);
					// TODO Zirkularität pruefen
					String replacement = getMakroValueFor(key);
					wert = wert.replaceAll(part, replacement);
					replaced = true;
				}
				if( !replaced) {
					result.put(name, wert);
					break;
				}
			} while(true);
		}
		
		return result;
	}

	private String getMakroValueFor(String key) {
		for (MakroDefinition makroDefinition : skript.getGlobal().getMakrodefinitionen()) {
			if( key.equals(makroDefinition.getName())) {
				return makroDefinition.getWert();
			}
		}
		
		// TODO als Fehler behandeln
		return "";
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
		
		for( Rechner rechner : getResolvedRechner()) {
			if( rechner.getName().equals(rechnerName)) {
				return rechner;
			}
		}
		
		throw new StartStoppException("Ein Rechner mit dem Name \"" + rechnerName + "\" ist nicht definiert!");
	}
	
	public String makroResolvedString(String wert) throws StartStoppException {
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

	
}
