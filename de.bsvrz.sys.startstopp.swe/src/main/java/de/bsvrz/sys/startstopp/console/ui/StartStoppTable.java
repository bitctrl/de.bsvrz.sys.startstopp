package de.bsvrz.sys.startstopp.console.ui;

import java.util.List;

import com.googlecode.lanterna.gui2.table.Table;

public abstract class StartStoppTable<T> extends Table<String> {

	private List<T> elements;

	public StartStoppTable(List<T> elements, String... header) {
		super(header);
		this.elements = elements;
		for (T element : elements) {
			getTableModel().addRow(getStringsFor(element));
		}
	}

	protected void addElement(T element) {
		elements.add(element);
		getTableModel().addRow(getStringsFor(element));
	}

	protected void addElement(int row, T element) {
		elements.add(row, element);
		getTableModel().insertRow(row, getStringsFor(element));
	}

	protected void removeCurrentElement() {
		removeElementAt(getSelectedRow());
	}
	
	protected void removeElementAt(int row) {
		elements.remove(row);
		getTableModel().removeRow(row);
	}

	protected T getSelectedElement() {
		return elements.get(getSelectedRow());
	}

	protected void replaceCurrentElementWith(T element) {
		int selectedRow = getSelectedRow();
		elements.remove(selectedRow);
		elements.add(selectedRow, element);
		List<String> values = getStringsFor(element);
		for (int idx = 0; idx < getTableModel().getColumnCount(); idx++) {
			getTableModel().setCell(idx, selectedRow, values.get(idx));
		}
	}

	protected abstract List<String> getStringsFor(T element);
}
