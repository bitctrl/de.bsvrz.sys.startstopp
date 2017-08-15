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
 * Klasse zum Speichern der Daten der einzelne StartStopp Versionen
 * @author Dambach Werke GmbH
 */
public class HistoryData extends StartStoppEigenschaften
{
  /**
   * Hashwert der Datei
   */
  private String m_hashwert = null;
 
  /**
   * Konstruktor der Klasse 
   * @param version Version des StartStopp Blocks
   * @param erstelltAm Versionierungsdatum
   * @param erstelltDurch Veranlasser der Versionierung
   * @param aenderungsGrund Grund der Versionierung
   * @param hashwert Hashwert der Datei
   */
  public HistoryData (String version, String erstelltAm, String erstelltDurch, String aenderungsGrund, String hashwert)
  {
    super (0, version, erstelltAm, erstelltDurch, aenderungsGrund);

    m_hashwert = hashwert;
  }

  /**
   * @return liefert die Klassenvariable m_hashwert zurück
   */
  public String getHashwert()
  {
    return m_hashwert;
  }

  /**
   * @param hashwert setzt die Klassenvariable m_hashwert
   */
  public void setHashwert( String hashwert )
  {
    m_hashwert = hashwert;
  }
}
