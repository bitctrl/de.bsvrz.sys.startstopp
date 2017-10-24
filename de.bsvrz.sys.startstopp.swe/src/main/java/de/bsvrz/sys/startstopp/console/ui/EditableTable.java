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

package de.bsvrz.sys.startstopp.console.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyStroke;

import de.bsvrz.sys.startstopp.console.ui.editor.SkriptEditor;

public abstract class EditableTable<T> extends Table<T> {

	private List<T> dataList;
	private boolean editierbar = true;

	public EditableTable(List<T> dataList, String... columnName) {
		super(columnName);
		setTableCellRenderer(new EditableTableCellRenderer<T>());
		this.dataList = dataList;
		for (T element : dataList) {
			getTableModel().addRow(getElementArray(element));
		}
		setSelectAction(() -> editSelectedElement());
	}

	private Collection<T> getElementArray(T element) {
		List<T> list = new ArrayList<>();
		for (int idx = 0; idx < getTableModel().getColumnCount(); idx++) {
			list.add(element);
		}
		return list;
	}

	protected void addElement(T element) {
		int row = dataList.size();
		dataList.add(row, element);
		getTableModel().insertRow(row, getElementArray(element));
	}

	protected void addElement(int row, T element) {
		dataList.add(row, element);
		getTableModel().insertRow(row, getElementArray(element));
	}

	protected T removeCurrentElement() {
		return removeElementAt(getSelectedRow());
	}

	protected T removeElementAt(int row) {
		getTableModel().removeRow(row);
		return dataList.remove(row);
	}

	protected void clearTable() {
		dataList.clear();
		while (getTableModel().getRowCount() > 0) {
			getTableModel().removeRow(0);
		}
	}

	@Override
	public Result handleKeyStroke(KeyStroke key) {

		if (editierbar) {
			int selectedRow = getSelectedRow();

			if (SkriptEditor.isDeleteKey(key)) {
				deleteElementAt(selectedRow);
				return Result.HANDLED;
			} else if (SkriptEditor.isInsertAfterKey(key)) {
				insertElementAfter(selectedRow);
				return Result.HANDLED;
			} else if (SkriptEditor.isInsertBeforeKey(key)) {
				insertElementBefore(selectedRow);
				return Result.HANDLED;
			} else if (SkriptEditor.isEintragNachObenKey(key)) {
				moveElementUp(selectedRow);
				return Result.HANDLED;
			} else if (SkriptEditor.isEintragNachUntenKey(key)) {
				moveElementDown(selectedRow);
				return Result.HANDLED;
			}
		}

		return super.handleKeyStroke(key);
	}

	void deleteElementAt(int selectedRow) {
		if ((selectedRow >= 0) && (selectedRow < dataList.size())) {
			if (checkDelete(dataList.get(selectedRow))) {
				removeElementAt(selectedRow);
				setSelectedRow(Math.max(0, selectedRow - 1));
			}
		}
	}

	void insertElementAfter(int selectedRow) {
		T newParameter = requestNewElement();
		if (newParameter != null) {
			addElement(selectedRow, newParameter);
			setSelectedRow(selectedRow + 1);
		}
	}

	void insertElementBefore(int selectedRow) {
		T newParameter = requestNewElement();
		if (newParameter != null) {
			addElement(selectedRow, newParameter);
		}
	}

	void moveElementUp(int selectedRow) {
		if (selectedRow > 0) {
			T parameter = removeElementAt(selectedRow);
			addElement(selectedRow - 1, parameter);
			setSelectedRow(selectedRow - 1);
		}
	}

	private void moveElementDown(int selectedRow) {
		if (selectedRow < dataList.size() - 1) {
			T parameter = removeElementAt(selectedRow);
			addElement(selectedRow + 1, parameter);
			setSelectedRow(selectedRow + 1);
		}
	}

	protected void replaceCurrentElementWith(T element) {
		int selectedRow = getSelectedRow();
		removeCurrentElement();
		addElement(selectedRow, element);
	}

	private void editSelectedElement() {
		T oldElement = getSelectedElement();
		if( oldElement == null) {
			return;
		}
		T newParameter = editElement(oldElement);
		if (newParameter != null) {
			int row = getSelectedRow();
			removeElementAt(row);
			addElement(row, newParameter);
		}
	}

	protected T getSelectedElement() {
		int selectedRow = getSelectedRow();
		if( selectedRow >= dataList.size()) {
			return null;
		}
		return dataList.get(selectedRow);
	}

	@Override
	public WindowBasedTextGUI getTextGUI() {
		return (WindowBasedTextGUI) super.getTextGUI();
	}

	protected abstract T requestNewElement();

	protected abstract T editElement(T oldElement);

	protected abstract boolean checkDelete(T element);

	protected abstract List<String> getStringsFor(T element);

	public final String getStringForColumn(int columnIndex, T element) {
		List<String> strings = getStringsFor(element);
		if (columnIndex < 0 || columnIndex >= strings.size()) {
			return "?";
		}
		return strings.get(columnIndex);
	}

	public void setEditierbar(boolean editierbar) {
		this.editierbar = editierbar;
	}
}
