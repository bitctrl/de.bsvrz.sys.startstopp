package de.bsvrz.sys.startstopp.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnationen;
import de.bsvrz.sys.startstopp.api.jsonschema.Kernsysteme;
import de.bsvrz.sys.startstopp.config.StartStoppException;

public class ManagedInkarnation {

	private Inkarnationen inkarnation;
	private ManagedSkript skript;

	public ManagedInkarnation(ManagedSkript skript, Inkarnationen inkarnation) {
		this.skript = skript;
		this.inkarnation = inkarnation;
	}

	public Inkarnationen getInkarnation() {
		return inkarnation;
	}

	public List<String> getResolvedParameter() throws StartStoppException {

		List<String> result = new ArrayList<>();

		Map<String, String> resolvedMakros = skript.getResolvedMakros();
		Pattern pattern = Pattern.compile("%.*?%");

		for (String parameter : inkarnation.getAufrufParameter()) {
			String wert = parameter;
			Matcher matcher = pattern.matcher(wert);
			while (matcher.find()) {
				String part = matcher.group();
				String key = part.substring(1, part.length() - 1);
				String replacement = resolvedMakros.get(key);
				if (replacement == null) {
					throw new StartStoppException("Das Makro " + key + " ist nicht definiert");
				}
				wert = wert.replaceAll(part, replacement);
			}
			result.add(wert);
		}

		return result;
	}

	public String getResolvedApplikation() throws StartStoppException {

		Map<String, String> resolvedMakros = skript.getResolvedMakros();
		Pattern pattern = Pattern.compile("%.*?%");

		String wert = inkarnation.getApplikation();
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

	public boolean isKernSystem() {

		for (Kernsysteme kernsystem : skript.getSkript().getGlobal().getKernsysteme()) {
			if (kernsystem.getInkarnationsName().equals(inkarnation.getInkarnationsName())) {
				return true;
			}
		}

		return false;
	}
}
