package de.bsvrz.sys.startstopp.console.ui;

import java.util.Collections;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Interactable.FocusChangeDirection;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.input.KeyStroke;

public class UrlasserDialog extends DialogWindow {

	private MessageDialogButton result = MessageDialogButton.Cancel;
	private TextBox grundField;
	private TextBox passwdField;
	private TextBox nameField;
	private Button okButton;
	private Button cancelButton;

	protected UrlasserDialog(String title) {
		super(title);

		setHints(Collections.singleton(Hint.CENTERED));
		setCloseWindowWithEscape(true);
		
		Panel buttonPanel = new Panel();
		buttonPanel.setLayoutManager(new GridLayout(2).setHorizontalSpacing(1));
		okButton = new Button("OK", new Runnable() {
			@Override
			public void run() {
				result = MessageDialogButton.OK;
				close();
			}
		});
		buttonPanel.addComponent(okButton);
		cancelButton = new Button("Abbrechen", new Runnable() {
			@Override
			public void run() {
				result = MessageDialogButton.Cancel;
				close();
			}
		});
		buttonPanel.addComponent(cancelButton);

		Panel mainPanel = new Panel();
		mainPanel.setLayoutManager(new GridLayout(1).setLeftMarginSize(1).setRightMarginSize(1));

		mainPanel.addComponent(new Label("Name:"));
		nameField = new TextBox();
		mainPanel.addComponent(nameField, GridLayout.createHorizontallyFilledLayoutData(1));
		
		mainPanel.addComponent(new Label("Passwort:"));
		passwdField = new TextBox("");
		passwdField.setMask('*');
		mainPanel.addComponent(passwdField, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new Label("Grund:"));
		grundField = new TextBox();
		mainPanel.addComponent(grundField, GridLayout.createHorizontallyFilledLayoutData(1));
		mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));
		
		buttonPanel.setLayoutData(
				GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER, false, false))
				.addTo(mainPanel);

		setComponent(mainPanel);
	}

	@Override
	public MessageDialogButton showDialog(WindowBasedTextGUI textGUI) {
		result = MessageDialogButton.Cancel;
		super.showDialog(textGUI);
		return result;
	}

	public String getGrund() {
		return grundField.getText();
	}

	public String getVeranlasser() {
		return nameField.getText();
	}

	public String getPasswort() {
		return passwdField.getText();
	}
	
	@Override
	public boolean handleInput(KeyStroke key) {
		System.err.println("Urlasserdialog: " + key);
		if( key.isAltDown()) {
			switch(key.getCharacter()) {
			case 'a':
			case 'A':
				setFocusedInteractable(cancelButton, FocusChangeDirection.NEXT);
				break;
			case 'o':
			case 'O':
				setFocusedInteractable(okButton, FocusChangeDirection.NEXT);
				break;
			}
			return true;
		}
		return super.handleInput(key);
	}
}
