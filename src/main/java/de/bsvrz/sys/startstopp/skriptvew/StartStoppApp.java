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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.jdom.Element;

import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.buv.NutzerSchnittstelle;
import de.bsvrz.sys.startstopp.prozessvew.HostRechner;
import de.bsvrz.sys.startstopp.prozessvew.Logbuch;
import de.bsvrz.sys.startstopp.prozessvew.Memory;
import de.bsvrz.sys.startstopp.prozessvew.ProzessVerwaltung;

/**
 * Hauptklasse der StartStopp Applikation. Als Aufrufparameter sind erforderlich:
 * -startStoppKonfiguration: Verzeichnis in dem sich eine Datei StartStopp.xml befindet
 * oder in das eine Datei "hineinversioniert" werden soll<br>
 * - verisonieren: (optional) Name (inklusive Pfad) einer XML-Datei die als neue StartStopp Datei in das
 * im Parameter "-startStoppKonfiguration" angegebenen Verzeichnis versioniert werden soll.<br>
 * -port: (optional) Portnummer für die Benutzeroberfläche via Telnet. Dieser Parameter ist optional. Wird 
 * kein Port übergeben, so wird keine Telnetverbindung gestartet<br>  
 * -inkarnationsName: (optional) Inkarantionsname der StartStoppApplikation, nur notwendig, wenn auf
 * einem Rechner mehrere StartStopp Applikation laufen<br>
 * -reset=true: (optional) Startet StartStopp auch dann, wenn der Merker, dass schon eine StartStopp
 * Applikation mit diesem Inkarantionsnamen läuft noch gesetzt ist.<br> 
 * -ausgabe=true: (optional) Die Standardausgaben der gestarteten Applikationen werden zusätzlich an
 * der Konsole von StartStopp ausgegeben.<br>
 * -simVariante: (optional) Simulationsvariante unter der die Applikation dieser StartStopp Konfiguration
 * gestartet werden sollen. Ohne Parameter: Simulationsvariante 0<br>
 * -urlUmsetzen=false: URL-Umsetzung in Makros ausschalten (Grundeinstellung ist eingeschaltet)<br>
 * -rechner: (optional) Pid des Rechners unter dem die StartStopp Informationen publiziert werden. 
 * Ohne Parameter: StartStopp Applikation publiziert ihre Informationen unter der Pid des Rechners, auf
 * den die StartStopp Applikation gestartet wurde. Sonderfall: wird als Rechner ein Leerzeichen (" ") übergeben,
 * werden keine Rechner spezifischen Attributgruppen versand.<br>
 * -adresse: (optional) Adresse des Rechners die in den Ids der Inkarnationen verwendet wird. 
 * Ohne Parameter: StartStopp Applikation verwendet die TCP/IP Adresse des Rechners, auf
 * den die StartStopp Applikation gestartet wurde.<br>
 * 
 * @author Dambach Werke GmbH
 */
public class StartStoppApp
{
  /**
   * Debug
   */
  private static Debug logger = null;
  
  /**
   * Verzeichnis in der sich eine StartStopp Datei befindet
   */
  private static String m_startStoppKonfiguration = null;
  
  /**
   * Datei die versioniert werden soll
   */
  private static String m_versionieren = null;
  
  /**
   * Port für Telnet Verbindung
   */
  private static String m_port = null;
  
  /**
   * Singleton Instanz für Klasse ProzessVerwaltung
   */
  private static ProzessVerwaltung m_prozessVerwaltung = null;

  /**
   * Singleton Instanz für Klasse StartStoppVerwaltung
   */
  private static StartStoppVerwaltung m_startStoppVerwaltung = null;
  
  /**
   * Singleton Instanz für Klasse NutzerSchnittstelle 
   */
  private static NutzerSchnittstelle m_nutzerSchnittstelle = null;

  /**
   * Instanz der Klasse zum Verhindern eines mehrfachen Starts der StartStopp Applikation 
   */
  private static StartStoppPräferenz m_startStoppSperren = null;
  
  /**
   * IP-Adresse des lokalen Rechners
   */
  private static String m_rechnerAdresse = null;
  
  /**
   * Name des lokalen Rechners
   */
  private static String m_rechnerName    = null;
  
