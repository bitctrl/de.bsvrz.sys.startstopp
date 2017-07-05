package de.bsvrz.sys.startstopp.console.ui;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.lanterna.gui2.table.Table;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.config.StartStoppException;
import de.bsvrz.sys.startstopp.console.StartStoppConsole;

public class InkarnationTable extends Table<Object> {

	private final class Simulator extends Thread {
		private Simulator() {
			super("StatusUpdater");
			setDaemon(true);
		}

		public void run() {

			while (true) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (int row = 0; row < getTableModel().getRowCount(); row++) {
					Applikation applikation = inkarnations.get(row);
					try {
						applikation = StartStoppConsole.getInstance().getClient().getApplikation(applikation.getInkarnationsName());
					} catch (StartStoppException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					getTableModel().setCell(1, row, applikation.getStatus());
				}
			}
		}
	}

	private List<Applikation> inkarnations = new ArrayList<>();

	public InkarnationTable() throws StartStoppException {
		super("Name", "Status", "Startzeit");

		for (Applikation inkarnation : StartStoppConsole.getInstance().getClient().getApplikationen()) {
			getTableModel().addRow(inkarnation.getInkarnationsName(), inkarnation.getStatus(),
					inkarnation.getLetzteStartzeit());
			inkarnations.add(inkarnation);
		}

		Thread simulator = new Simulator();
		simulator.start();
	}
	
	public Applikation getSelectedOnlineInkarnation() {
		int row = getSelectedRow();
		if(( row < 0 ) || (row >= inkarnations.size())) {
			return null;
		}
		
		return inkarnations.get(row);
	}

}