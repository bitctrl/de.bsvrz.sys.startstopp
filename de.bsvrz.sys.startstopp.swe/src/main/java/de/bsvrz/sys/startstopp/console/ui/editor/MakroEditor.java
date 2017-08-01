package de.bsvrz.sys.startstopp.console.ui.editor;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.WindowListener;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.input.KeyStroke;

import de.bsvrz.sys.startstopp.api.jsonschema.MakroDefinition;
import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;

public class MakroEditor extends DialogWindow {

	private MakroDefinition result;
	private Button okButton;
	private MakroDefinition makroDefinition;
	private Button cancelButton;
	private TextBox nameField;
	private TextBox wertField;

	public MakroEditor(StartStoppSkript skript, MakroDefinition makroDefinition) {
		super("StartStopp - Editor: Inkarnation: ");

		this.makroDefinition = makroDefinition;
		setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.FIT_TERMINAL_WINDOW));
		setCloseWindowWithEscape(true);
		
		initUI();
	}
	
	private void initUI() {
		Panel buttonPanel = new Panel();
		buttonPanel.setLayoutManager(new GridLayout(2).setHorizontalSpacing(1));
		okButton = new Button("OK", new Runnable() {

			@Override
			public void run() {
				makroDefinition.setName(nameField.getText());
				makroDefinition.setWert(wertField.getText());
				result = makroDefinition;
				close();
			}
		});
		buttonPanel.addComponent(okButton);
		cancelButton = new Button("Abbrechen", new Runnable() {

			@Override
			public void run() {
				close();
			}
		});
		buttonPanel.addComponent(cancelButton);

		Panel mainPanel = new Panel();
		mainPanel.setLayoutManager(new GridLayout(1).setLeftMarginSize(1).setRightMarginSize(1));

		mainPanel.addComponent(new Label("Name:"));
		nameField = new TextBox();
		nameField.setText(makroDefinition.getName());
		nameField.setPreferredSize(new TerminalSize(nameField.getText().length(), 1));
		mainPanel.addComponent(nameField, GridLayout.createHorizontallyFilledLayoutData(1));
		
		mainPanel.addComponent(new Label("Adresse:"));
		wertField = new TextBox("");
		wertField.setText(makroDefinition.getWert());
		wertField.setPreferredSize(new TerminalSize(wertField.getText().length(), 1));
		mainPanel.addComponent(wertField, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));
		
		buttonPanel.setLayoutData(
				GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER, false, false))
				.addTo(mainPanel);

		setComponent(mainPanel);
	}
	
	@Override
	public MakroDefinition showDialog(WindowBasedTextGUI textGUI) {
		super.showDialog(textGUI);
		return result;
	}
}
