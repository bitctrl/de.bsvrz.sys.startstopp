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
 * Klasse zum Speichern der Eigenschaften der Protokolldateien
 * @author Dambach Werke GmbH
 */

public class ProtokollDatei
{
  /**
   * Id des StartStopp Blocks zu dem diese Dateidefinitionen gehören
   */
  private String m_startStoppBlockId;
  
  /**
   * Alias Bezeichnung
   */
  private String m_alias;
  
  /**
   * Name der Datei
   */
  private String m_nameDatei;
  
  /**
   * Grösse der Datei in MB
   */
  private long m_groesse;

  /**
   * Konstruktor der Klasse 
   * @param startStoppBlockId Id des StartStopp Blocks zu dem diese Dateidefinitionen gehören
   * @param alias Alias Bezeichnung
   * @param nameDatei Name der Datei
   * @param groesse Grösse der Datei in MB
   */
  public ProtokollDatei (String startStoppBlockId, String alias, String nameDatei, long groesse)
  {
    m_startStoppBlockId = startStoppBlockId;
    m_alias             = alias;
    m_nameDatei         = nameDatei;
    m_groesse           = groesse;
  }
  
  /**
   * @return liefert die Klassenvariable m_groesse zurück
   */
  public long getGroesse()
  {
    return m_groesse;
  }

  /**
   * @param groesse setzt die Klassenvariable m_groesse
   */
  public void setGroesse( long groesse )
  {
    m_groesse = groesse;
  }

  /**
   * @return liefert die Klassenvariable m_alias zurück
   */
  public String getAlias()
  {
    return m_alias;
  }

  /**
   * @param alias setzt die Klassenvariable m_alias
   */
  public void setAlias( String alias )
  {
    m_alias = alias;
  }

  /**
   * @return liefert die Klassenvariable m_nameDatei zurück
   */
  public String getNameDatei()
  {
    return m_nameDatei;
  }

  /**
   * @param nameDatei setzt die Klassenvariable m_nameDatei
   */
  public void setNameDatei( String nameDatei )
  {
    m_nameDatei = nameDatei;
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