  /**
   * Pid eines Rechners der für die Publizierung der StartStopp Informationen verwendet werden soll
   * (statt der PID des lokalen Rechners)
   */
  private static String m_pidRechner = null;
 
  /**
   * IP Adresse eines Rechners die in den Ids der Inkarnationen verwendet wird. 
   * (statt der IP-Adresse des lokalen Rechners)
   */
  private static String m_ipAdresse = null;
  
  /**
   * Inkarnationsname der StartStopp Applikation
   */
  private static String m_startStoppInkarnationsName = null;

  /**
   * Simulationsvariante unter der die StartStopp Applikation die Applikationen der
   * startStopp.xml Datei starten soll.
   */
  private static String m_simVariante    = null;

  /**
   * Soll Kennzeichen das StartStopp schon läuft zurückgesetzt werden
   */
  private static boolean m_reset = false;
  
  /**
   * Sollen die Standardausgaben der gestarteten Programme zusätzlich an der Konsole ausgegeben
   * werden oder nicht
   */
  private static boolean m_ausgabe = false;

  /**
   * Sollen die Fehlerausgaben der gestarteten Programme zusätzlich an der Konsole ausgegeben
   * werden oder nicht
   */
  private static boolean m_fehlerAusgabe = false;

  /**
   * Die ApplikationsKennung wird hier gespeichert. Sie besteht aus den Aufrufargumenten, dem Klassennamen (wie beim
   * Debug) und der Pid des lokalen Verantwortlichen.
   */
  public static StringBuilder _applicationLabel;
  
  /**
   * Merker, dass StartStopp beendet wird
   */
  private static boolean m_beendeStartStopp;
  
  /**
   * Sollen URLs in Makros in File-Pfade umgesetzt werden oder nicht, default ja.
   */
  private static boolean m_urlUmsetzen = true;
  
  /**
   * Soll die Anzahl der Nachstarts in der Übersicht ausgegeben werden, default nein.
   */
  private static boolean m_ausgabeNachstarts = false;
  
  /**
   * Soll der letzte Startzeitpunkt in der Übersicht ausgegeben werden, default nein.
   */
  private static boolean m_ausgabeLetzteStartzeit = false;
  
  /**
   * Formatierung der Ausgabe in der Übersicht, default keine.
   */
  private static int m_ausgabeFormatierung = 0; // keine Formatierung
 
  /**
   * Wartezeit in Sekunden vor der ersten Anmeldung am DAV wg. Timeout (10 Sekunden) bei System mit Rechteverwaltung, default 10 Sekunden.
   */
  private static int m_wartezeitErsteAnmeldung = 10; // 10 Sekunden
  
  /**
   * Wartezeit in Sekunden zwischen den Anmeldungsversuchen am DAV wg. Timeout (10 Sekunden) bei System mit Rechteverwaltung, default 10 Sekunden.
   */
  private static int m_wartezeitZwischenAnmeldungen = 10; // 10 Sekunden
  
  /**
   * Methode zum Einstellen eines Testbetriebs. Dieser Testbetrieb kann z.B. für JUNIT Test gesetzt werden.
   * In diesem Testbetrieb werden im Moment folgende Aktivitäten (nicht) durchgeführt:<br>
   * - StartStopp Anfrage Variante 1: Die Prozesse werden auch bei Aktion 'starten' nicht gestartet.
   */
  private static boolean m_testBetrieb = false;
  
