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

import java.util.prefs.Preferences;

import de.bsvrz.sys.startstopp.prozessvew.ProzessDaten;

/**
 * Klasse stellt Methoden zur Verfügung, mit denen eine Applikation prüfen
 * kann, ob sie bereits aktiv ist oder nicht. Die Kennzeichnung dass die Applikation
 * gestartet wurde erfolgt über Benutzereinstellungen in Windows-Registry oder 
 * XML-Dokumenten (Klasse java.util.prefs.Preferences). Hier wird die Betriebssystem PID
 * eingetragen, mit der die Applikation gestartet wurde.
 * @author Dambach Werke GmbH
 */
public class StartStoppPräferenz
{
  /**
   * Name des Rooteintrags
   */
  private final String node = "/startStopp/inkarnationen";
  
  /**
   * Preferenz Variable
   */
  private Preferences m_prefs = null; 
  
  /**
   * Prozessdaten einer Applikation 
   */
  private ProzessDaten m_prozessDaten = null;
  
  /**
   * Kennung gestartet
   */
  private final String _gestartet = "gestartet";
  
  /**
   * Kennung beendet
   */
  private final String _beendet = "beendet";

  /**
   * Konstruktor der Klasse für die StartStopp Applikation selbst
   */
  public StartStoppPräferenz ()
  {
    m_prefs = Preferences.systemRoot().node( node );
    
    m_prozessDaten = null;
  }

  /**
   * Konstruktor der Klasse für eine von StartStopp zu startende Applikation
   */
  public StartStoppPräferenz (ProzessDaten prozessDaten)
  {
    m_prefs = Preferences.systemRoot().node( node );
    
    m_prozessDaten = prozessDaten;
  }

  /**
   * Methode kennzeichnet innerhalb des Rechnersystems, dass die StartStopp Applikation
   * gestartet wurde.   
   */
  public void merkeApplikationGestartet ()
  {
    m_prefs.put( bestimmeKey(), _gestartet);
  }

  /**
   * Methode kennzeichnet innerhalb des Rechnersystems, dass die StartStopp Applikation
   * beendet wurde.   
   */
  public void loescheApplikationGestartet ()
  {
    m_prefs.remove( bestimmeKey() );
  }

  /**
   * Methode prüft, ob die StartStopp Applikation bereits läuft 
   * @return true: Applikation läuft schon, false Applikation läuft nicht
   */
  public boolean isGestartet ()
  {
    return (m_prefs.get( bestimmeKey(), _beendet ).equals( _gestartet ));
  }
  
  /**
   * Schreibt die übergeben Pid einer Inkarantion in die Preferences
   * @param pid zu merkende Pid
   */
  public void merkePid (int pid)
  {
    m_prefs.putInt(bestimmeKey(), pid);
  }
  
  /**
   * Methode gibt die in den Preferences gespeicherte Pid dieser Inkarnation zurück
   * @return die in den Preferences gespeicherte Pid, im Fehlerfall -1
   */
  public int getPid ()
  {
    return m_prefs.getInt( bestimmeKey(), -1 );
  }
  
  /**
   * Methode bestimmt den Namen des Schlüssels der für diesen Anwendungsfall verwendet wird.
   * Für die StartStopp Applikation selbst ist es der Inkarnationsname der beim Aufruf übergeben
   * wurde, für Applikationen die von StartStopp aus aufgerufen werden ist es der Prozessname
   * (wichtig die ProzessId kann hier nicht verwendet werden, da in der ProzessId der Name des
   * StartStopp Blocks enthalten ist und es möglich ist, dass die Applikation von einem anderen
   * StartStopp Block heraus aktiviert wird).
   * @return zu verwendender Key
   */
  private String bestimmeKey ()
  {
    if (m_prozessDaten == null)
      return StartStoppApp.getStartStoppInkarnationsName();
    else
      return m_prozessDaten.getName();
  }
}
