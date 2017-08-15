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

package de.bsvrz.sys.startstopp.skriptvew;

/**
 * Klasse zum Speichern der globalen Makros
 * @author Dambach Werke GmbH
 *
 */
public class MakroGlobal
{
  /**
   * ID des StartStopp Blocks zu dem die Makros gehören
   */
  private String m_startStoppBlockId;
  
  /**
   * Makroname
   */
  private String m_name;
  
  /**
   * Makrodefinition
   */
  private String m_wert;

  /**
   * @return liefert die Klassenvariable m_name zurück
   */
  public String getName()
  {
    return m_name;
  }

  /**
   * @param name setzt die Klassenvariable m_name
   */
  public void setName( String name )
  {
    m_name = name;
  }

  /**
   * @return liefert die Klassenvariable m_wert zurück
   */
  public String getWert()
  {
    return m_wert;
  }

  /**
   * @param wert setzt die Klassenvariable m_wert
   */
  public void setWert( String wert )
  {
    m_wert = wert;
  }

  /**
   * @return liefert die Klassenvariable m_startStoppBlockId zurück
   */
  public String getStartStoppBlockId()
  {
    return m_startStoppBlockId;
  }

  /**
   * @param startStoppBlockId setzt die Klassenvariable m_startStoppBlockId
   */
  public void setStartStoppBlockId( String startStoppBlockId )
  {
    m_startStoppBlockId = startStoppBlockId;
  }  
}
