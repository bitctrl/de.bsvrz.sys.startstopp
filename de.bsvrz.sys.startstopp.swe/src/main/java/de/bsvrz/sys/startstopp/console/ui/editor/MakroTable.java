package de.bsvrz.sys.startstopp.console.ui.editor;

import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.table.Table;

import de.bsvrz.sys.startstopp.api.jsonschema.MakroDefinition;
import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;

public class MakroTable extends Table<Object> {

	private StartStoppSkript skript;
	private WindowBasedTextGUI gui;

	public MakroTable(WindowBasedTextGUI gui, StartStoppSkript skript) {
		super("Name", "Wert");

		this.gui = gui;
		this.skript = skript;
		for (MakroDefinition makroDefinition : skript.getGlobal().getMakrodefinitionen()) {
			getTableModel().addRow(makroDefinition.getName(), makroDefinition.getWert());
		}
		
		setSelectAction(new Runnable() {
			@Override
			public void run() {
				int row = getSelectedRow();
				MakroDefinition makroDefinition = skript.getGlobal().getMakrodefinitionen().get(row);
				MakroEditor editor = new MakroEditor(skript, makroDefinition); 
				MakroDefinition result = editor.showDialog(gui);
				if (result != null) {
					makroDefinition.setName(result.getName());
					getTableModel().setCell(0, row, makroDefinition.getName());
					makroDefinition.setWert(result.getWert());
					getTableModel().setCell(1, row, makroDefinition.getWert());
				}
			}
		});
		
	}
	
	public MakroDefinition getSelectedDefintion() {
		int row = getSelectedRow();
		if(( row < 0 ) || (row >= skript.getGlobal().getMakrodefinitionen().size())) {
			return null;
		}
		
		return skript.getGlobal().getMakrodefinitionen().get(row);
	}
}
