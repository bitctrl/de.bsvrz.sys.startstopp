/*
 * Segment 10 System (Sys), SWE 10.1 StartStopp API
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

package de.bsvrz.sys.startstopp.api.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import de.bsvrz.sys.startstopp.api.StartStoppException;

public final class Util {

	private Util() {
		// es werden keine Instanzen der Klasse angelegt.
	}

	
	public static Object cloneObject(Serializable src) {
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream outputStream = new ObjectOutputStream(byteStream);
			outputStream.writeObject(src);

			ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(byteStream.toByteArray()));
			return input.readObject();
		} catch (ClassNotFoundException | IOException e) {
			throw new IllegalStateException("Duplizieren von " + src + " ist nicht möglich!", e);
		}
	}

	public static String wrapText(int width, String text) {

		StringBuilder textBuffer = new StringBuilder();
		String[] parts = text.split("\\n");
		for( String part : parts ) {
			if( textBuffer.length() > 0) {
				textBuffer.append('\n');
			}
			textBuffer.append(wrapLine(width, part));
		}
		return textBuffer.toString();
	}
	
	private static String wrapLine(int width, String text) {

		int useableWidth = width - 6;
		String[] parts = text.split("\\s");
		StringBuilder lineBuffer = new StringBuilder(200);
		StringBuilder textBuffer = new StringBuilder(text.length() + 20);

		for (String part : parts) {
			if (lineBuffer.length() == 0) {
				lineBuffer.append(part);
			} else if (lineBuffer.length() + part.length() + 1 > useableWidth) {
				if (textBuffer.length() > 0) {
					textBuffer.append('\n');
				}
				textBuffer.append(lineBuffer.toString());
				lineBuffer.setLength(0);
				lineBuffer.append(part);
			} else {
				lineBuffer.append(' ');
				lineBuffer.append(part);
			}
		}

		if (lineBuffer.length() > 0) {
			if (textBuffer.length() > 0) {
				textBuffer.append('\n');
			}
			textBuffer.append(lineBuffer.toString());
		}

		return textBuffer.toString();
	}
	
	public static long convertToWarteZeitInMsec(String warteZeitStr) throws StartStoppException {
		try {
			return TimeUnit.SECONDS.toMillis(Integer.parseInt(warteZeitStr));
		} catch (NumberFormatException e) {
			throw new StartStoppException(e.getLocalizedMessage());
		}
	}

	public static String nonEmptyString(String string) {
		return nonEmptyString(string, "");
	}

	public static String nonEmptyString(String string, String defaultValue) {
		if( string == null) {
			return defaultValue;
		}
		return string;
	}


	public static String shorterString(String parameterStr, int len) {
		int strLen = parameterStr.length();
		if( strLen <= len) {
			return parameterStr;
		}
		
		return parameterStr.substring(0, len - 4) + " ...";
	}
}
