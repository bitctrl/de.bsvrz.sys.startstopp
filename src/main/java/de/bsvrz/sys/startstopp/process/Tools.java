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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;

import com.sun.jna.platform.win32.Kernel32;

/**
 * Tools f&uuml;r die Inkarnationsprozesse.
 * 
 * @author BitCtrl Systems GmbH, Gieseler
 * @version $Id: $
 *
 */
public class Tools {
	/**
	 * Stellt fest, ob es sich um ein Windows System handelt.
	 * 
	 * @return <code>true</code> bei einem Windows System, sonst <code>false</code>
	 */
	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}

	/**
	 * Stellt fest, ob es sich um ein Unix System handelt.
	 * 
	 * @return <code>true</code> bei einem Unix System, sonst <code>false</code>
	 */
	public static boolean isUnix() {
		return System.getProperty("os.name").toLowerCase().contains("unix");
	}

	/**
	 * Stellt fest, ob es sich um ein Linux System handelt.
	 * 
	 * @return <code>true</code> bei einem Linux System, sonst <code>false</code>
	 */
	public static boolean isLinux() {
		return System.getProperty("os.name").toLowerCase().contains("linux");
	}

	/**
	 * Stellt fest, ob es sich um ein Mac System handelt.
	 * 
	 * @return <code>true</code> bei einem Mac System, sonst <code>false</code>
	 */
	public static boolean isMac() {
		return System.getProperty("os.name").toLowerCase().contains("mac");
	}

	public static ProcessInfo findProcess(final String cmdLine) {

		List<ProcessInfo> processesList = JProcesses.get().fastMode().listProcesses();

		for (final ProcessInfo processInfo : processesList) {
			if (isMatchingCommandLine(cmdLine, processInfo.getCommand())) {
				return processInfo;
			}
		}

		return null;
	}
	
	public static boolean isMatchingCommandLine(final String inkCmdLine, final String proccmdLine) {
		
		if( StringUtils.isAsciiPrintable(inkCmdLine)) {
			return proccmdLine.contains(inkCmdLine);
		}
		
		String buildRegexp = buildRegexp(inkCmdLine);
		
		Matcher matcher = Pattern.compile(buildRegexp).matcher( proccmdLine );
		return matcher.find();
	}

	private static String buildRegexp(String s) {
		StringBuilder rstring = new StringBuilder();
		
	    for (int i = 0; i < s.length(); i++) { 
	        if (s.charAt(i) > 127) { 
	            rstring.append(".+");
	        } else {
	        	rstring.append(s.charAt(i));
	        }
	    }
	        
	    return rstring.toString();
	}
	
	/**
	 * Send CTRL-C to the process using a given PID
	 * 
	 * @param processID
	 */
	public static int terminateWindowsProzess(Integer pid) {
		if(!isWindows()) {
			throw new IllegalStateException("Das ist kein Windows-System");
		}
		
		if(!Kernel32.INSTANCE.AttachConsole(pid)) {
			return Kernel32.INSTANCE.GetLastError();
		}

		if(!Kernel32.INSTANCE.GenerateConsoleCtrlEvent(Kernel32.CTRL_C_EVENT, 0)) {
			return Kernel32.INSTANCE.GetLastError();
		}
		
		return 0;
	}
}
