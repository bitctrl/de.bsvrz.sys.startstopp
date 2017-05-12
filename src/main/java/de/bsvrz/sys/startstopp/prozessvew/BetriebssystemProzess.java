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

import java.util.ArrayList;
import java.util.List;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.skriptvew.StartStoppPräferenz;

/**
 * Klasse stellt Methoden zur Verfügung die es ermöglichen zu Prüfen ob Applikationen
 * bereits auf diesem Rechner laufen bzw. diese auf Betriebssystemebene zu beenden. 
 * Das Erkennen ob ein Prozess bereits läuft wird je nach Betriebssystem unterschiedlich 
 * gehandhabt:
 * - UNIX/LINUX: es wird der Systembefehl <code>ps -ef<\code> ausgeführt. Das Ergebnis
 * dieses Befehls wird dann interpetiert.<br>
 * - Windows: unter Windows gibt es nicht unter allen Betriebssystemen einen Befehl um
 * die Prozessliste aufzulisten. Abhilfe schaffen da die "Ps Tools" von Micrososft, die
 * es ermögichen eine Prozessliste zu erstellen und Prozesse zu beenden.
 * (http://www.microsoft.com/germany/technet/sysinternals/utilities/PsTools.mspx)
 * Die Klasse erwartet, dass die Tools pslist.exe bzw. pskill.exe sich in einem Verzeichnis
 * befinden, dass über die path Variable des Betriebssystem erreichbar ist (Sinnvollerweise 
 * C:\Windows\System32).
 * Unter Java auf Windows gibt es keine Möglichkeit die Pid eines von Java aus gestarteten
 * Prozesses zu bestimmen. Daher kommt hier folgendes Vorgehen zum Zuge:
 * 1. bevor ein Prozess gestartet wird wird die aktuelle Prozessliste ausgelesen
 * (hierfür muss die Methode iniPidBestimmung() aufgerufen werden.
 * 2. Der Prozess wird gestartet
 * 3. es wird wieder die Prozessliste eingelesen und mit der ersten Prozessliste verglichen.
 * Der neu hinzugekommene Prozess muss der gestartet Prozess sein.
 * (hierfür muss die Methode bestimmePid() aufgerufen werden.
 * - Mac: Verfahren nicht installiert.<br>
 * @author Dambach Werke GmbH
 */
public class BetriebssystemProzess
{
  /**
   * Debug-Logger.
   */
  protected static final Debug m_debug = Debug.getLogger();
  /**
   * Aufruf der zu prüfenden Applikation
   */
  private String m_applikation = null;
  
  /**
   * Pid der Applikation
   */
  private int m_pid = -1;
  
  /**
   * Merker beim Windowssystem ob die Liste der Prozesse gefüllt oder ausgewertet
   * werden soll.
   */
  private boolean m_fuellen = false;
  
  /**
   * Prozessdaten des zu prüfenden Prozesses
   */
  private ProzessDaten m_prozessDaten = null;
  
  /**
   * Liste mit den aktuell bekannten Pid des betriebssystems
   */
  private List<Integer> m_windowPids = new ArrayList<Integer>();
  
  /**
   * Konstruktor der Klasse. In diesem Konstruktor wird festgestellt auf welcher 
   * Betriebssystemplattform die Applikation gestartet wurde.
   * @param prozessDaten Prozessdaten der Inkarnation
   */
  public BetriebssystemProzess (ProzessDaten prozessDaten)
  {
    m_prozessDaten = prozessDaten;
    
    if (m_prozessDaten != null)
      m_applikation  = prozessDaten.getAufruf();
  }

  /**
   * Methode prüft ob eine Applikation bereits gestartet ist. Es wird dabei nur geprüft,
   * ob eine Applikation läuft, die von einer StartStopp Applikation mit demselben
   * Inkarnationsnamen wie die jetzt laufende, gestartet wurde (siehe StartStoppApp).  
   * Hierbei werden die Informationen die in den "Benutzereinstellungen in Windows-Registry oder 
   * XML-Dokumenten" (siehe Klasse StartStoppPräferenz) gespeichert sind, ausgewertet.
   * @return true: Applikation läuft schon, sonst false
   */
  public boolean isGestartet ()
  {
    // UNIX/Linux Variante
    
    if (Tools.isUnix() || Tools.isLinux())
    {
      return (getPid() > 0);
    }

    // Windows Variante
    
//    StartStoppPräferenz sss = new StartStoppPräferenz (m_prozessDaten.getProzessId());
    StartStoppPräferenz sss = new StartStoppPräferenz (m_prozessDaten);
    
    m_pid = sss.getPid();

    return (m_pid >= 0 );
  }

