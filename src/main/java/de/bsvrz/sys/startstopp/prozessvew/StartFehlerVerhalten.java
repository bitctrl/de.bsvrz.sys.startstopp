/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contact Information:
 * Dambach-Werke GmbH
 * Elektronische Leitsysteme
 * Fritz-Minhardt-Str. 1
 * 76456 Kuppenheim
 * Phone: +49-7222-402-0
 * Fax: +49-7222-402-200
 * mailto: info@els.dambach.de
 */

package de.bsvrz.sys.startstopp.prozessvew;

import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_START_FEHLER_VERHALTEN;

/**
 * Klasse zum Realisieren der Einstellungen für das Startfehlerverhalten eines
 * Prozessen
 * @author Dambach Werke GmbH
 */
 
  public class StartFehlerVerhalten
  {
    /**
     * Option
     */
    private E_START_FEHLER_VERHALTEN m_option;
    
    /**
     * Anzahl Wiederholungen
     */
    private long m_wiederholungen;

    /**
     * @return liefert die Klassenvariable m_option zurück
     */
    public E_START_FEHLER_VERHALTEN getOption()
    {
      return m_option;
    }

    /**
     * @param option setzt die Klassenvariable m_option
     */
    public void setOption( E_START_FEHLER_VERHALTEN option )
    {
      m_option = option;
    }

    /**
     * @return liefert die Klassenvariable m_wiederholungen zurück
     */
    public long getWiederholungen()
    {
      return m_wiederholungen;
    }

    /**
     * @param wiederholungen setzt die Klassenvariable m_wiederholungen
     */
    public void setWiederholungen( long wiederholungen )
    {
      m_wiederholungen = wiederholungen;
    }
  }
  
