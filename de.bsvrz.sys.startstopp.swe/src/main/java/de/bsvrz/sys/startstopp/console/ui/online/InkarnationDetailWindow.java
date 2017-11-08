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
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung.Warteart;
import de.bsvrz.sys.startstopp.api.jsonschema.StartFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppFehlerVerhalten.Option;
import de.bsvrz.sys.startstopp.api.util.Util;
import de.bsvrz.sys.startstopp.console.StartStoppConsole;
import de.bsvrz.sys.startstopp.console.ui.StartStoppButton;

class InkarnationDetailWindow extends BasicWindow {

	

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
		Label typLabel = new Label(inkarnation.getInkarnationsTyp().name());
		typLabel.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));
		panel.addComponent(typLabel);

		panel.addComponent(new Label("StartArt: "));
		panel.addComponent(new Label(getStartArtStr(inkarnation)));

		panel.addComponent(new Label("Initialisierung: "));
		panel.addComponent(new Label(getInitialisierungStr(inkarnation)));

		StartBedingung startBedingung = inkarnation.getStartBedingung();
		showStartBedingungen(panel, startBedingung);

		StartFehlerVerhalten startFehlerVerhalten = inkarnation.getStartFehlerVerhalten();
		showStartFehlerVerhalten(panel, startFehlerVerhalten);

		StoppBedingung stoppBedingung = inkarnation.getStoppBedingung();
		showStoppBedingung(panel, stoppBedingung);

		StoppFehlerVerhalten stoppFehlerVerhalten = inkarnation.getStoppFehlerVerhalten();
		showStoppFehlerVerhalten(panel, stoppFehlerVerhalten);

		Button applikationButton = new StartStoppButton("Applikation", () -> showApplikationPanel());
		panel.addComponent(applikationButton.withBorder(Borders.singleLine()));
		applikationButton.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.CENTER, false, false, 2, 1));

		setComponent(panel);
	}

	private void showStoppFehlerVerhalten(Panel panel, StoppFehlerVerhalten verhalten) {
		
		if( verhalten == null) {
			return;
		}

		Label label = new Label("");
		panel.addComponent(label);
		label.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.CENTER, false, false, 2, 1));

		panel.addComponent(new Label("StoppFehlerverhalten: "));
		String verhaltenStr = verhalten.getOption().name();
		int wiederholungen = Integer.parseInt(Util.nonEmptyString(verhalten.getWiederholungen(), "0"));
		if (wiederholungen > 0) {
			verhaltenStr += ", " + wiederholungen + " Wiederholungen";
		}
		panel.addComponent(new Label(verhaltenStr));
	}

	private void showStoppBedingung(Panel panel, StoppBedingung bedingung) {
		
		if( bedingung == null) {
			return;
		}

		Label label = new Label("");
		panel.addComponent(label);
		label.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.CENTER, false, false, 2, 1));

		panel.addComponent(new Label("StoppBedingung: "));
		String referenzStr = String.join(",", bedingung.getNachfolger());
		String rechner = bedingung.getRechner();
		if (rechner != null && !rechner.isEmpty()) {
			referenzStr = rechner + ": " + referenzStr;
		}
		panel.addComponent(new Label(referenzStr));

		int warteZeit = Integer.parseInt(Util.nonEmptyString(bedingung.getWartezeit(), "0"));
		if( warteZeit > 0) {
			panel.addComponent(new Label("Wartezeit: "));
			panel.addComponent(new Label(warteZeit + " Sekunden"));
		}
	}

	private void showStartFehlerVerhalten(Panel panel, StartFehlerVerhalten verhalten) {
		if( verhalten == null) {
			return;
		}
		
		Label label = new Label("");
		panel.addComponent(label);
		label.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.CENTER, false, false, 2, 1));
		
		panel.addComponent(new Label("StartFehlerverhalten: "));
		String verhaltenStr = verhalten.getOption().name();
		int wiederholungen = Integer.parseInt(Util.nonEmptyString(verhalten.getWiederholungen(), "0"));
		if (wiederholungen > 0) {
			verhaltenStr += ", " + wiederholungen + " Wiederholungen";
		}
		panel.addComponent(new Label(verhaltenStr));
	}

	private void showStartBedingungen(Panel panel, StartBedingung bedingung) {
		
		if( bedingung == null) {
			return;
		}

		Label label = new Label("");
		label.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.CENTER, false, false, 2, 1));
		panel.addComponent(label);

		panel.addComponent(new Label("StartBedingung: "));
		String vorgaengerStr = String.join(",", bedingung.getVorgaenger());
		String rechner = bedingung.getRechner();
		if (rechner != null && !rechner.isEmpty()) {
			vorgaengerStr = rechner + ": " + vorgaengerStr;
		}
		panel.addComponent(new Label(vorgaengerStr));

		panel.addComponent(new Label("WarteArt: "));
		String warteStr = bedingung.getWarteart().name();
		int warteZeit = Integer.parseInt(Util.nonEmptyString(bedingung.getWartezeit(), "0"));
		if( warteZeit > 0) {
			warteStr += ", Wartezeit: " + warteZeit + " Sekunden";
		}
		panel.addComponent(new Label(warteStr));
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
