package de.bsvrz.sys.startstopp.process.sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public class Starter {

	public static class DaemonThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = Executors.defaultThreadFactory().newThread(r);
			thread.setDaemon(false);
			return thread;
		}

	}

	public static class StreamReader extends Thread {

		private InputStream inputStream;

		public StreamReader(InputStream inputStream) {
			this.inputStream = inputStream;
		}

		@Override
		public void run() {
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(inputStream, Charset.defaultCharset()))) {
				String line = null;
				do {
					line = reader.readLine();
					if (line != null) {
						System.err.println(line);
					}
				} while (line != null);

			} // TODO Auto-generated method stub
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {

		String startPath = new File(System.getProperty("user.dir")).toURI().getPath();
		String classPath = Sample.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		classPath = classPath.replace(startPath, "");

//		 Process process = Runtime.getRuntime().exec("java -cp " + classPath + " net.upeuker.test.Sample");
		Process process = Runtime.getRuntime().exec("java huhu");
//		 Process process = Runtime.getRuntime().exec("java -version");

		StreamReader inputReader = new StreamReader(process.getInputStream());
		inputReader.start();
		StreamReader errorReader = new StreamReader(process.getErrorStream());
//		errorReader.start();
		process.getErrorStream().close();

		int waitCode = process.waitFor();
		int exitValue = process.exitValue();
		System.err.println("WaitCode: " + waitCode + " ExitCode: " + exitValue);

	}
}
