package de.bsvrz.sys.startstopp.console.ui.editor;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.WindowListener;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import de.bsvrz.sys.startstopp.api.jsonschema.MakroDefinition;
import de.bsvrz.sys.startstopp.api.jsonschema.Rechner;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;

public class RechnerEditor extends DialogWindow implements WindowListener {

	private Button okButton;
	private Button cancelButton;
	private TextBox nameField;
	private TextBox addresseField;
	private TextBox portField;
	
	private Rechner rechner;
	private Rechner result;
	private StartStoppSkript skript;


	public RechnerEditor(StartStoppSkript skript, Rechner rechner) {
		super("StartStopp - Editor: Rechner: " + rechner.getName());

		this.skript = skript;
		this.rechner = rechner;
		
		setHints(Arrays.asList(Window.Hint.CENTERED));
		setCloseWindowWithEscape(true);
		addWindowListener(this);

		initUI();
	}
	
	private void initUI() {
		Panel buttonPanel = new Panel();
		buttonPanel.setLayoutManager(new GridLayout(2).setHorizontalSpacing(1));
		okButton = new Button("OK", new Runnable() {

			@Override
			public void run() {
				rechner.setName(nameField.getText());
				rechner.setTcpAdresse(addresseField.getText());
				rechner.setPort(portField.getText());
				result = rechner;
				
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
		nameField.setText(rechner.getName());
		mainPanel.addComponent(nameField, GridLayout.createHorizontallyFilledLayoutData(1));
		
		mainPanel.addComponent(new Label("Adresse:"));
		addresseField = new TextBox("");
		addresseField.setText(rechner.getTcpAdresse());
		mainPanel.addComponent(addresseField, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new Label("Port:"));
		portField = new TextBox();
		portField.setText(rechner.getPort());
		portField.setValidationPattern(Pattern.compile("\\d*"));

		mainPanel.addComponent(portField, GridLayout.createHorizontallyFilledLayoutData(1));

		mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));
		
		buttonPanel.setLayoutData(
				GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER, false, false))
				.addTo(mainPanel);

		setComponent(mainPanel);
	}

	@Override
	public Rechner showDialog(WindowBasedTextGUI textGUI) {
		super.showDialog(textGUI);
		return result;
	}
	
	@Override
	public boolean handleInput(KeyStroke key) {
		
		System.err.println("RechnerEditor: " + key);
		if(key.isAltDown()) {
			if( key.getKeyType() == KeyType.Character) {
				switch(key.getCharacter()) {
				case 'm':
				case 'M':
					MakroSelektor selektor = new MakroSelektor(getTextGUI(), skript);
					MakroDefinition selectedMakroDefinition = selektor.getSelectedMakroDefinition();
					if( selectedMakroDefinition != null) {
						Interactable interactable = getFocusedInteractable();
						if( interactable instanceof TextBox) {
							TextBox textBox = (TextBox) interactable;
							int position = textBox.getCursorLocation().getColumn();
							StringBuilder oldText = new StringBuilder(textBox.getText());
							oldText.insert(position, "%" + selectedMakroDefinition.getName() + "%");
							textBox.setText(oldText.toString());
						}
					}
					break;
				}
			}
			return true;
		}
		
		// TODO Auto-generated method stub
		return super.handleInput(key);
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
