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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Panel;

import de.bsvrz.sys.startstopp.api.jsonschema.KernSystem;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;
import de.bsvrz.sys.startstopp.console.ui.EditableTable;

class KernsystemEditor extends StartStoppElementEditor<List<KernSystem>> {

	private class KernSystemTable extends EditableTable<KernSystem> {

		KernSystemTable(List<KernSystem> dataList, String ... columnName) {
			super(dataList, columnName);
		}

		@Override
		protected KernSystem requestNewElement() {
			InkarnationSelektor inkarnationSelektor = new InkarnationSelektor(skript);
			for (KernSystem ks : kernSysteme) {
				inkarnationSelektor.removeInkarnation(ks.getInkarnationsName());
			}
			return new KernSystem(inkarnationSelektor.getInkarnation(getTextGUI()).getInkarnationsName());
		}

		@Override
		protected KernSystem editElement(KernSystem oldElement) {
			return null;
		}

		@Override
		protected List<String> getStringsFor(KernSystem element) {
			return Collections.singletonList(element.getInkarnationsName());
		}
	}

	private List<KernSystem> kernSysteme = new ArrayList<>();
	private KernSystemTable ksTable;
	private StartStoppSkript skript;

	KernsystemEditor(StartStoppSkript skript) {
		super(skript, "Kernsystem");
		this.skript = skript;
		for (KernSystem kernSystem : skript.getGlobal().getKernsysteme()) {
			kernSysteme.add((KernSystem) Util.cloneObject(kernSystem));
		}
	}

	@Override
	protected void initComponents(Panel mainPanel) {
		mainPanel.setLayoutManager(new GridLayout(1).setLeftMarginSize(1).setRightMarginSize(1));
		ksTable = new KernSystemTable(kernSysteme, "Kernsystem");
		mainPanel.addComponent(ksTable, GridLayout.createHorizontallyFilledLayoutData(1));
	}

	@Override
	public List<KernSystem> getElement() {
		return kernSysteme;
	}
}
