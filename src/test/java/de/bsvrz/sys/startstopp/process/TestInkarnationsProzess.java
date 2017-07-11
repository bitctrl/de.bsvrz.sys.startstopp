package de.bsvrz.sys.startstopp.process;


import java.util.logging.Level;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.process.InkarnationsProzess;
import de.bsvrz.sys.startstopp.process.InkarnationsProzessIf;
import de.bsvrz.sys.startstopp.process.InkarnationsProzessListener;
import de.bsvrz.sys.startstopp.process.InkarnationsProzessStatus;

public class TestInkarnationsProzess {
	
	private InkarnationsProzessStatus status;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
//		Debug logger = Debug.getLogger();
		Debug.setHandlerLevel("StdErr", Level.FINE);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testStartFehler_NoSuchFileOrDirecory() {
		InkarnationsProzessIf inkarnationsProzess = new InkarnationsProzess();
		inkarnationsProzess.setProgramm("bubu");
		inkarnationsProzess.setInkarnationsName("Test");
		inkarnationsProzess.start();
		while (inkarnationsProzess.getStatus() == InkarnationsProzessStatus.GESTOPPT) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Assert.assertEquals("Status", InkarnationsProzessStatus.STARTFEHLER, inkarnationsProzess.getStatus());
		Assert.assertTrue("Startfehler", inkarnationsProzess.getStartFehler().contains("error=2"));
	}
	
	@Test
	public final void testStartFehler_MainClassNotFound() {
		Debug logger = Debug.getLogger();
		Debug.setHandlerLevel("StdErr", Level.FINE);
		InkarnationsProzessIf inkarnationsProzess = new InkarnationsProzess(logger);
		inkarnationsProzess.setProgramm("java bubu");
		inkarnationsProzess.setInkarnationsName("Test");
		inkarnationsProzess.start();
		while (inkarnationsProzess.getStatus() == InkarnationsProzessStatus.GESTOPPT || inkarnationsProzess.getStatus() == InkarnationsProzessStatus.GESTARTET) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Assert.assertEquals("Status", InkarnationsProzessStatus.STARTFEHLER, inkarnationsProzess.getStatus());
		Assert.assertTrue("Fehlermeldung", inkarnationsProzess.getProzessAusgabe().contains("Hauptklasse"));
		Assert.assertEquals("Exitcode", 1, inkarnationsProzess.getLastExitCode());
	}
	
	@Test
	public final void testStartFehler_InvalidOption() {
		InkarnationsProzessIf inkarnationsProzess = new InkarnationsProzess();
		inkarnationsProzess.setProgramm("java");
		inkarnationsProzess.setProgrammArgumente("-invalidOption=");
		inkarnationsProzess.setInkarnationsName("Test");
		inkarnationsProzess.start();
		while (inkarnationsProzess.getStatus() == InkarnationsProzessStatus.GESTOPPT || inkarnationsProzess.getStatus() == InkarnationsProzessStatus.GESTARTET) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Assert.assertEquals("Status", InkarnationsProzessStatus.STARTFEHLER, inkarnationsProzess.getStatus());
		Assert.assertTrue("Fehlermeldung", inkarnationsProzess.getProzessAusgabe().contains("fatal exception"));
		Assert.assertEquals("Exitcode", 1, inkarnationsProzess.getLastExitCode());
	}

	@Test
	public final void testStartFehler_Listener() {	
		InkarnationsProzessIf inkarnationsProzess = new InkarnationsProzess();
		inkarnationsProzess.setProgramm("java");
		inkarnationsProzess.setProgrammArgumente("-invalidOption=");
		inkarnationsProzess.setInkarnationsName("Test");
		Object sync = new Object();
		status = null;
		inkarnationsProzess.start();
		
		inkarnationsProzess.addProzessListener(new InkarnationsProzessListener() {
			
			@Override
			public void statusChanged(InkarnationsProzessStatus neuerStatus) {
				synchronized (sync ) {
					System.out.println("Neuer Status: " + neuerStatus);
					status = neuerStatus;
					sync.notify();
				}
			}
		});

		synchronized (sync) {
			try {
				sync.wait(2000);
			} catch (InterruptedException e) {
				Assert.fail();
			}
		}
		
		Assert.assertEquals("Status", InkarnationsProzessStatus.GESTARTET, status);
		
		synchronized (sync) {
			try {
				sync.wait(2000);
			} catch (InterruptedException e) {
				Assert.fail();
			}
		}
		
		Assert.assertEquals("Status", InkarnationsProzessStatus.STARTFEHLER, status);
	}
	
	@Test
	public final void testStart() {	
		InkarnationsProzessIf inkarnationsProzess = new InkarnationsProzess(Debug.getLogger());
		inkarnationsProzess.setProgramm("java");
		inkarnationsProzess.setProgrammArgumente("-cp target/test-classes de.bsvrz.sys.startstopp.process.TestInkarnation");
		inkarnationsProzess.setInkarnationsName("Bubu");
		Object sync = new Object();
		status = null;
		inkarnationsProzess.start();
		
		inkarnationsProzess.addProzessListener(new InkarnationsProzessListener() {
			
			@Override
			public void statusChanged(InkarnationsProzessStatus neuerStatus) {
				synchronized (sync ) {
					System.out.println("Neuer Status: " + neuerStatus);
					status = neuerStatus;
					sync.notify();
				}
			}
		});

		synchronized (sync) {
			try {
				sync.wait(2000);
			} catch (InterruptedException e) {
				Assert.fail();
			}
		}
		
		Assert.assertEquals("Status", InkarnationsProzessStatus.GESTARTET, status);
		
		synchronized (sync) {
			try {
				sync.wait(15000);
			} catch (InterruptedException e) {
				Assert.fail();
			}
		}
		
		Assert.assertEquals("Status", InkarnationsProzessStatus.GESTOPPT, status);
	}
	
	@Test
	public final void testStopp() {	
		Debug logger = Debug.getLogger();
		Debug.setHandlerLevel("StdErr", Level.FINE);
		InkarnationsProzessIf inkarnationsProzess = new InkarnationsProzess(logger);
		inkarnationsProzess.setProgramm("java");
		inkarnationsProzess.setProgrammArgumente("-cp target/test-classes de.bsvrz.sys.startstopp.process.TestInkarnation");
		inkarnationsProzess.setInkarnationsName("Test");
		Object sync = new Object();
		status = null;
		inkarnationsProzess.start();
		
		inkarnationsProzess.addProzessListener(new InkarnationsProzessListener() {
			
			@Override
			public void statusChanged(InkarnationsProzessStatus neuerStatus) {
				synchronized (sync ) {
					System.out.println("Neuer Status: " + neuerStatus);
					status = neuerStatus;
					sync.notify();
				}
			}
		});

		synchronized (sync) {
			try {
				sync.wait(2000);
			} catch (InterruptedException e) {
				Assert.fail();
			}
		}
		
		Assert.assertEquals("Status", InkarnationsProzessStatus.GESTARTET, status);
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		inkarnationsProzess.stopp();
		
		synchronized (sync) {
			try {
				sync.wait(2000);
			} catch (InterruptedException e) {
				Assert.fail();
			}
		}
		
		Assert.assertEquals("Status", InkarnationsProzessStatus.GESTOPPT, status);
	}
}
