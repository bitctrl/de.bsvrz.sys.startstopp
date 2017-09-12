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

import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.bsvrz.dav.daf.util.cron.CronDefinition;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.StartArt;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;
import de.bsvrz.sys.startstopp.process.OnlineApplikation.TaskType;
import de.bsvrz.sys.startstopp.util.NamingThreadFactory;

class OnlineApplikationTimer {

	private static final int STOP_FEHLER_TIMEOUT_SEC = 30;
	private ScheduledFuture<?> currentTask;
	private ScheduledExecutorService taskExecutor;

	private OnlineApplikation onlineApplikation;
	private TaskType currentTaskType = TaskType.DEFAULT;

	OnlineApplikationTimer(OnlineApplikation onlineApplikation) {
		taskExecutor = Executors
				.newSingleThreadScheduledExecutor(new NamingThreadFactory("ApplikationTimer_" + onlineApplikation.getName()));
		this.onlineApplikation = onlineApplikation;
	}

	public void dispose() {
		clear();
		taskExecutor.shutdown();
	}

	public void initZyklusTimer() throws StartStoppException {
		
		long zyklischerStart = 0;

		StartArt startArt = onlineApplikation.getApplikation().getInkarnation().getStartArt();
		switch (startArt.getOption()) {
		case INTERVALLABSOLUT:
			zyklischerStart = new CronDefinition(startArt.getIntervall()).nextScheduledTime(System.currentTimeMillis());
			break;
		case INTERVALLRELATIV:
			zyklischerStart = ManagementFactory.getRuntimeMXBean().getStartTime();
			long intervalle = (System.currentTimeMillis() - zyklischerStart)
					/ Util.convertToWarteZeitInMsec(startArt.getIntervall());
			zyklischerStart += (intervalle + 1) * Util.convertToWarteZeitInMsec(startArt.getIntervall());
			break;
		default:
			return;
		}

		clear();

		currentTaskType = TaskType.INTERVALLTIMER;
		currentTask = taskExecutor.schedule(() -> {
			onlineApplikation.checkState(TaskType.INTERVALLTIMER);
		}, zyklischerStart - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	public long getTaskDelay(TimeUnit unit) {
		if( currentTask != null) {
			return currentTask.getDelay(unit);
		}
		return 0;
	}

	public void clear() {
		if( currentTask != null) {
			currentTask.cancel(true);
			currentTask = null;
		}
	}

	public boolean isIntervallTaskAktiv() {
		return currentTaskType == TaskType.INTERVALLTIMER && getTaskDelay(TimeUnit.MILLISECONDS) > 0;
	}

	public void initStoppFehlerTask(OnlineApplikation applikation) {
		clear();
		currentTaskType = TaskType.STOPPFEHLER;
		
		int faktor = 1;
		StoppFehlerVerhalten stoppFehlerVerhalten = applikation.getApplikation().getInkarnation().getStoppFehlerVerhalten();
		if( stoppFehlerVerhalten != null) {
			faktor += Integer.parseInt(Util.nonEmptyString(stoppFehlerVerhalten.getWiederholungen(), "0"));
		}
		currentTask = taskExecutor.schedule(() -> onlineApplikation.checkState(TaskType.STOPPFEHLER), STOP_FEHLER_TIMEOUT_SEC * faktor, TimeUnit.SECONDS);
	}
	
	public void initWarteTask(long warteZeitInMsec) {
		clear();
		if( warteZeitInMsec <= 0 ) {
			return;
		}
		
		currentTaskType = TaskType.WARTETIMER;
		currentTask = taskExecutor.schedule(() -> onlineApplikation.checkState(TaskType.WARTETIMER), warteZeitInMsec,
				TimeUnit.MILLISECONDS);
	}

	public boolean isWarteTaskAktiv() {
		return currentTaskType == TaskType.WARTETIMER && getTaskDelay(TimeUnit.MILLISECONDS) > 0;
	}

	public boolean isStoppFehlerTaskAktiv() {
		return currentTaskType == TaskType.STOPPFEHLER && getTaskDelay(TimeUnit.MILLISECONDS) > 0;
	}

}
