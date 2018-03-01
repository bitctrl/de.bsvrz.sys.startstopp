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

package de.bsvrz.sys.startstopp.util;

import org.eclipse.jetty.util.log.Logger;

import de.bsvrz.sys.funclib.debug.Debug;

public class JettyLogWrapper implements Logger {

	private final static Debug LOGGER = Debug.getLogger();
	private boolean debugEnabled;
	private boolean infoEnabled;

	@Override
	public String getName() {
		return "StartStopp-JettyLogger";
	}

	@Override
	public void warn(String msg, Object... args) {
		LOGGER.warning(msg, args);
	}

	@Override
	public void warn(Throwable thrown) {
		LOGGER.warning(thrown.getLocalizedMessage(), thrown);
	}

	@Override
	public void warn(String msg, Throwable thrown) {
		LOGGER.warning(msg, thrown);
	}

	@Override
	public void info(String msg, Object... args) {
		if (isInfoEnabled()) {
			LOGGER.info(msg, args);
		}
	}

	@Override
	public void info(Throwable thrown) {
		if (isInfoEnabled()) {
			LOGGER.info(thrown.getLocalizedMessage(), thrown);
		}
	}

	@Override
	public void info(String msg, Throwable thrown) {
		if (isInfoEnabled()) {
			LOGGER.info(msg, thrown);
		}
	}

	@Override
	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	@Override
	public void setDebugEnabled(boolean enabled) {
		debugEnabled = enabled;
	}

	public boolean isInfoEnabled() {
		return infoEnabled;
	}

	public void setInfoEnabled(boolean enabled) {
		infoEnabled = enabled;
	}

	@Override
	public void debug(String msg, Object... args) {
		LOGGER.fine(msg, args);
	}

	@Override
	public void debug(String msg, long value) {
		LOGGER.fine(msg, value);
	}

	@Override
	public void debug(Throwable thrown) {
		LOGGER.fine(thrown.getLocalizedMessage(), thrown);
	}

	@Override
	public void debug(String msg, Throwable thrown) {
		LOGGER.fine(msg, thrown);
	}

	@Override
	public Logger getLogger(String name) {
		return this;
	}

	@Override
	public void ignore(Throwable ignored) {
		// ignore
	}
}
