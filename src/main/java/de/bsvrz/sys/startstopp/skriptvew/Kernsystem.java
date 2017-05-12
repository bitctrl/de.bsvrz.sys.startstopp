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
 * Klasse die die EInstellungen für eine Kernsystemapplikation enthält
 * @author Dambach Werke GmbH
 */

public class Kernsystem
{
  /**
   * Applikationsname der zum Kernsystem gehörenden Applikation
   */
  private String m_applikation;
  
  /**
   * Wartezeit vor dem Starten der Applikation in Sekunden
   */
  private long m_wartezeit = -1;

  /**
   * Soll der Parameter '-inkarnationsname' beim Aufruf übergeben werden 
   */
  private boolean m_mitInkarnationsname = false;

  /**
   * Parameterloser Konstruktor der Klasse
   */
  public Kernsystem ()
  {
  }
  
  /**
   * Konstruktor
   * @param applikation Applikationsname
   * @param wartezeit Wartezeit
   */
  public Kernsystem (String applikation, long wartezeit)
  {
    setApplikation (applikation);
    setWartezeit   (wartezeit);
   
  }

  /**
   * Konstruktor
   * @param applikation Applikationsname
   */
  public Kernsystem( String applikation )
  {
    setApplikation (applikation);
  }

  /**
   * Methode zum Setzen der Wartezeit vor dem Starten der Applikation
   * @param wartezeit Wartezeit
   */
  private void setWartezeit( long wartezeit )
  {
    m_wartezeit = wartezeit;
  }

  /**
   * Methode zum Setzen des Applikationsnamens
   * @param applikation Appliaktionsname
   */
  private void setApplikation( String applikation )
  {
    m_applikation = applikation;
  }
  
  /**
   * Methode liefert den Applikationsname
   * @return Applikationsname
   */
  public String getApplikation ()
  {
    return m_applikation;
  }
  
  /**
   * Methode liefert die Wartezeit. Wurde keine Wartezeit definiert (Attribut ist optional) wird
   * als Wert 0 zurückgeliefert.
   * @return Wartezeit
   */
  public long getWartezeit ()
  {
    if (!isWarteZeitVersorgt())
      return 0;
    
    return m_wartezeit;  
  }
  
  /**
   * Methode prüft ob die Wartezeit vorgegeben wurde (Attribut ist optional)
   * @return Wartezeit vorgegeben
   */
  public boolean isWarteZeitVersorgt ()
  {
    return m_wartezeit != -1;
  }

  /**
   * Methode prüft ob der Parameter '-inkarnationsname=' beim Aufruf der Inkarnation mit uebergeben werden soll
   * oder nicht
   * @return Aufrufparameter übergeben
   */
  public boolean isMitInkarnationsname()
  {
    return m_mitInkarnationsname;
  }

  /**
   * Methode definiert ob der Parameter '-inkarnationsname=' beim Aufruf der Inkarnation mit uebergeben werden soll
   * oder nicht
   * @param mitInkarnationsname Aufrufparameter uebergeben
   */
  public void setMitInkarnationsname( boolean mitInkarnationsname )
  {
    m_mitInkarnationsname = mitInkarnationsname;
  }
}