  /**
   * Hauptprogramm der StartStopp Applikation
   * @param args übergebene Aufrufparameter
   * -startStoppKonfiguration: Verzeichnis in dem sich eine Datei StartStopp.xml befindet
   * oder in das eine Datei "hineinversioniert" werden soll<br>
   * - verisonieren: Name (inklusive Pfad) einer XML-Datei die als neue StartStopp Datei in das
   * im Parameter "-startStoppKonfiguration" angegebenen Verzeichnis versioniert werden soll.<br>
   * Dieser Parameter ist optional.
   * -port: Portnummer für die Benutzeroberfläche via Telnet. Dieser Parameter ist optional. Wird 
   * kein Port übergeben, so wird keine Telnetverbindung gestartet<br>  
   */
  public static void main( String[] args )
  {
    String buffer = null;

    //--------------------------------------------------------------------------------
    // Debug-Logger initialisieren
    //--------------------------------------------------------------------------------

    final String[] initialArgs = new String[args.length];
    System.arraycopy(args, 0, initialArgs, 0, args.length);
    final ArgumentList argumentList = new ArgumentList(initialArgs);
    Debug.init("StartStoppApp", argumentList);
    
    logger = Debug.getLogger();
    
    //--------------------------------------------------------------------------------
    // Allgemeine Informationen
    //--------------------------------------------------------------------------------

    String debugAusgabe = "";

    debugAusgabe += System.getProperty("os.name") + "\n";
    
    long frei = (new Memory()).getFreierSpeicher();
    
    if (frei == -1)
      debugAusgabe += "Freier Speicher: nicht ermittelbar" + "\n";
    else
      debugAusgabe += "Freier Speicher: " + frei + " Bytes" + "\n";

    //--------------------------------------------------------------------------------
    // Auswerten der Aufrufparameter
    //--------------------------------------------------------------------------------
    
    m_versionieren = null;
    
    // Default Inkarnationsname
    
    m_startStoppInkarnationsName = "StartStopp";

    for (int i=0; i<args.length; ++i)
    {
      String s[] = args[i].split( "=" );
      
      if (s.length == 2)
      {
        if (s[0].equalsIgnoreCase( "-startStoppKonfiguration" ))
          m_startStoppKonfiguration = s [1];

        if (s[0].equalsIgnoreCase( "-versionieren" ))
          m_versionieren = s [1];

        if (s[0].equalsIgnoreCase( "-port" ))
          m_port = s [1];

        if (s[0].equalsIgnoreCase( "-rechner" ))
        {
          m_pidRechner = s [1];
          HostRechner.getInstanz().setPidRechner (m_pidRechner);
        }

        if (s[0].equalsIgnoreCase( "-adresse" ))
        {
          m_ipAdresse = s [1];
          HostRechner.getInstanz().setIpAdresse (m_ipAdresse);
        }

        if (s[0].equalsIgnoreCase( "-inkarnationsName" ))
          m_startStoppInkarnationsName = s [1].replaceAll( " ", "_" );

        if (s[0].equalsIgnoreCase( "-simVariante" ))
          m_simVariante = s [1];

        if (s[0].equalsIgnoreCase( "-reset" ))
          if (s[1].equalsIgnoreCase( "true" ))
            m_reset = true;

        if (s[0].equalsIgnoreCase( "-ausgabe" ))
          if (s[1].equalsIgnoreCase( "true" ))
            m_ausgabe = true;

        if (s[0].equalsIgnoreCase( "-fehlerAusgabe" ))
          if (s[1].equalsIgnoreCase( "true" ))
            m_fehlerAusgabe = true;

        if (s[0].equalsIgnoreCase( "-urlUmsetzen" ))
          if (s[1].equalsIgnoreCase("false"))
            m_urlUmsetzen = false;

        if (s[0].equalsIgnoreCase( "-ausgabeNachstarts" ))
        {
          if (s[1].equalsIgnoreCase( "true" ))
          {
            m_ausgabeNachstarts = true;
            m_ausgabeFormatierung = 30;
          }
        }

        if (s[0].equalsIgnoreCase( "-ausgabeLetzteStartzeit" ))
        {
          if (s[1].equalsIgnoreCase( "true" ))
          {
            m_ausgabeLetzteStartzeit = true;
            m_ausgabeFormatierung = 30;
          }
        }
        
        if (s[0].equalsIgnoreCase( "-ausgabeErweitert" ))
        {
          if (s[1].equalsIgnoreCase( "true" ))
          {
            m_ausgabeNachstarts = true;
            m_ausgabeLetzteStartzeit = true;
            m_ausgabeFormatierung = 30;
          }
        }
        
        if (s[0].equalsIgnoreCase( "-ausgabeFormatierung" ))
        {
          try
          {
            m_ausgabeFormatierung = Integer.parseInt(s[1]);
          }
          catch (NumberFormatException e)
          {
          }
        }

        if (s[0].equalsIgnoreCase( "-wartezeitErsteAnmeldung" ))
        {
          try
          {
            m_wartezeitErsteAnmeldung = Integer.parseInt(s[1]);
          }
          catch (NumberFormatException e)
          {
          }
        }

        if (s[0].equalsIgnoreCase( "-wartezeitZwischenAnmeldungen" ))
        {
          try
          {
            m_wartezeitZwischenAnmeldungen = Integer.parseInt(s[1]);
          }
          catch (NumberFormatException e)
          {
          }
        }
      }
    }
    
    debugAusgabe += "-inkarnationsName = " + m_startStoppInkarnationsName + "\n";
    debugAusgabe += "-startStoppKonfiguration = " + m_startStoppKonfiguration + "\n";
    debugAusgabe += "-versionieren = " + m_versionieren + "\n";
    debugAusgabe += "-port = " + m_port + "\n";
    debugAusgabe += "-reset = " + m_reset + "\n";

    // Plausibilitätsprüfung
    
    if (m_startStoppKonfiguration == null)
    {
      beendeStartStoppWegenFehler ("Aufrufparameter \"-startStoppKonfiguration\" fehlerhaft !");
    }

    File directory = new File (m_startStoppKonfiguration);
    if (!directory.exists())
    {
      beendeStartStoppWegenFehler ("Verzeichnis \"" + m_startStoppKonfiguration + "\" nicht gefunden !");
    }

    _applicationLabel = createApplicationLabel( args );

    //--------------------------------------------------------------------------------
    // Bestimmen der lokalen IP-Adresse und des lokalen Rechnernamens 
    //--------------------------------------------------------------------------------
    
    m_rechnerName    = HostRechner.getInstanz().getHostName();
    m_rechnerAdresse = HostRechner.getInstanz().getHostAdresse();
    
    if (m_versionieren == null)
    {
      debugAusgabe += HostRechner.getInstanz().toString();
      logger.info( debugAusgabe );
    }
    else
      System.out.println(debugAusgabe);

    //--------------------------------------------------------------------------------
    // Logbuch
    //--------------------------------------------------------------------------------

    Logbuch l = Logbuch.getInstanz();
    
    if (m_versionieren == null)
    {
      String s = "";
      s += "Systemstart auf Rechner " + m_rechnerName + "\n";
      s += "----------------------------------------------------------------------\n\n";
      logger.info( s );
    }

    //--------------------------------------------------------------------------------
    // Prüfen ob StartStopp Applikation schon läuft
    //--------------------------------------------------------------------------------

    m_startStoppSperren = new StartStoppPräferenz ();

    if (m_reset)
      m_startStoppSperren.loescheApplikationGestartet();
      
    if (m_startStoppSperren.isGestartet())
    {
      System.err.println("StartStopp Applikation läuft bereits !\r\n" +
                         "Sollte die StartStopp Applikation nicht aktiv sein, so muss die Applikation " +
                         "mit dem Aufrufparameter \"-reset=true\" gestartet werden.");
      System.exit( 0 );
    }

    m_startStoppSperren.merkeApplikationGestartet();
    
    //--------------------------------------------------------------------------------
    // Bisherige StartStoppHistorie einlesen
    //--------------------------------------------------------------------------------

    StartStoppHistorie history = StartStoppHistorie.getInstanz();
    
    //--------------------------------------------------------------------------------
    // Plausibilitätsprüfung
    //--------------------------------------------------------------------------------

    String startStoppDatei = null;
    
    boolean startStoppDateiVorhanden = false;
    boolean versionierenDateiVorhanden = false;
    
    if (m_startStoppKonfiguration == null)
    {
      StartStoppApp.beendeStartStoppWegenFehler ("Aufrufparameter \"-startStoppKonfiguration\" fehlerhaft !");
    }
    else
    { 
      // Prüfen ob in dem angegebenen Verzeichnis eine StartStopp Datei
      // vorhanden ist.

      startStoppDatei = m_startStoppKonfiguration + "/" + Versionierung._startStoppDatei;
      
      startStoppDateiVorhanden = (new File (startStoppDatei)).exists();
    }

    if (m_versionieren == null)
    {
      if (!startStoppDateiVorhanden)
      { 
        StartStoppApp.beendeStartStoppWegenFehler( "keine StartStopp Datei \"" + startStoppDatei + "\" gefunden  !");
      }
    }
    else
    {
      versionierenDateiVorhanden = (new File (m_versionieren)).exists();
      
      if (!versionierenDateiVorhanden)
      {
        StartStoppApp.beendeStartStoppWegenFehler ("Versionierungsdatei \"" + m_versionieren + "\" nicht gefunden !");
      }
    }

    //--------------------------------------------------------------------------------
    // Versionierung
    //--------------------------------------------------------------------------------
    
    if (m_versionieren != null)
    {
      history.leseHistory( false );
      
      Versionierung v = new Versionierung (m_versionieren);
      if (v.isFehler())
      {
        buffer = "Bei der Versionierung trat ein Fehler auf:";
        
        System.err.println(buffer);
        System.err.println(v.getFehlerText());
        
//        l.schreibe( buffer );
//        l.schreibe( v.getFehlerText() );
      }
      else
      {
        buffer = "Versionierung durchgeführt !";
        
        System.out.println(buffer);
        
//        l.schreibe( buffer );
      }

      m_startStoppSperren.loescheApplikationGestartet();

    } // if (m_versionieren != null)
    
    //--------------------------------------------------------------------------------
    // StartStopp Applikation starten
    //--------------------------------------------------------------------------------
    
    if (m_versionieren == null)
    {
      history.leseHistory ( true );
      
      //--------------------------------------------------------------------------------
      // Prüfen ob Datei existiert und der DTD entspricht
      //--------------------------------------------------------------------------------
      
      Element root = XMLTools.leseRootElement( startStoppDatei );
      
      if (root == null)
      {
        StartStoppApp.beendeStartStoppWegenFehler ("Datei \"" + startStoppDatei + "\" fehlerhaft !");
      }
      
      //--------------------------------------------------------------------------------
      // Prüfen ob Datei mit StartStoppHistorie übereinstimmt
      //--------------------------------------------------------------------------------

      boolean mitPruefung = true;
      
      if (mitPruefung)
      {
        String hashwert = history.getAktuellerHashwert();
        if (hashwert == null)
        {
          StartStoppApp.beendeStartStoppWegenFehler ("Historydatei nicht gefunden ! Bitte Versionierung durchführen !");
        }

        String aktuellerHashwert = Hashwert.berechneHashwert( "MD5", startStoppDatei );
        
        if (hashwert.equals( aktuellerHashwert ))
        {
          logger.info("Hashwert stimmt überein");
        }
        else
        {
          StartStoppApp.beendeStartStoppWegenFehler ("Hashwert stimmt nicht überein ! Bitte Versionierung durchführen !");
        }
      }

      //--------------------------------------------------------------------------------
      // Anlegen der Singelton Instanzen
      //--------------------------------------------------------------------------------
      
      m_prozessVerwaltung    = ProzessVerwaltung.getInstanz();
      m_startStoppVerwaltung = StartStoppVerwaltung.getInstanz();
  
      //--------------------------------------------------------------------------------
      // Skriptverwaltung starten
      //--------------------------------------------------------------------------------

      long absender = 0l;
      long simulationsvariante = 0l;
      
      if (m_simVariante != null)
      {
        try
        {
          simulationsvariante = Long.parseLong( m_simVariante );
        }
        catch (Exception e) 
        {
        } 
      }
      
      // XML-Datei auswerten

      logger.info(startStoppDatei);
      
      SkriptVerwaltung sv = new SkriptVerwaltung (startStoppDatei, absender, simulationsvariante);
      
      if (sv.isFehlerStartStopp())
      {
        beendeStartStoppWegenFehler( "Plausibilitätsfehler in \"" + startStoppDatei  + "\"");
      }
      else
      {
        // StartStopp Block laden
  
        String id = sv.getStartStoppBlockId();
  
        StartStoppBlock ssb = m_startStoppVerwaltung.getStartStoppBlock( id );
  
        // auf Plausibilität prüfen
        
        if (ssb.isPlausibel())
        {
          // Telnet starten
          
          starteTelnet (m_port);
          
          // StartStopp Block starten
        
          ssb.starteProzesse( absender, simulationsvariante );
        }
        else
        {
          beendeStartStoppWegenFehler( "Plausibilitätsfehler in \"" + startStoppDatei  + "\"");
        }
      }
      
    } // if (m_versionieren == null)
  }

