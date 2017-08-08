/*
 * Segment 10 System (Sys), SWE 10.1 StartStopp
 * Copyright (C) 2007-2017 BitCtrl Systems GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Contact Information:<br>
 * BitCtrl Systems GmbH<br>
 * Weißenfelser Straße 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.sys.startstopp.process;

import java.io.File;
import java.util.logging.Level;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.bsvrz.sys.funclib.debug.Debug;

public class TestInkarnationsProzess {

	private InkarnationsProzessStatus status;
	private static String classPath;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Debug logger = Debug.getLogger();
		Debug.setHandlerLevel("StdErr", Level.FINE);
		String startPath = new File(System.getProperty("user.dir")).toURI().getPath();
		classPath = TestInkarnation.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		classPath = classPath.replace(startPath, "");
		Debug.getLogger().info("Set classpath to: " + classPath);
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
	public final void testStartFehlerNoSuchFileOrDirecory() {
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
	public final void testStartFehlerMainClassNotFound() {
		Debug logger = Debug.getLogger();
		Debug.setHandlerLevel("StdErr", Level.FINE);
		InkarnationsProzessIf inkarnationsProzess = new InkarnationsProzess(logger);
		inkarnationsProzess.setProgramm("java bubu");
		inkarnationsProzess.setInkarnationsName("Test");
		inkarnationsProzess.start();
		while (inkarnationsProzess.getStatus() == InkarnationsProzessStatus.GESTOPPT
				|| inkarnationsProzess.getStatus() == InkarnationsProzessStatus.GESTARTET) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Assert.assertEquals("Status", InkarnationsProzessStatus.STARTFEHLER, inkarnationsProzess.getStatus());
		String ausgabe = inkarnationsProzess.getProzessAusgabe().toLowerCase();
		Assert.assertTrue("Fehlermeldung: " + ausgabe, ausgabe.contains("hauptklasse") || ausgabe.contains("mainclass"));
		Assert.assertEquals("Exitcode", 1, inkarnationsProzess.getLastExitCode());
	}

	@Test
	public final void testStartFehlerInvalidOption() {
		InkarnationsProzessIf inkarnationsProzess = new InkarnationsProzess();
		inkarnationsProzess.setProgramm("java");
		inkarnationsProzess.setProgrammArgumente("-invalidOption=");
		inkarnationsProzess.setInkarnationsName("Test");
		inkarnationsProzess.start();
		while (inkarnationsProzess.getStatus() == InkarnationsProzessStatus.GESTOPPT
				|| inkarnationsProzess.getStatus() == InkarnationsProzessStatus.GESTARTET) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Assert.assertEquals("Status", InkarnationsProzessStatus.STARTFEHLER, inkarnationsProzess.getStatus());
		String ausgabe = inkarnationsProzess.getProzessAusgabe().toLowerCase();
		Assert.assertTrue("Fehlermeldung: " + ausgabe, ausgabe.contains("fatal exception"));
		Assert.assertEquals("Exitcode", 1, inkarnationsProzess.getLastExitCode());
	}

	@Test
	public final void testStartFehlerListener() {
		InkarnationsProzessIf inkarnationsProzess = new InkarnationsProzess();
		inkarnationsProzess.setProgramm("java");
		inkarnationsProzess.setProgrammArgumente("-invalidOption=");
		inkarnationsProzess.setInkarnationsName("Test");
		Object sync = new Object();
		status = null;
		inkarnationsProzess.addProzessListener(new InkarnationsProzessListener() {

			@Override
			public void statusChanged(InkarnationsProzessStatus neuerStatus) {
				synchronized (sync) {
					System.out.println("Neuer Status: " + neuerStatus);
					status = neuerStatus;
					sync.notify();
				}
			}
		});
		inkarnationsProzess.start();

		synchronized (sync) {
			try {
				sync.wait(5000);
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
		inkarnationsProzess
				.setProgrammArgumente("-cp " + classPath + " de.bsvrz.sys.startstopp.process.TestInkarnation");
		inkarnationsProzess.setInkarnationsName("Bubu");
		Object sync = new Object();
		status = null;

		inkarnationsProzess.addProzessListener(new InkarnationsProzessListener() {

			@Override
			public void statusChanged(InkarnationsProzessStatus neuerStatus) {
				synchronized (sync) {
					System.out.println("Neuer Status: " + neuerStatus);
					status = neuerStatus;
					sync.notify();
				}
			}
		});
		inkarnationsProzess.start();

		synchronized (sync) {
			try {
				sync.wait(6000);
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
	public final void testTerminiere() {

		Debug logger = Debug.getLogger();
		Debug.setHandlerLevel("StdErr", Level.FINE);

		
		if( Tools.isWindows()) {
			logger.warning("TODO: Test not supported for Windows-Environment");
			return;
		}
		
		InkarnationsProzessIf inkarnationsProzess = new InkarnationsProzess(logger);
		inkarnationsProzess.setProgramm("java");
		inkarnationsProzess
				.setProgrammArgumente("-cp " + classPath + " de.bsvrz.sys.startstopp.process.TestInkarnation");
		inkarnationsProzess.setInkarnationsName("Test");
		Object sync = new Object();
		status = null;

		inkarnationsProzess.addProzessListener(new InkarnationsProzessListener() {

			@Override
			public void statusChanged(InkarnationsProzessStatus neuerStatus) {
				synchronized (sync) {
					System.out.println("Neuer Status: " + neuerStatus);
					status = neuerStatus;
					sync.notify();
				}
			}
		});
		inkarnationsProzess.start();

		synchronized (sync) {
			try {
				sync.wait(6000);
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

		inkarnationsProzess.terminate();

		synchronized (sync) {
			try {
				sync.wait(2000);
			} catch (InterruptedException e) {
				Assert.fail();
			}
		}

		Assert.assertEquals("Status", InkarnationsProzessStatus.GESTOPPT, status);
	}

	@Test
	public final void testKill() {
		Debug logger = Debug.getLogger();
		Debug.setHandlerLevel("StdErr", Level.FINE);
		InkarnationsProzessIf inkarnationsProzess = new InkarnationsProzess(logger);
		inkarnationsProzess.setProgramm("java");
		inkarnationsProzess
				.setProgrammArgumente("-cp " + classPath + " de.bsvrz.sys.startstopp.process.TestInkarnation");
		inkarnationsProzess.setInkarnationsName("Test");
		Object sync = new Object();
		status = null;

		inkarnationsProzess.addProzessListener(new InkarnationsProzessListener() {

			@Override
			public void statusChanged(InkarnationsProzessStatus neuerStatus) {
				synchronized (sync) {
					System.out.println("Neuer Status: " + neuerStatus);
					status = neuerStatus;
					sync.notify();
				}
			}
		});
		inkarnationsProzess.start();

		synchronized (sync) {
			try {
				sync.wait(6000);
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

		inkarnationsProzess.kill();

		synchronized (sync) {
			try {
				sync.wait(2000);
			} catch (InterruptedException e) {
				Assert.fail();
			}
		}

		Assert.assertEquals("Status", InkarnationsProzessStatus.GESTOPPT, status);
	}

	@Test
	public final void testStartMitUmlaut() {
		InkarnationsProzessIf inkarnationsProzess = new InkarnationsProzess(Debug.getLogger());
		inkarnationsProzess.setProgramm("java");
		inkarnationsProzess
				.setProgrammArgumente("-cp " + classPath + " de.bsvrz.sys.startstopp.process.TestInkarnation -üUmlaut");
		inkarnationsProzess.setInkarnationsName("Bubu");
		Object sync = new Object();
		status = null;
		inkarnationsProzess.start();

		inkarnationsProzess.addProzessListener(new InkarnationsProzessListener() {

			@Override
			public void statusChanged(InkarnationsProzessStatus neuerStatus) {
				synchronized (sync) {
					System.out.println("Neuer Status: " + neuerStatus);
					status = neuerStatus;
					sync.notify();
				}
			}
		});

		synchronized (sync) {
			try {
				sync.wait(200000);
			} catch (InterruptedException e) {
				Assert.fail();
			}
		}

		Assert.assertEquals("Status", InkarnationsProzessStatus.GESTARTET, status);

		Assert.assertNotNull("Pid", inkarnationsProzess.getPid());
	}
}
