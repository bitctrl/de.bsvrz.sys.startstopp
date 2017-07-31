package de.bsvrz.sys.startstopp.console.ui.editor;

import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;

import de.bsvrz.sys.startstopp.api.jsonschema.MakroDefinition;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;

public class MakroSelektor {
	
	private MakroDefinition selectedMakroDefinition = null;
	private ActionListDialogBuilder builder;
	private WindowBasedTextGUI gui;
	
	public MakroSelektor(WindowBasedTextGUI gui, StartStoppSkript skript) {
		
		this.gui = gui;
		
		builder = new ActionListDialogBuilder();
		builder.setTitle("Makroauswahl");
		for( MakroDefinition definition : skript.getGlobal().getMakrodefinitionen()) {
			builder.addAction(definition.getName(), new Runnable() {
				@Override
				public void run() {
					selectedMakroDefinition = definition;
				}
			});
		}
		
	}

	public MakroDefinition getSelectedMakroDefinition() {
		builder.build().showDialog(gui);
		return selectedMakroDefinition;
	}
}