  /**
   * Methode liefert die Betriebssystem PID der Inkarnation zurück. Unter Unix/Linux
   * wird hierzu der Befehl "ps -ef" ausgewertet, unter Windows das Ergebnis des letzten
   * Aufrufs der Methode "bestimmePid".
   * @return > 0 Pid, sonst Fehler
   */
  public int getPid ()
  {
    if (Tools.isUnix())
    {
      pruefeUnix();
      
      return m_pid;
    }

    if (Tools.isLinux())
    {
      pruefeLinux();
      
      return m_pid;
    }

    if (Tools.isWindows())
    {
      return m_pid;
    }

    return -1;
  }
  
  /**
   * Methode beendet die Applikation auf Betriebsysremebene. Je nach Betriebssystem werden dabei 
   * folgende Systembefehle verwendet:<br>
   * UNIX/Linux: sh kill -p PID<br>
   * Windows: pskill PID<br>
   * Mac: nicht realisiert
   */
  public void beendeProzess ()
  {
    if (Tools.isUnix() || Tools.isLinux())
    {
      if (m_pid != -1)
      {
        String befehl = "kill -9 " + m_pid;
        
        System.out.println("Befehl = " + befehl);
        Tools.ausführen (befehl);
      }
    }

    if (Tools.isWindows())
    {
      if (m_pid != -1)
      {
        String befehl = "cmd /c pskill.exe " + m_pid;
        
        Tools.ausführen (befehl);

//        StartStoppPräferenz sss = new StartStoppPräferenz (m_prozessDaten.getProzessId());
        StartStoppPräferenz sss = new StartStoppPräferenz (m_prozessDaten);
        
        sss.loescheApplikationGestartet();
      }
    }
  }
  
  /**
   * Methode prüft ob auf einem Unix Rechner eine Prozess läuft. Hierzu wird
   * der Befehl "ps -ef" ausgeführt und das Ergebnis ausgewertet.
   */
  private void pruefeUnix()
  {
    String befehl = "ps -ef";
    
    m_pid = -1;
    
    List<String> ausgabe = Tools.ausführen(befehl);

    if (ausgabe.size() > 0)
    {
      m_pid = bestimmePidUnix(ausgabe, m_prozessDaten.getAufruf());
    }
    else
    {
      System.err.println("Keine Antwort auf Befehl \"" + befehl + "\" erhalten");
    }
  }

  /**
   * Methode prüft ob auf einem Linux Rechner eine Prozess läuft. Hierzu wird
   * der Befehl "ps -ef --cols 10000" ausgeführt und das Ergebnis ausgewertet.
   * '--cols 10000' wird benötigt um die Fensterbreite zu setzen, speziell für den
   * Start im Hintergrund (startproc).
   */
  private void pruefeLinux()
  {
    String befehl = "ps -ef --cols 10000";
    
    m_pid = -1;
    
    List<String> ausgabe = Tools.ausführen(befehl);

    if (ausgabe.size() > 0)
    {
      m_pid = bestimmePidUnix(ausgabe, m_prozessDaten.getAufruf());
    }
    else
    {
      System.err.println("Keine Antwort auf Befehl \"" + befehl + "\" erhalten");
    }
  }

  /**
   * Methode bestimmt die Pid zu einem bestimmten Prozess unter Unix/Linux. Als Input dient
   * eine Liste von Strings, in der die Ausgabe des Systembefehls "ps -ef" enthalten
   * ist. 
   * @param ausgabe Ausgabe des Systembefehle "ps -ef" zeilenweise als Liste von Strings
   * @param applikation Aufruf der Applikation die geprüft werden soll
   * @return -1: Applikation nicht gefunden, > 0 Pid der Applikation
   */
  private int bestimmePidUnix (List<String> ausgabe, String applikation)
  {
    int pid = -1;
    
    // mehrfache Leerzeichen entfernen und Leerzeichen am Anfang und Ende des 
    // Strings.
    
    String applikationBereinigt = applikation.replaceAll ( " +", " " ).trim();
    applikationBereinigt = applikationBereinigt.replaceAll ( "\"", "" );
 
    for (int i=0; i<ausgabe.size(); ++i)
    {
      String zeile = ausgabe.get( i );

      // mehrfache Leerzeichen entfernen und Leerzeichen am Anfang und Ende des 
      // Strings.
      
      String zeileBereinigt = zeile.replaceAll ( " +", " " ).trim();
      zeileBereinigt = zeileBereinigt.replaceAll ( "\"", "" );

      if (zeileBereinigt.contains( applikationBereinigt ))
      {
        String[] s = zeileBereinigt.split( " " );

        String s1 = Tools.bestimmeErsteZahl( s[1] );
        if (s1 != null)
          pid = lesePid( s1 );
      }
    }
    
    return pid;
  }

