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

import java.net.InetAddress;
import java.net.UnknownHostException;

import de.bsvrz.sys.startstopp.skriptvew.StartStoppApp;

/**
 * Klasse realisiert den Hostrechner auf dem die StartStopp Applikation laufen soll
 * @author Dambach Werke GmbH
 */
public class HostRechner
{
  /**
   * Name des Rechners
   */
  private String m_rechnerName = null;
  
  /**
   * IP Adresse des Rechners
   */
  private String m_rechnerAdresse = null;

  /**
   * Pid des Rechner der für die Publikation der StartStopp Informationen verwendet werden soll 
   * (statt der Pid des Rechners auf dem die StartStopp Applikation läuft)
  */
  private String m_pidRechnerVorgabe = null;
  
  /**
   * IP Adresse eines Rechners die in den Ids der Inkarnationen verwendet wird. 
   * (statt der IP-Adresse des lokalen Rechners)
   */
  private static String m_rechnerAdresseVorgabe = null;

  /**
   * Konstruktor der Klasse
   */
  private HostRechner()
  {
    try
    {
      m_rechnerAdresse = InetAddress.getLocalHost().getHostAddress();
      m_rechnerName = InetAddress.getLocalHost().getHostName();
    }
    catch (UnknownHostException e)
    {
      m_rechnerAdresse   = null;
      m_rechnerName = null;
    }
  }
  
  /**
   * Liefert die einzige Instanz der Klasse
   * @return einzige Instanz der Klasse
   */
  public static HostRechner getInstanz()
  {
    return Inner.INSTANCE;
  }

  /**
   * Innere Klasse zum Sicherstellen, dass wirklich nur eine Instanz der Klasse
   * gebildet wird
   * @author Dambach Werke GmbH
   */
  public static class Inner
  {
    private static HostRechner INSTANCE = new HostRechner();
  }

  /**
   * @return liefert die Variable hostName zurück
   */
  public String getHostName()
  {
    return m_rechnerName;
  }

  /**
   * @param hostName setzt die Variable hostName
   */
  public void setHostName( String hostName )
  {
    m_rechnerName = hostName;
  }

  /**
   * @return liefert die Variable hostAdresse zurück
   */
  public String getHostAdresse()
  {
    if (m_rechnerAdresseVorgabe != null)
      return m_rechnerAdresseVorgabe;
    
    return m_rechnerAdresse;
  }

  /**
   * @param hostAdresse setzt die Variable hostAdresse
   */
  public void setHostAdresse( String hostAdresse )
  {
    m_rechnerAdresse = hostAdresse;
  }

  /**
   * Ausgeben der Klasse als String
   */
  @Override
  public String toString ()
  {
    String s = "";
    
    s += "Rechnername: " + getHostName() + "\n";
    s += "IP-Adresse: " + getHostAdresse() + "\n";
    
    return s;
  }

  /**
   * Methode zum definierten Setzen der Pid des Rechner der für die Publikation der StartStopp Informationen 
   * verwendet werden soll (statt der Pid des Rechners auf dem die StartStopp Applikation läuft)
   * @param pidRechner Pid des Rechners
   */
  public void setPidRechner( String pidRechner )
  {
    m_pidRechnerVorgabe = pidRechner;
  }
  
  /**
   * Methode liefert die Pid des Rechners der für die Publikation der StartStopp Informationen verwendet 
   * werden soll
   * @return Pid des Rechners, im Fehlerfall null
   */
  public String getPidRechner ()
  {
    if (!DaVKommunikation.getInstanz().istVerbunden())
      return null;

    if (m_pidRechnerVorgabe != null)
      if (m_pidRechnerVorgabe.equals( " " ))
        return null;

    // Pid für Testzwecke manuell vorgegeben
    
    if (m_pidRechnerVorgabe != null)
    {
      if (!DaVKommunikation.getInstanz().isPidRechnerGueltig( m_pidRechnerVorgabe ))
      {
        StartStoppApp.beendeStartStoppWegenFehler ("Konfigurationsobjekt Rechner \"" + m_pidRechnerVorgabe + "\" nicht gefunden !");
        return null;
      }

      return m_pidRechnerVorgabe;
    }
    
    // Pid aus Rechnernamen ermitteln auf dem die StartStopp Applikation läuft
    
    return DaVKommunikation.getInstanz().bestimmePidRechnerByHost( getHostName() );
  }

  /**
   * Methode zum definieren der IP Adresse eines Rechners die in den Ids der Inkarnationen verwendet wird. 
   * (statt der IP-Adresse des lokalen Rechners)
   * @param ipAdresse TCP/IP Adresse des Rechners 
   */
  public void setIpAdresse( String ipAdresse )
  {
    m_rechnerAdresse = ipAdresse;
  }
}
