package de.bsvrz.sys.startstopp.console.ui;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.GridLayout.Alignment;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowListener;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.input.KeyStroke;

import de.bsvrz.sys.startstopp.config.StartStoppException;

public class StartStoppEditWindow extends BasicWindow implements WindowListener {
	private InkarnationTable table;

	public StartStoppEditWindow() throws StartStoppException {
		super("StartStopp - Editor");

		setHints(Arrays.asList(Window.Hint.FULL_SCREEN));

		Panel panel = new Panel();
		panel.setLayoutManager(new GridLayout(1));
		panel.setLayoutData(GridLayout.createLayoutData(Alignment.BEGINNING, Alignment.BEGINNING, true, true));

		Label infoLabel = new Label("s-System   i-Inkarnation e-Bearbeiten");
		panel.addComponent(infoLabel.withBorder(Borders.singleLine()));
		infoLabel.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));

		table = new InkarnationTable();
		table.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));
		panel.addComponent(table.withBorder(Borders.singleLine()));

		addWindowListener(this);

		setComponent(panel);
	}

	@Override
	public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
		// TODO Auto-generated method stub
		switch (keyStroke.getKeyType()) {
		case Character:
			switch (keyStroke.getCharacter()) {
			case 's':
				ActionListDialogBuilder builder = new ActionListDialogBuilder().setTitle("System");
				builder.addActions(new EditorSaveAction(), new EditorCloseAction(this));
				builder.build().showDialog(getTextGUI());
				break;
			
			}
			break;

		case Escape:
			close();
			break;
		default:
			break;
		}
	}

	@Override
	public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
		table.setVisibleRows(newSize.getRows() - 2);

	}

	@Override
	public void onMoved(Window window, TerminalPosition oldPosition, TerminalPosition newPosition) {
		// TODO Auto-generated method stub

	}
}