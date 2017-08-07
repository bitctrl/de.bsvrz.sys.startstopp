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

package de.bsvrz.sys.startstopp.console.ui.editor;

import java.util.Collections;
import java.util.List;

import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

public abstract class EditableTable<T> extends Table<String> {

	private List<T> dataList;

	public EditableTable(List<T> dataList, String... columnName) {
		super(columnName);
		this.dataList = dataList;
		for( T element : dataList) {
			getTableModel().addRow(renderElement(element));
		}
		setSelectAction(() -> editSelectedElement());
	}

	@Override
	public Result handleKeyStroke(KeyStroke key) {

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

		return super.handleKeyStroke(key);
	}

	void deleteElementAt(int selectedRow) {
		if ((selectedRow >= 0) && (selectedRow < dataList.size())) {
			dataList.remove(selectedRow);
			getTableModel().removeRow(selectedRow);
			setSelectedRow(Math.max(0, selectedRow - 1));
		}
	}

	void insertElementAfter(int selectedRow) {
		T newParameter = requestNewElement();
		if (newParameter != null) {
			dataList.add(selectedRow, newParameter);
			getTableModel().insertRow(Math.min(selectedRow + 1, getTableModel().getRowCount()), Collections.singleton(renderElement(newParameter)));
			setSelectedRow(selectedRow + 1);
		}
	}


	void insertElementBefore(int selectedRow) {
		T newParameter = requestNewElement();
		if (newParameter != null) {
			dataList.add(selectedRow, newParameter);
			getTableModel().insertRow(selectedRow, Collections.singleton(renderElement(newParameter)));
		}
	}

	void moveElementUp(int selectedRow) {
		if (selectedRow > 0) {
			T parameter = dataList.remove(selectedRow);
			dataList.add(selectedRow - 1, parameter);
			getTableModel().setCell(0, selectedRow - 1, renderElement(dataList.get(selectedRow - 1)));
			getTableModel().setCell(0, selectedRow, renderElement(dataList.get(selectedRow)));
			setSelectedRow(selectedRow - 1);
		}
	}

	private void moveElementDown(int selectedRow) {
		if (selectedRow < dataList.size() - 1) {
			T parameter = dataList.remove(selectedRow);
			dataList.add(selectedRow + 1, parameter);
			getTableModel().setCell(0, selectedRow + 1, renderElement(dataList.get(selectedRow + 1)));
			getTableModel().setCell(0, selectedRow, renderElement(dataList.get(selectedRow)));
			setSelectedRow(selectedRow + 1);
		}
	}

	private void editSelectedElement() {
		T oldElement = dataList.get(getSelectedRow());
		T newParameter = editElement(oldElement);
		if (newParameter != null) {
			int row = getSelectedRow();
			dataList.remove(row);
			dataList.add(row, newParameter);
			getTableModel().setCell(0, row, renderElement(newParameter));
		}
	}

	protected abstract T requestNewElement();

	protected abstract T editElement(T oldElement);

	protected  abstract String renderElement(T element);
	
}