  /**
   * Methode initialisiert auf Windowssystemen die Felder, die zur Bestimmung der Pid eines Prozesses benötigt 
   * werden. Unter Unix/Linux Systemen hat die Methode keine Funktionalität.
   */
  public void iniPidBestimmung ()
  {    
    if (Tools.isWindows())
    {
      m_windowPids.clear();
    
      m_fuellen = true;
    
      pruefeWindows ();
    }
  }

  /**
   * Methode bestimmt sofern vorher die Methode iniPidBestimmung() aufgerufen wurde die Pid eines
   * Prozesses auf einem Windowssystem. Unter Unix/Linux Systemen hat die Methode keine Funktionalität.
   * Wird die Methode aufgerufen ohne vorher die Methode iniPidBestimmung() aufgerufen zu haben,
   * wird eine IllegalArgumentException ausgelöst.
   * @throws IllegalArgumentException 
   */
  public void bestimmePid ()
  {
    if (Tools.isWindows())
    {
      if (m_fuellen == false)
      {
        throw (new IllegalStateException("Fehler: Methode bestimmePid() wurde aufgerufen ohne dass vorher " +
                                         "die Methode iniPidBestimmung() aufgerufen wurde !"));
      }
      m_fuellen = false;
      
      pruefeWindows ();
    }
  }

  /**
   * Methode prüft ob auf einem Windows Rechner eine Prozess läuft. Hierzu wird
   * der Befehl "pslist" ausgeführt und das Ergebnis ausgewertet.
   */
  private void pruefeWindows ( )
  {
    String befehl = "cmd /c pslist.exe ";
    
    m_pid = -1;
    
    List<String> ausgabe = Tools.ausführen (befehl);
    
    if (ausgabe.size() > 0)
      m_pid = bestimmePidWindows ( ausgabe, m_applikation );
  }

  /**
   * Methode bestimmt die Pid zu einem bestimmten Prozess unter Windows. Als Input dient
   * eine Liste von Strings, in der die Ausgabe des Systembefehls "pslist" enthalten
   * ist. 
   * @param ausgabe Ausgabe des Systembefehle "ps -ef" zeilenweise als Liste von Strings
   * @param applikation Aufruf der Applikation die geprüft werden soll
   * @return -1: Applikation nicht gefunden, > 0 Pid der Applikation
   */

  private int bestimmePidWindows( List<String> ausgabe, String applikation )
  {
    int pid = -1;

    for (int i=0; i<ausgabe.size(); ++i)
    {
      String zeile = ausgabe.get( i );
      
      // Mehrere Leerzeichen durch ein Leerzeichen ersetzen und dann die
      // Leerzeichen am Anfang und Ende entfernen
      
      String z1 = zeile.replaceAll ( " +", " " ).trim();

      // Aufsplitten in die einzelnen Elemente
      
      String[] s = z1.split( " " );
      
      if (s.length >= 2)
      {
//        System.out.println(s[0] + " - " + s[1]);
      
        String s1 = Tools.bestimmeErsteZahl( s[1] );
        
        if (s1 != null)
        {
          pid = lesePid( s1 );
  
          // Liste füllen
            
          if (m_fuellen)
          {
            if (!m_windowPids.contains( pid))
            {
              m_windowPids.add( pid );
            }
          }
              
          // Liste prüfen
              
          else
          {
            if (m_windowPids.size() > 0)
            {
              if (!s[0].contains( "cmd" ) && !s[0].contains( "pslist" ))
              {
                if (!m_windowPids.contains( pid))
                  m_pid = pid;
              }
            }
          }
          
        } // if (s1 != null)
        
      } // if (s.length >= 2)
    }

    return m_pid;
  }

  /**
   * Methode wandelt einen String in eine Zahl um
   * @param text umzuwandelnder String
   * @return Zahl
   */
  private int lesePid (String text)
  {
    int pid = -1;
    
    try
    {
      pid = Integer.parseInt( text );
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    
    return pid;
  }
}
