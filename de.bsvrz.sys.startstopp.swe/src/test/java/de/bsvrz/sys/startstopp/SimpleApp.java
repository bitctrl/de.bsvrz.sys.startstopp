package de.bsvrz.sys.startstopp;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientDavParameters;
import de.bsvrz.dav.daf.main.config.management.UserAdministration;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

public class SimpleApp implements StandardApplication {

	@Override
	public void parseArguments(ArgumentList argumentList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initialize(ClientDavInterface connection) throws Exception {
		// TODO Auto-generated method stub

	}
	
	public static void main(String[] args) throws Exception {
//		StandardApplicationRunner.run(new SimpleApp(), args);
		
		ClientDavParameters parameters = new ClientDavParameters();
		ClientDavConnection connection = new ClientDavConnection(parameters);
		connection.connect();
		connection.login("Tester", "geheim");
		
		System.err.println("Verbunden: " + connection.isConnected());
		System.err.println("Angemeldet: " + connection.isLoggedIn());
		
		UserAdministration userAdministration = connection.getDataModel().getUserAdministration();
//		userAdministration.changeUserPassword("Tester", "geheim", "TestDatenverteilerBenutzer" , "geheim");
		
		boolean userAdmin = userAdministration.isUserAdmin("TestDatenverteilerBenutzer", "geheim", "TestDatenverteilerBenutzer");
		System.err.println("Admin: " + userAdmin);
		
		System.exit(0);
	}

}
