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
   * Klasse zur Aufnahme der allgemeinen Eigenschaften des StartStopp Blocks
   * @author Dambach Werke GmbH
   */

  public class StartStoppEigenschaften
  {
    /**
     * Erzeuger des StartStopp Blocks<br>
     * 0 - StartStopp Applikation (beim Starten)<br>
     * sonst - ProzessId des Prozesses, der über den DaV diesen StartStopp Block angelegt
     * hat
     */
    private long m_absender;
    
    /**
     * Version des StartStopp Blocks
     */
    private String m_version         = null;
    
    /**
     * Versionierungsdatum
     */
    private String m_erstelltAm      = null;
    
    /**
     * Veranlasser der Versionierung
     */
    private String m_erstelltDurch   = null;
    
    /**
     * Grund der Versionierung
     */
    private String m_aenderungsGrund = null;
    
    /**
     * Konstruktor der Klasse
     * @param absender Erzeuger des StartStopp Blocks
     * @param version Version des StartStopp Blocks
     * @param erstelltAm Versionierungsdatum
     * @param erstelltDurch Veranlasser der Versionierung
     * @param aenderungsGrund Grund der Versionierung
     */
    public StartStoppEigenschaften (long absender,
                                    String version,
                                    String erstelltAm,
                                    String erstelltDurch,
                                    String aenderungsGrund)
    {
      m_absender        = absender;
      m_version         = version;
      m_erstelltAm      = erstelltAm;
      m_erstelltDurch   = erstelltDurch;
      m_aenderungsGrund = aenderungsGrund;
    }

    /**
     * @return liefert die Klassenvariable m_aenderungsGrund zurück
     */
    public String getAenderungsGrund()
    {
      return m_aenderungsGrund;
    }

    /**
     * @param aenderungsGrund setzt die Klassenvariable m_aenderungsGrund
     */
    public void setAenderungsGrund( String aenderungsGrund )
    {
      m_aenderungsGrund = aenderungsGrund;
    }

    /**
     * @return liefert die Klassenvariable m_erstelltAm zurück
     */
    public String getErstelltAm()
    {
      return m_erstelltAm;
    }

    /**
     * @param erstelltAm setzt die Klassenvariable m_erstelltAm
     */
    public void setErstelltAm( String erstelltAm )
    {
      m_erstelltAm = erstelltAm;
    }

    /**
     * @return liefert die Klassenvariable m_erstelltDurch zurück
     */
    public String getErstelltDurch()
    {
      return m_erstelltDurch;
    }

    /**
     * @param erstelltDurch setzt die Klassenvariable m_erstelltDurch
     */
    public void setErstelltDurch( String erstelltDurch )
    {
      m_erstelltDurch = erstelltDurch;
    }

    /**
     * @return liefert die Klassenvariable m_version zurück
     */
    public String getVersion()
    {
      return m_version;
    }

    /**
     * @param version setzt die Klassenvariable m_version
     */
    public void setVersion( String version )
    {
      m_version = version;
    }

    /**
     * @return liefert die Klassenvariable m_absender zurück
     */
    public long getAbsender()
    {
      return m_absender;
    }

    /**
     * @param absender setzt die Klassenvariable m_absender
     */
    public void setAbsender( long absender )
    {
      m_absender = absender;
    }
  }
