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

import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_NEUSTART;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_STARTART;

/**
 * Klasse zum Realisieren der Einstellungen für die Startart eines
 * Prozessen
 * @author Dambach Werke GmbH
 */

  public class StartArt
  {
    /**
     * Option
     */
    private E_STARTART m_option;
    
    /**
     * Neustart
     */
    private E_NEUSTART m_neuStart;
    
    /**
     * Intervallzeit
     */
    private String intervallZeit;

    /**
     * @return liefert die Klassenvariable m_intervallZeit zurück
     */
    public String getIntervallZeit()
    {
      return intervallZeit;
    }

    /**
     * @param intervallZeit setzt die Klassenvariable m_intervallZeit
     */
    public void setIntervallZeit( String intervallZeit )
    {
      this.intervallZeit = intervallZeit;
    }

    /**
     * @return liefert die Klassenvariable m_neuStart zurück
     */
    public E_NEUSTART getNeuStart()
    {
      return m_neuStart;
    }

    /**
     * @param neuStart setzt die Klassenvariable m_neuStart
     */
    public void setNeuStart( E_NEUSTART neuStart )
    {
      m_neuStart = neuStart;
    }

    /**
     * @return liefert die Klassenvariable m_option zurück
     */
    public E_STARTART getOption()
    {
      return m_option;
    }

    /**
     * @param option setzt die Klassenvariable m_option
     */
    public void setOption( E_STARTART option )
    {
      m_option = option;
    }

  }
