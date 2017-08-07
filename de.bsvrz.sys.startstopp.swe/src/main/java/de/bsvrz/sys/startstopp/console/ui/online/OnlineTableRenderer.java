package de.bsvrz.sys.startstopp.console.ui.online;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.graphics.ThemeStyle;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.gui2.table.DefaultTableCellRenderer;
import com.googlecode.lanterna.gui2.table.Table;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation.Status;

public class OnlineTableRenderer extends DefaultTableCellRenderer<Object> {

	@Override
	public void drawCell(Table<Object> table, Object cell, int columnIndex, int rowIndex,
			TextGUIGraphics textGUIGraphics) {
		Object status = table.getTableModel().getCell(1, rowIndex);
		String statusName = ((Status) status).name();

		ThemeDefinition themeDefinition = table.getThemeDefinition();
		if ((table.getSelectedColumn() == columnIndex && table.getSelectedRow() == rowIndex)
				|| (table.getSelectedRow() == rowIndex && !table.isCellSelection())) {
			if (table.isFocused()) {
				textGUIGraphics.applyThemeStyle(themeDefinition.getCustom(statusName, themeDefinition.getActive()));
			} else {
				textGUIGraphics.applyThemeStyle(themeDefinition.getCustom(statusName, themeDefinition.getSelected()));
			}
			textGUIGraphics.fill(' '); // Make sure to fill the whole cell first
		} else {
			textGUIGraphics.applyThemeStyle(themeDefinition.getCustom(statusName, themeDefinition.getNormal()));
			textGUIGraphics.fill(' '); // Make sure to fill the whole cell first
		}

		String[] lines = getContent(cell);
		int rowCount = 0;
		for (String line : lines) {
			textGUIGraphics.putString(0, rowCount++, line);
		}
	}

	private String[] getContent(Object cell) {
		String[] lines;
		if (cell == null) {
			lines = new String[] { "" };
		} else {
			lines = cell.toString().split("\r?\n");
		}
		return lines;
	}
}
