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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestInkarnationsProzess {

	private static String classPath;

	private Object lock;
	private OSApplikation process;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String startPath = new File(System.getProperty("user.dir")).toURI().getPath();
		classPath = TestInkarnation.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		classPath = classPath.replace(startPath, "");
	}

	@Before
	public void setupTest() {
		lock = new Object();
		process = new OSApplikation();
	}

	@Test(timeout = 10000)
	public final void testStartFehlerNoSuchFileOrDirecory() throws InterruptedException {
		process.setProgramm("bubu");
		process.setInkarnationsName("testStartFehlerNoSuchFileOrDirecory");
		process.addStatusHandler((newStatus) -> {
			if (newStatus == OSApplikationStatus.STARTFEHLER)
				triggerLock();
		});

		process.start();
		waitForLock();

		Assert.assertEquals("Status", OSApplikationStatus.STARTFEHLER, process.getStatus());
		Assert.assertTrue("Startfehler", process.getStartFehler().contains("error=2"));
	}

	@Test(timeout = 10000)
	public final void testStartFehlerMainClassNotFound() throws InterruptedException {
		process.setProgramm("java bubu");
		process.setInkarnationsName("testStartFehlerMainClassNotFound");
		process.addStatusHandler((newStatus) -> {
			if (newStatus == OSApplikationStatus.STARTFEHLER)
				triggerLock();
		});
		process.start();
		waitForLock();

		Assert.assertEquals("Status", OSApplikationStatus.STARTFEHLER, process.getStatus());
		String ausgabe = process.getProzessAusgabe().toLowerCase();
		System.err.println("Ausgabe: " + ausgabe);
		Assert.assertTrue("Fehlermeldung: " + ausgabe,
				ausgabe.contains("hauptklasse") || ausgabe.contains("mainclass"));
		Assert.assertEquals("Exitcode", 1, process.getLastExitCode());
	}

	@Test(timeout = 10000)
	public final void testStartFehlerInvalidOption() throws InterruptedException {
		process.setProgramm("java");
		process.setProgrammArgumente("-invalidOption=");
		process.setInkarnationsName("testStartFehlerInvalidOption");
		process.addStatusHandler((newStatus) -> {
			if (newStatus == OSApplikationStatus.STARTFEHLER)
				triggerLock();
		});

		process.start();

		waitForLock();

		Assert.assertEquals("Status", OSApplikationStatus.STARTFEHLER, process.getStatus());
		String ausgabe = process.getProzessAusgabe().toLowerCase();
		Assert.assertTrue("Fehlermeldung: " + ausgabe, ausgabe.contains("fatal exception"));
		Assert.assertEquals("Exitcode", 1, process.getLastExitCode());
	}

	@Test(timeout = 10000)
	public final void testStartFehlerListener() throws InterruptedException {

		List<OSApplikationStatus> empfangen = new ArrayList<>();
		OSApplikationStatus[] erwartet = { OSApplikationStatus.GESTARTET,
				OSApplikationStatus.STARTFEHLER };

		process.setProgramm("java");
		process.setProgrammArgumente("-invalidOption=");
		process.setInkarnationsName("testStartFehlerListener");
		process.addStatusHandler((newStatus) -> {
			empfangen.add(newStatus);
			if (empfangen.size() >= erwartet.length) {
				triggerLock();
			}
		});

		process.start();
		waitForLock();

		for (int idx = 0; idx < erwartet.length; idx++) {
			Assert.assertEquals("Unerwarteter Status", erwartet[idx], empfangen.get(idx));
		}
	}

	@Test(timeout = 20000)
	public final void testStart() throws InterruptedException {

		List<OSApplikationStatus> empfangen = new ArrayList<>();
		OSApplikationStatus[] erwartet = { OSApplikationStatus.GESTARTET,
				OSApplikationStatus.GESTOPPT };

		process.setProgramm("java");
		process.setProgrammArgumente("-cp " + classPath + " de.bsvrz.sys.startstopp.process.TestInkarnation");
		process.setInkarnationsName("testStart");

		process.addStatusHandler((newStatus) -> {
			empfangen.add(newStatus);
			if (empfangen.size() >= erwartet.length) {
				triggerLock();
			}
		});

		process.start();

		waitForLock();
		for (int idx = 0; idx < erwartet.length; idx++) {
			Assert.assertEquals("Unerwarteter Status", erwartet[idx], empfangen.get(idx));
		}
	}

	@Test(timeout = 20000)
	public final void testTerminiere() throws InterruptedException {

		if (!process.terminateSupported()) {
			System.err.println("TODO: Test not supported for this environment");
			return;
		}

		process.setProgramm("java");
		process.setProgrammArgumente("-cp " + classPath + " de.bsvrz.sys.startstopp.process.TestInkarnation");
		process.setInkarnationsName("testTerminiere");

		process.addStatusHandler((neuerStatus)->triggerLock());

		process.start();
		waitForLock();

		Assert.assertEquals("Status", OSApplikationStatus.GESTARTET, process.getStatus());

		TimeUnit.SECONDS.sleep(OSApplikation.STARTFEHLER_LAUFZEIT_ERKENNUNG_IN_SEC);
		process.terminate();
		waitForLock();

		Assert.assertEquals("Status", OSApplikationStatus.GESTOPPT, process.getStatus());
	}

	@Test(timeout = 20000)
	public final void testKill() throws InterruptedException {
		process.setProgramm("java");
		process.setProgrammArgumente("-cp " + classPath + " de.bsvrz.sys.startstopp.process.TestInkarnation");
		process.setInkarnationsName("testKill");

		process.addStatusHandler((neuerStatus) -> triggerLock());

		process.start();
		waitForLock();

		Assert.assertEquals("Status", OSApplikationStatus.GESTARTET, process.getStatus());

		TimeUnit.SECONDS.sleep(OSApplikation.STARTFEHLER_LAUFZEIT_ERKENNUNG_IN_SEC);
		process.kill();
		waitForLock();

		Assert.assertEquals("Status", OSApplikationStatus.GESTOPPT, process.getStatus());
	}

	@Test
	public final void testStartMitUmlaut() throws InterruptedException {

		process.setProgramm("java");
		process.setProgrammArgumente("-cp " + classPath + " de.bsvrz.sys.startstopp.process.TestInkarnation -üUmlaut");
		process.setInkarnationsName("testStartMitUmlaut");
		process.addStatusHandler((neuerStatus) -> triggerLock());

		process.start();
		waitForLock();

		Assert.assertEquals("Status", OSApplikationStatus.GESTARTET, process.getStatus());
		Assert.assertNotNull("Pid", process.getPid());
	}

	private void triggerLock() {
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	private void waitForLock() throws InterruptedException {
		synchronized (lock) {
			lock.wait();
		}
	}
}
