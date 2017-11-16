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

import java.text.DateFormat;
import java.util.Date;

import org.junit.Test;

import de.bsvrz.dav.daf.util.cron.CronDefinition;

/*
*      Min    Std  Tag  Mon   WT
*        5      0    *    *    *    Jeden Tag um 00:05:00
*       15  14,20    1    *    *    Am 1. jeden Monats um 14:15:00 und um 20:15:00
*        0     22    *    *  1-5    An jedem Werktag (Mo-Fr) um 22:00:00
*       23  * / 2    *    *    *    Alle 2 Stunden um jeweils xx:23:00, also 00:23:00, 02:23:00, ...
*        5      4    *    *  son    Jeden Sonntag um 04:05:00
*        0      1    1   12    1    Jeden 1. Dezember UND jeden Montag im Dezember jeweils um 01:00:00
* 
*/

public class CronSyntaxTest {

	String[] cronSamples = { "5 0 * * *", "15 14,20 1 * *", "0 22 * * 1-5", "23 */2 * * *", "5 4 * * son",
			"0 1 1 12 1" };

	@Test
	public void sampleSyntax() {
		for (String sample : cronSamples) {
			listNextDate(sample);
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void falscheAnzahl() {
		listNextDate("* * * *");
	}

	@Test(expected=IllegalArgumentException.class)
	public void ungueltigeMinute() {
		listNextDate("77 * * * *");
	}

	@Test(expected=IllegalArgumentException.class)
	public void ungueltigeStunde() {
		listNextDate("0 26 * * *");
	}

	@Test(expected=IllegalArgumentException.class)
	public void ungueltigerTag() {
		listNextDate("0 0 0 * *");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void ungueltigerMonat() {
		listNextDate("0 0 * 93 *");
	}

	@Test(expected=IllegalArgumentException.class)
	public void ungueltigerWochentag() {
		listNextDate("0 0 0 0 sb");
	}

	@Test
	public void ungueltigerBereich() {
		listNextDate("0 9-2 * * *");
	}


	private void listNextDate(String sample) {
		CronDefinition definition = new CronDefinition(sample);
		long nextScheduledTime = definition.nextScheduledTime(System.currentTimeMillis());
		System.err.println(sample + ": " + DateFormat.getDateTimeInstance().format(new Date(nextScheduledTime)));
	}
	

}
