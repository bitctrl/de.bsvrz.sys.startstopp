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

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.TextGUIGraphics;

public class StartStoppButton extends Button implements HasHotkey {

	private static class StartStoppButtonRenderer implements ButtonRenderer {

		@Override
		public TerminalPosition getCursorLocation(Button button) {
	        if(button.getThemeDefinition().isCursorVisible()) {
	            return new TerminalPosition(1 + getLabelShift(button, button.getSize()), 0);
	        }
	        else {
	            return null;
	        }
		}

		@Override
		public TerminalSize getPreferredSize(Button button) {
	        return new TerminalSize(Math.max(8, TerminalTextUtils.getColumnWidth(button.getLabel()) + 2), 1);
		}

		@Override
		public void drawComponent(TextGUIGraphics graphics, Button button) {
			
			StartStoppButton startStoppButton = (StartStoppButton) button;
			int hotkeyOffset = startStoppButton.getHotkeyOffset();
			
	        ThemeDefinition themeDefinition = button.getThemeDefinition();
	        if(button.isFocused()) {
	            graphics.applyThemeStyle(themeDefinition.getActive());
	        }
	        else {
	            graphics.applyThemeStyle(themeDefinition.getInsensitive());
	        }
	        graphics.fill(' ');
	        graphics.setCharacter(0, 0, themeDefinition.getCharacter("LEFT_BORDER", '<'));
	        graphics.setCharacter(graphics.getSize().getColumns() - 1, 0, themeDefinition.getCharacter("RIGHT_BORDER", '>'));

	        if(button.isFocused()) {
	            graphics.applyThemeStyle(themeDefinition.getActive());
	        }
	        else {
	            graphics.applyThemeStyle(themeDefinition.getPreLight());
	        }
	        int labelShift = getLabelShift(button, graphics.getSize());
	        graphics.setCharacter(1 + labelShift + hotkeyOffset, 0, button.getLabel().charAt(hotkeyOffset));

	        if(TerminalTextUtils.getColumnWidth(button.getLabel()) == 1) {
	            return;
	        }
	        if(button.isFocused()) {
	            graphics.applyThemeStyle(themeDefinition.getSelected());
	        }
	        else {
	            graphics.applyThemeStyle(themeDefinition.getNormal());
	        }
	        if( hotkeyOffset > 0) {
		        graphics.putString(1 + labelShift, 0, button.getLabel().substring(0, hotkeyOffset));
	        }
	        graphics.putString(1 + labelShift + hotkeyOffset + 1, 0, button.getLabel().substring(hotkeyOffset + 1));
		}

	    private int getLabelShift(Button button, TerminalSize size) {
	        int availableSpace = size.getColumns() - 2;
	        if(availableSpace <= 0) {
	            return 0;
	        }
	        int labelShift = 0;
	        int widthInColumns = TerminalTextUtils.getColumnWidth(button.getLabel());
	        if(availableSpace > widthInColumns) {
	            labelShift = (size.getColumns() - 2 - widthInColumns) / 2;
	        }
	        return labelShift;
	    }
	}
	
	public StartStoppButton(String label) {
		super(label);
		setRenderer(new StartStoppButtonRenderer());
	}

	public StartStoppButton(String label, Runnable runnable) {
		super(label, runnable);
		setRenderer(new StartStoppButtonRenderer());
	}

	@Override
	public String getLabel() {
		String result = super.getLabel();
		return result.replace("&", "");
	}

	int getHotkeyOffset() {
		int result = super.getLabel().indexOf('&');
		return Math.max(result, 0);
	}

	@Override
	public Character getHotkey() {
		return getLabel().charAt(getHotkeyOffset());
	}
}
