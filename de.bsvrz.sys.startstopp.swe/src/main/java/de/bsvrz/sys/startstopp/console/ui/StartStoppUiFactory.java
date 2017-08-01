package de.bsvrz.sys.startstopp.console.ui;

import javax.inject.Inject;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialog;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;

import de.bsvrz.sys.startstopp.console.ui.editor.SkriptEditor;

@Singleton
public class StartStoppUiFactory {
	
	@Inject
	Injector injector;

	public ActionListDialog createApplikationsMenue(String inkarnation) {
		ActionListDialogBuilder builder = new ActionListDialogBuilder().setTitle("Applikation");

		Class<?>[] actionClasses = {ApplikationStartAction.class, ApplikationRestartAction.class,
				ApplikationStoppAction.class, ApplikationDetailAction.class};
		
		
		for( Class<?> actionClass : actionClasses) {
			ApplikationAction action = (ApplikationAction) injector.getInstance(actionClass);
			action.setInkarnation(inkarnation);
			builder.addAction(action);
		}
		
		return builder.build();
	}

	public ActionListDialog createSystemMenue() {
		ActionListDialogBuilder builder = new ActionListDialogBuilder().setTitle("System");

		Class<?>[] actionClasses = {StartStoppStoppAction.class, StartStoppRestartAction.class,
				StartStoppExitAction.class, TerminalCloseAction.class};

		for( Class<?> actionClass : actionClasses) {
			Runnable action = (Runnable) injector.getInstance(actionClass);
			builder.addAction(action);
		}
		
		return builder.build();
	}

	public SkriptEditor getSkriptEditor() {
		return injector.getInstance(SkriptEditor.class);
	}
}