  /**
   * @return liefert die Klassenvariable m_ausgabeNachstarts zurück
   */
  public static boolean isAusgabeNachstarts()
  {
    return m_ausgabeNachstarts;
  }

  /**
   * @return liefert die Klassenvariable m_ausgabeLetzteStartzeit zurück
   */
  public static boolean isAusgabeLetzteStartzeit()
  {
    return m_ausgabeLetzteStartzeit;
  }

  /**
   * @return liefert die Klassenvariable m_ausgabeFormatiert zurück
   */
  public static int getAusgabeFormatierung()
  {
    return m_ausgabeFormatierung;
  }

  /**
   * @return liefert die Klassenvariable m_wartezeitErsteAnmeldung zurück
   */
  public static int getWartezeitErsteAnmeldung()
  {
    return m_wartezeitErsteAnmeldung;
  }

  /**
   * @return liefert die Klassenvariable m_wartezeitZwischenAnmeldungen zurck
   */
  public static int getWartezeitZwischenAnmeldungen()
  {
    return m_wartezeitZwischenAnmeldungen;
  }

  /**
   * @return liefert die Klassenvariable m_nutzerSchnittstelle zurück
   */
  public static NutzerSchnittstelle getNutzerSchnittstelle()
  {
    return m_nutzerSchnittstelle;
  }

