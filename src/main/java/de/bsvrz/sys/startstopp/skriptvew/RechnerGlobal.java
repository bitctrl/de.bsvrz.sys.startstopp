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
 * Klasse zum Speichern der Eigenschaften der globalen Rechner
 * @author Dambach Werke GmbH
 */

public class RechnerGlobal
{
  /**
   * Id des StartStopp Blocks zu dem diese Rechnerdefinitionen gehören
   */
  private String m_startStoppBlockId;
  
  /**
   * Alias des Rechners
   */
  private String m_alias;
  
  /**
   * tatsächliche TCP/IP Adresse des Rechners
   */
  private String m_tcpAdresse;

  /**
   * Konstruktor der Klasse
   * @param startStoppBlockId Id des StartStopp Blocks zu dem diese Definition gehört
   * @param alias Name des Rechners
   * @param tcpAdresse IP-Adresse des Rechners
   */
  
  public RechnerGlobal (String startStoppBlockId, String alias, String tcpAdresse)
  {
    m_startStoppBlockId = startStoppBlockId;
    m_alias             = alias;
    m_tcpAdresse        = tcpAdresse;
  }
  
  /**
   * @return liefert die Klassenvariable m_alias zurück
   */
  public String getAlias()
  {
    return m_alias;
  }

  /**
   * @param rechnerGlobal setzt die Klassenvariable m_alias
   */
  public void setAlias( String rechnerGlobal )
  {
    m_alias = rechnerGlobal;
  }

  /**
   * @return liefert die Klassenvariable m_tcpAdresse zurück
   */
  public String getTcpAdresse()
  {
    return m_tcpAdresse;
  }

  /**
   * @param tcpAdresse setzt die Klassenvariable m_tcpAdresse
   */
  public void setTcpAdresse( String tcpAdresse )
  {
    m_tcpAdresse = tcpAdresse;
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
