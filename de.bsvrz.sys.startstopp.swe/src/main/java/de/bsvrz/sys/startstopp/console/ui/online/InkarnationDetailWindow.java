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

package de.bsvrz.sys.startstopp.console.ui.online;

import java.util.Collections;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;

import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartArt;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StartFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;
import de.bsvrz.sys.startstopp.console.StartStoppConsole;
import de.bsvrz.sys.startstopp.console.ui.StartStoppButton;

class InkarnationDetailWindow extends BasicWindow {

	public class StartFehlerVerhaltenDetails extends Panel {

		public StartFehlerVerhaltenDetails(StartFehlerVerhalten verhalten) {
			// TODO Auto-generated constructor stub
		}

	}

	public class StartBedingungDetails extends Panel {

		public StartBedingungDetails(StartBedingung bedingung) {
			// TODO Auto-generated constructor stub
		}
	}

	public class StoppFehlerVerhaltenDetails extends Panel {

		public StoppFehlerVerhaltenDetails(StoppFehlerVerhalten verhalten) {
			// TODO Auto-generated constructor stub
		}

	}

	public class StoppBedingungDetails extends Panel {

		public StoppBedingungDetails(StoppBedingung bedingung) {
			// TODO Auto-generated constructor stub
		}
	}

	private Applikation applikation;

	InkarnationDetailWindow(Applikation applikation) {
		super("Details (Inkarnation): " + applikation.getInkarnation().getInkarnationsName());
		this.applikation = applikation;
		setCloseWindowWithEscape(true);
		initUI();
	}

	private void initUI() {

		Inkarnation inkarnation = applikation.getInkarnation();

		Panel panel = new Panel();
		panel.setLayoutManager(new GridLayout(2));
		panel.setLayoutData(GridLayout.createHorizontallyEndAlignedLayoutData(1));

		panel.addComponent(new Label("Inkarnationstyp: "));
		panel.addComponent(new Label(inkarnation.getInkarnationsTyp().name()));

		panel.addComponent(new Label("StartArt: "));
		panel.addComponent(new Label(getStartArtStr(inkarnation)));

		panel.addComponent(new Label("Initialisierung: "));
		panel.addComponent(new Label(getInitialisierungStr(inkarnation)));

		if (inkarnation.getStartBedingung() != null) {
			StartBedingungDetails details = new StartBedingungDetails(inkarnation.getStartBedingung());
			panel.addComponent(details);
			details.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2));
		}

		if (inkarnation.getStartFehlerVerhalten() != null) {
			StartFehlerVerhaltenDetails details = new StartFehlerVerhaltenDetails(
					inkarnation.getStartFehlerVerhalten());
			panel.addComponent(details);
			details.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2));
		}

		if (inkarnation.getStoppBedingung() != null) {
			StoppBedingungDetails details = new StoppBedingungDetails(inkarnation.getStoppBedingung());
			panel.addComponent(details);
			details.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2));
		}

		if (inkarnation.getStoppFehlerVerhalten() != null) {
			StoppFehlerVerhaltenDetails details = new StoppFehlerVerhaltenDetails(
					inkarnation.getStoppFehlerVerhalten());
			panel.addComponent(details);
			details.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2));
		}

		Button applikationButton = new StartStoppButton("Applikation", () -> showApplikationPanel());
		panel.addComponent(applikationButton.withBorder(Borders.singleLine()));
		applikationButton.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2));

		setComponent(panel);
	}

	private void showApplikationPanel() {
		if (applikation != null) {
			ApplikationDetailWindow window = new ApplikationDetailWindow(applikation);
			window.setHints(Collections.singleton(Hint.EXPANDED));
			close();
			StartStoppConsole.getGui().addWindow(window);
		}
	}

	private String getInitialisierungStr(Inkarnation inkarnation) {
		StringBuilder initialisierungsString = new StringBuilder(100);
		if (inkarnation.getMitInkarnationsName()) {
			initialisierungsString.append("Mit Inkarnationsname");
		} else {
			initialisierungsString.append("Ohne Inkarnationsname");
		}

		if (inkarnation.getInitialize()) {
			initialisierungsString.append(" - automatisch");
		} else {
			initialisierungsString.append(" - DAV");
		}

		return initialisierungsString.toString();
	}

	private String getStartArtStr(Inkarnation inkarnation) {
		StartArt startArt = inkarnation.getStartArt();
		String startArtStr = startArt.getOption().name();
		switch (startArt.getOption()) {
		case INTERVALLABSOLUT:
		case INTERVALLRELATIV:
			startArtStr = startArtStr + " Intervall: \"" + Util.nonEmptyString(startArt.getIntervall());
			break;
		case AUTOMATISCH:
		case MANUELL:
		default:
			break;
		}

		if (startArt.getNeuStart()) {
			startArtStr = startArtStr + " NEUSTART";
		}

		return startArtStr;
	}
}
