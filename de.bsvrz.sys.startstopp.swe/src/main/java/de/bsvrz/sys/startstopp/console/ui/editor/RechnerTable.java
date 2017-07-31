package de.bsvrz.sys.startstopp.console.ui.editor;

import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;

public class RechnerTable extends Table<String> {

	private StartStoppSkript skript;
	private WindowBasedTextGUI gui;

	public RechnerTable(WindowBasedTextGUI gui, StartStoppSkript skript) {
		super("Name", "Host", "Port");
		this.skript = skript;
		this.gui = gui;

		setSelectAction(new Runnable() {
			@Override
			public void run() {
				int row = getSelectedRow();
				Rechner rechner = skript.getGlobal().getRechner().get(row);
				RechnerEditor editor = new RechnerEditor(skript, rechner);
				Rechner result = editor.showDialog(gui);
				if (result != null) {
					rechner.setName(result.getName());
					getTableModel().setCell(0, row, rechner.getName());
					rechner.setTcpAdresse(result.getTcpAdresse());
					getTableModel().setCell(1, row, rechner.getTcpAdresse());
					rechner.setPort(result.getPort());
					getTableModel().setCell(2, row, rechner.getPort());
				}
			}
		});

		this.skript = skript;
		for (Rechner rechner : skript.getGlobal().getRechner()) {
			getTableModel().addRow(rechner.getName(), rechner.getTcpAdresse(), rechner.getPort());
		}
	}

	@Override
	public Result handleKeyStroke(KeyStroke keyStroke) {
		// TODO Auto-generated method stub
		System.err.println("RechnerTable-Key: " + keyStroke);

		if (keyStroke.getKeyType() == KeyType.Character) {
			switch (keyStroke.getCharacter()) {
			case '+':
				int row = getSelectedRow() + 1;
				Rechner rechner = new Rechner();
				rechner.setName("Neuer Rechner");
				rechner.setTcpAdresse("");
				rechner.setPort("");
				RechnerEditor editor = new RechnerEditor(skript, rechner);
				Rechner newRechner = editor.showDialog(gui);
				if (newRechner != null) {
					skript.getGlobal().getRechner().add(row, newRechner);
				}
				break;
			case '-':
				MessageDialogBuilder builder = new MessageDialogBuilder();
				builder.addButton(MessageDialogButton.Yes);
				builder.addButton(MessageDialogButton.No);
				builder.setTitle("Rechner löschen");
				builder.setText("Soll der ausgewählte Rechner wirklich gelöscht werden?");
				MessageDialogButton result = builder.build().showDialog(gui);
				if (result.equals(MessageDialogButton.Yes)) {
					int deleteRow = getSelectedRow();
					skript.getGlobal().getRechner().remove(deleteRow);
					getTableModel().removeRow(deleteRow);
				}
				break;
			default:
			}
		}

		return super.handleKeyStroke(keyStroke);
	}
}
