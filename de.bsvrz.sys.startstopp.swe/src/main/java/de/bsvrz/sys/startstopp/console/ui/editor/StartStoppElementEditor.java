package de.bsvrz.sys.startstopp.console.ui.editor;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;

public abstract class StartStoppElementEditor<T> extends DialogWindow {

	private boolean okPressed;
	
	@Inject
	public StartStoppElementEditor(@Assisted String title) {
		super(title);
		setHints(Arrays.asList(new Hint[]{Hint.CENTERED, Hint.FIT_TERMINAL_WINDOW}));
	}

	@Inject
	@PostConstruct
	private final void initUI() {
		Panel buttonPanel = new Panel();
		buttonPanel.setLayoutManager(new GridLayout(2).setHorizontalSpacing(1));
		Button okButton = new Button("OK", new Runnable() {
			@Override
			public void run() {
				okPressed = true;
				close();
			}
		});
		buttonPanel.addComponent(okButton);
		Button cancelButton = new Button("Abbrechen", new Runnable() {

			@Override
			public void run() {
				close();
			}
		});
		buttonPanel.addComponent(cancelButton);

		Panel mainPanel = new Panel();
		mainPanel.setLayoutManager(new GridLayout(1).setLeftMarginSize(1).setRightMarginSize(1));

		initComponents(mainPanel);
		
		mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));
		
		buttonPanel.setLayoutData(
				GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER, false, false))
				.addTo(mainPanel);

		setComponent(mainPanel);
	}

	@Override
	public Boolean showDialog(WindowBasedTextGUI textGUI) {
		super.showDialog(textGUI);
		return okPressed;
	}
	
	protected abstract void initComponents(Panel mainPanel);
	public abstract T getElement();
}
