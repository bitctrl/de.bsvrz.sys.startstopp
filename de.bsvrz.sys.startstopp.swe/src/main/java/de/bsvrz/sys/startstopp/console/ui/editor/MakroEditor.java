package de.bsvrz.sys.startstopp.console.ui.editor;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Window; 
import com.googlecode.lanterna.gui2.WindowListener;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.input.KeyStroke;

public class MakroEditor extends DialogWindow implements WindowListener {

	public MakroEditor() {
		super("StartStopp - Editor: Inkarnation: ");

		setHints(Arrays.asList(Window.Hint.CENTERED));
		setCloseWindowWithEscape(true);
		addWindowListener(this);
	}
	
	@Override
	public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMoved(Window window, TerminalPosition oldPosition, TerminalPosition newPosition) {
		// TODO Auto-generated method stub
		
	}

}
