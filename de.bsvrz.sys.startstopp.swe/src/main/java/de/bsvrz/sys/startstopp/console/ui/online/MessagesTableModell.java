package de.bsvrz.sys.startstopp.console.ui.online;

import javax.inject.Inject;

import com.googlecode.lanterna.gui2.table.TableModel;

class MessagesTableModell extends TableModel<Object> {

	@Inject
	MessagesTableModell() {
		super("Meldungen");
	}

	public void addMessage(String string) {
		addRow(string);
	}

	public void setMessage(String message) {
		setCell(0, 0, message);
	}
}