  /**
   * @return liefert die Klassenvariable m_port zurück
   */
  public static String getPort()
  {
    return m_port;
  }

  /**
   * @return liefert die Klassenvariable m_startStoppKonfiguration zurück
   */
  public static String getStartStoppKonfiguration()
  {
    return m_startStoppKonfiguration;
  }

  /**
   * @return liefert die Klassenvariable m_versionieren zurück
   */
  public static String getVersionieren()
  {
    return m_versionieren;
  }
  
  /**
   * Methode zum Beenden von StartStopp im Fehlerfall. In dieser Methode
   * werden alle laufenden Threads der Applikation mitbeendet. Zusätzlich
   * wird ein Fehlertext auf der Standardfehlerausgabe und im Logbuch 
   * ausgegeben. 
   * @param text Fehlertext
   */
  public static void beendeStartStoppWegenFehler (String text)
  {
    m_beendeStartStopp = true;

    if (m_startStoppSperren != null)
      m_startStoppSperren.loescheApplikationGestartet();

    String buffer = "Gravierender Systemfehler:";
    
    logger.error (buffer);
    logger.error( text );
    
    buffer = "--> Programmabbruch";
    
    logger.error( buffer );
    
    if (m_prozessVerwaltung != null)
      m_prozessVerwaltung.beendeStartStoppApplikation( 0l, true );
    else
    {
      System.err.println("StartStopp beendet");
      System.exit( 0 );
    }
  }

