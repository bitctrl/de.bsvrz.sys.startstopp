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

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.gui2.ComponentRenderer;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.TextGUIGraphics;

public class OnlineStatusLabel extends Label {

	private final class OnlineStatusLabelRenderer implements ComponentRenderer<Label> {
		@Override
		public TerminalSize getPreferredSize(Label component) {
			return new TerminalSize(getText().length(), 1);
		}

		@Override
		public void drawComponent(TextGUIGraphics graphics, Label component) {
			ThemeDefinition themeDefinition = component.getThemeDefinition();

			graphics.applyThemeStyle(themeDefinition.getNormal());
			if (themeDefinition.getBooleanProperty("COLOR_STATUS", false)) {
				graphics.setForegroundColor(getForegroundColor());
				graphics.setBackgroundColor(getBackgroundColor());
			} else {
				graphics.setForegroundColor(TextColor.ANSI.DEFAULT);
				graphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
			}
			graphics.putString(0, 0, getText());
		}
	}

	public OnlineStatusLabel() {
		super("     ");
	}

	@Override
	protected ComponentRenderer<Label> createDefaultRenderer() {
		return new OnlineStatusLabelRenderer();
	}
}
