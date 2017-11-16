package de.bsvrz.sys.startstopp.process;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.ZugangDav;
import de.bsvrz.sys.startstopp.process.os.OSApplikation;
import de.bsvrz.sys.startstopp.process.os.OSApplikationStatus;
import de.bsvrz.sys.startstopp.startstopp.StartStopp;

public class DavWrapper implements StandardApplication {

	private String appArguments;
	private String wrappedApp;
	private ClientDavInterface connection;
	private OSApplikation runner;

	@Override
	public void parseArguments(ArgumentList argumentList) throws Exception {
		wrappedApp = argumentList.fetchArgument("-wrappedApp=").asNonEmptyString();
		appArguments = argumentList.fetchArgument("-appArguments=").asNonEmptyString();
		appArguments = URLDecoder.decode(appArguments, "UTF-8");
	}

	@Override
	public void initialize(ClientDavInterface connection) throws Exception {

		this.connection = connection;

		runner = new OSApplikation(wrappedApp, wrappedApp);
		runner.setProgrammArgumente(appArguments);
		runner.onStatusChange.addHandler((status) -> handleOSApplikationStatus(status));
		runner.start();
	}

	public void handleOSApplikationStatus(OSApplikationStatus neuerStatus) {

		switch (neuerStatus) {
		case STARTFEHLER:
			connection.disconnect(true, String.join(",", runner.getProzessAusgabe()));
			System.exit(0);
			break;
		case GESTOPPT:
			connection.disconnect(false, "");
			System.exit(0);
			break;
		case GESTARTET:
		default:
			break;
		}
	}

	public static String getWrapperArguments(ProzessManager prozessManager, String applikationsName,
			String applikationsArgumente) throws StartStoppException {

		StringBuilder builder = new StringBuilder(500);

		String startPath = new File(System.getProperty("user.dir")).toURI().getPath();
		String classPath;
		try {
			classPath = DavWrapper.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		} catch (URISyntaxException e) {
			throw new StartStoppException(e.getLocalizedMessage());
		}
		classPath = classPath.replace(startPath, "");

		builder.append("-cp ");
		builder.append("../distributionspakete/de.bsvrz.sys.startstopp/de.bsvrz.sys.startstopp-runtime.jar ");
		builder.append(DavWrapper.class.getName());
		builder.append(' ');

		ZugangDav zugangDav = prozessManager.getZugangDav();
		if (zugangDav != null) {
			builder.append("-datenverteiler=");
			builder.append(zugangDav.getAdresse());
			builder.append(':');
			builder.append(zugangDav.getPort());
			builder.append(' ');

			builder.append("-benutzer=");
			builder.append(zugangDav.getUserName());
			builder.append(' ');
		}

		File passwdFile = StartStopp.getInstance().getOptions().getPasswdFile();
		if (passwdFile != null) {
			builder.append("-authentifizierung=");
			builder.append(passwdFile.getAbsolutePath());
			builder.append(' ');
		}

		builder.append("-wrappedApp=");
		builder.append(applikationsName);
		builder.append(' ');

		builder.append("-appArguments=");
		try {
			builder.append(URLEncoder.encode(applikationsArgumente, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new StartStoppException(e.getLocalizedMessage());
		}

		return builder.toString();
	}

	public static void main(String[] args) {
		StandardApplicationRunner.run(new DavWrapper(), args);
	}
}
