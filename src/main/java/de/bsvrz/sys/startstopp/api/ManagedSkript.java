package de.bsvrz.sys.startstopp.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnationen;
import de.bsvrz.sys.startstopp.api.jsonschema.Makrodefinitionen;
import de.bsvrz.sys.startstopp.api.jsonschema.Startstoppskript;
import de.bsvrz.sys.startstopp.api.jsonschema.Startstoppskriptstatus;
import de.bsvrz.sys.startstopp.config.StartStoppException;

public class ManagedSkript {

	private Startstoppskript skript;
	private Startstoppskriptstatus skriptStatus = new Startstoppskriptstatus();
	
	public ManagedSkript(Startstoppskript skript) {
		this.skript = skript;
		skriptStatus.getMessages().addAll(pruefeVollstaendigkeit());
		skriptStatus.getMessages().addAll(pruefeZirkularitaet());

		if( skriptStatus.getMessages().isEmpty()) {
			skriptStatus.setStatus(Startstoppskriptstatus.Status.INITIALIZED);
		} else {
			skriptStatus.setStatus(Startstoppskriptstatus.Status.FAILURE);
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

	public Startstoppskript getSkript() {
		return skript;
	}

	public Startstoppskriptstatus getSkriptStatus() {
		return skriptStatus;
	}

	public void versionieren() throws StartStoppException {
		// TODO Auto-generated method stub
		
	}

	public Collection<ManagedApplikation> getApplikationen() throws StartStoppException {
		if( skriptStatus.getStatus() != Startstoppskriptstatus.Status.INITIALIZED) {
			throw new StartStoppException("Das geladene StartStoppSkript ist nicht korrekt versioniert!");
		}
		
		Collection<ManagedApplikation> result = new ArrayList<>();
		for( Inkarnationen inkarnation : skript.getInkarnationen()) {
			ManagedApplikation applikation = new ManagedApplikation(new ManagedInkarnation(this, inkarnation));
			result.add(applikation);
		}
		return result;
	}

	public Map<String, String> getResolvedMakros() {
		
		Pattern pattern = Pattern.compile("%.*?%");
		Map<String, String> result = new LinkedHashMap<>();

		for (Makrodefinitionen makroDefinition : skript.getGlobal().getMakrodefinitionen()) {
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
		for (Makrodefinitionen makroDefinition : skript.getGlobal().getMakrodefinitionen()) {
			if( key.equals(makroDefinition.getName())) {
				return makroDefinition.getWert();
			}
		}
		
		// TODO als Fehler behandeln
		return "";
	}
}
