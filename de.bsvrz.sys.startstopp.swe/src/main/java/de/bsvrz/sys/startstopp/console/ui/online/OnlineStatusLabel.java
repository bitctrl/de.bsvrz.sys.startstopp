package de.bsvrz.sys.startstopp.console.ui.online;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.gui2.ComponentRenderer;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.TextGUIGraphics;

public class OnlineStatusLabel extends Label {

	public OnlineStatusLabel() {
		super(" ");
	}

	@Override
	protected ComponentRenderer<Label> createDefaultRenderer() {

		return new ComponentRenderer<Label>() {

			@Override
			public TerminalSize getPreferredSize(Label component) {
				return TerminalSize.ONE;
			}

			@Override
			public void drawComponent(TextGUIGraphics graphics, Label component) {
                ThemeDefinition themeDefinition = component.getThemeDefinition();

            	graphics.applyThemeStyle(themeDefinition.getNormal());
                if (themeDefinition.getBooleanProperty("COLOR_STATUS", false)) {
                	graphics.setBackgroundColor(getBackgroundColor());
                	graphics.putString(0, 0, " ");
                } else {
                	graphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                	graphics.putString(0, 0, getText());
                }
			}
		};
	}
}
