package de.bsvrz.sys.startstopp.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public interface SkriptManagerListener extends PropertyChangeListener {

	public static final String PROP_CURRENT_SKRIPT = SkriptManagerListener.class.getSimpleName() + ".currentSkript";

	@Override
	default void propertyChange(PropertyChangeEvent evt) {
		if (PROP_CURRENT_SKRIPT.equals(evt.getPropertyName())) {
			skriptAktualisiert((ManagedSkript) evt.getOldValue(), (ManagedSkript) evt.getNewValue());
		}
	}

	void skriptAktualisiert(ManagedSkript oldValue, ManagedSkript newValue);
}
