package de.bsvrz.sys.startstopp.console.ui.online;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.googlecode.lanterna.gui2.table.TableModel;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;

class ApplikationenTableModell extends TableModel<Object> {

	@Inject
	ApplikationenTableModell() {
		super("Inkarnation", "Status", "Startzeit");
	}

	public void setApplikationen(List<Applikation> applikationen) {

		while (getRowCount() > 0) {
			removeRow(0);
		}

		for (Applikation applikation : applikationen) {
			addRow(getValues(applikation));
		}
	}

	public void updateApplikationen(List<Applikation> applikationen) {
		for (int idx = 0; idx < applikationen.size(); idx++) {
			Applikation applikation = applikationen.get(idx);
			if (getRowCount() <= idx) {
				addRow(applikation.getInkarnation().getInkarnationsName(), applikation.getStatus(),
						applikation.getLetzteStartzeit());
			} else if (getCell(0, idx).equals(applikation.getInkarnation().getInkarnationsName())) {
				setCell(1, idx, applikation.getStatus());
				setCell(2, idx, applikation.getLetzteStartzeit());
			} else {
				insertRow(idx, getValues(applikation));
			}
		}

		while (getRowCount() > applikationen.size()) {
			removeRow(getRowCount() - 1);
		}
	}

	private Collection<Object> getValues(Applikation applikation) {
		Collection<Object> result = new ArrayList<>();
		result.add(applikation.getInkarnation().getInkarnationsName());
		result.add(applikation.getStatus());
		result.add(applikation.getLetzteStartzeit());
		return result;
	}

}