  /**
   * Methode zum Beenden von StartStopp im Normalfall. In dieser Methode
   * werden alle laufenden Threads der Applikation mitbeendet. Zusätzlich
   * wird ein Fehlertext auf der Standardfehlerausgabe und im Logbuch 
   * ausgegeben. 
   * @param text Fehlertext
   */
  public static void beendeStartStopp (String text)
  {
    m_beendeStartStopp = true;
   
    m_startStoppSperren.loescheApplikationGestartet();
    
    logger.info (text);

    String buffer = "StartStopp Applikation beenden";
    
    logger.info( buffer );

    if (m_prozessVerwaltung != null)
      m_prozessVerwaltung.beendeStartStoppApplikation( 0l, true );
    else
    {
      logger.info("StartStopp beendet");
      
      System.exit( 0 );
    }
  }

  /**
   * Diese Methode wandelt die Aufrufargumente in einen String für die ApplikationsKennung um.
   * @param args Aufrufargumente
   */
  public static StringBuilder createApplicationLabel(String[] args) 
  {
    StringBuilder applicationLabel = new StringBuilder();
    
    for (String arg : args) 
    {
      applicationLabel.append(arg);
    }
    
    return applicationLabel;
  }

  /**
   * @return liefert die Klassenvariable m_rechnerAdresse zurück
   */
  public static String getRechnerAdresse()
  {
    return HostRechner.getInstanz().getHostAdresse();
  }

  /**
   * @return liefert die Klassenvariable m_rechnerName zurück
   */
  public static String getRechnerName()
  {
    return HostRechner.getInstanz().getHostName();
  }

