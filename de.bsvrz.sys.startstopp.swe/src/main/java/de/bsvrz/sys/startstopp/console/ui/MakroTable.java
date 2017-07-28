package de.bsvrz.sys.startstopp.console.ui;

import com.googlecode.lanterna.gui2.table.Table;

import de.bsvrz.sys.startstopp.api.jsonschema.MakroDefinition;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;

public class MakroTable extends Table<Object> {

	private StartStoppSkript skript;

	public MakroTable(StartStoppSkript skript) {
		super("Name", "Wert");

		setCellSelection(true);
		
		this.skript = skript;
		for (MakroDefinition makroDefinition : skript.getGlobal().getMakrodefinitionen()) {
			getTableModel().addRow(makroDefinition.getName(), makroDefinition.getWert());
		}
	}
	
	public MakroDefinition getSelectedDefintion() {
		int row = getSelectedRow();
		if(( row < 0 ) || (row >= skript.getGlobal().getMakrodefinitionen().size())) {
			return null;
		}
		
		return skript.getGlobal().getMakrodefinitionen().get(row);
	}
}