  /**
   * Methode prüft ob die StartStopp Applikation beendet wird
   * @return wird die StartStopp Applikation beendet
   */
  public static boolean isStartStoppWirdBeendet()
  {
    return m_beendeStartStopp;
  }

  /**
   * Methode zum Starten der Telnetverbindung auf einem übergebenen Port.Wird als
   * Port null oder eine fehlerhafter Wert übergeben, wird keine Telnetverbindung
   * aufgebaut.
   * @param telnetPort Port auf dem die Telnetverbindung aufgebaut werden soll.
   */
  private static void starteTelnet (String telnetPort)
  {
    if (telnetPort != null)
    {
      int port = 0;
      
      try
      {
        port = Integer.parseInt( telnetPort );
      }
      catch (NumberFormatException e)
      {
        port = 0;
      }
  
      if (port == 0)
      {
        StartStoppApp.beendeStartStoppWegenFehler( "Aufrufparameter \"-port\" fehlerhaft !" );
      }
      else
      {
        try
        {
          m_nutzerSchnittstelle = new NutzerSchnittstelle( port);
        }
        catch ( Exception e )
        {
          m_nutzerSchnittstelle = null;
          logger.error("Telnetverbindung hat einen Fehler: " + e);
        }
        
        if (m_nutzerSchnittstelle != null)
          m_nutzerSchnittstelle.start();
      }
    }
  }

  /**
   * @return liefert die Klassenvariable m_startStoppInkarnationsName zurück
   */
  public static String getStartStoppInkarnationsName()
  {
    return m_startStoppInkarnationsName;
  }

  /**
   * @return liefert die Klassenvariable m_ausgabe zurück
   */
  public static boolean isAusgabeAufKonsole()
  {
    return m_ausgabe;
  }

  /**
   * @return liefert die Klassenvariable m_fehlerAusgabe zurück
   */
  public static boolean isFehlerAusgabeAufKonsole()
  {
    return m_fehlerAusgabe;
  }

  /**
   * Setzt den übergebenen URL-Pfad in einen File-Pfad um, wenn ein URL-Pfad übergeben wurde.
   * 
   * @param pfadAlsUrl umzusetzender URL-Pfad.
   * @return Original-Pfad oder umgesetzer URl-Pfad.
   */
  public static String urlUmsetzen(String pfadAlsUrl)
  {
  	String pfadAlsFile = pfadAlsUrl;
  	
    if (m_urlUmsetzen)
    {
    	try
			{
				URL url = new URL(pfadAlsUrl);
				
				if (url.getProtocol().equals("file"))
				{
					if (url.getHost().length() == 0)
					{
						try
						{
							File file = new File(url.toURI());
							
//							System.out.println("File " + file.getPath());
							
							pfadAlsFile = file.getPath();
						} 
						catch (URISyntaxException e)
						{
//		      		System.out.println("Keine URI " + value);
						}
						
					}
				}
			} 
    	catch (MalformedURLException e)
			{
//    		System.out.println("Keine URL " + value);
			}
    }

    return pfadAlsFile;
  }

  /**
   * Methode setzt die Variable startStoppKonfiguration
   * @param startStoppKonfiguration Verzeichnis der StartStopp Konfiguration
   */
  public static void setStartStoppKonfiguration( String startStoppKonfiguration )
  {
    m_startStoppKonfiguration = startStoppKonfiguration;
  }

  /**
   * Methode setzt den Inkarnationsnamen der Applikation 
   * @param startStoppInkarnationsName Applikationsname
   */
  public static void setStartStoppInkarnationsName( String startStoppInkarnationsName )
  {
    m_startStoppInkarnationsName = startStoppInkarnationsName;
  }

  /**
   * Methode bestimmt die IP-Adresse des Rechners, Methode wird nur für die JUNIT Test
   * benötigt
   */
  public void bestimmeRechnerAdresse ()
  {
    HostRechner.getInstanz();
  }

  /**
   * Methode prüft ob sich die Applikation im Testbetrieb befindet.
   * @return Testbetrieb
   */
  public static boolean isTestBetrieb()
  {
    return m_testBetrieb;
  }

  /**
   * Methode setzt den Testbetrieb bzw. schaltet ihn aus
   * @param testBetrieb Testbetrieb
   */
  public static void setTestBetrieb( boolean testBetrieb )
  {
    m_testBetrieb = testBetrieb;
  }
}
