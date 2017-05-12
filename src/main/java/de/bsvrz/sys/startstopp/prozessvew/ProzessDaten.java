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

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.skriptvew.GlobaleDaten;
import de.bsvrz.sys.startstopp.skriptvew.Kernsystem;
import de.bsvrz.sys.startstopp.skriptvew.StartStoppApp;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_AUSGABE;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_NEUSTART;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_START_FEHLER_VERHALTEN;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_STOPP_FEHLER_VERHALTEN;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_STARTART;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_WARTEART;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_ZUSTAND;

/**
 * Klasse beschreibt die Prozessdaten einer Inkarnation. Diese Daten bestehen zum einen aus
 * den Attributen die aus der StartStopp.xml Datei eingelesen wurden, zum anderen aus den
 * Statusinformationen die sich im Lebenszyklus einer Inkarnation ergeben.
 * @author Dambach Werke GmbH
 */

public class ProzessDaten implements Serializable
{
  /**
   * Debug
   */
  private static final Debug logger = Debug.getLogger();
  
  private static final long serialVersionUID = 3985694065630421645L;

  private final SimpleDateFormat _sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

  private final String _undefiniert = "-";

  /**
   * Text mit einer Fehlermeldung
   */
  private String m_fehlerText = "";
  
  //-----------------------------------------------------------------
  // Daten aus dem StartStoppblock
  //-----------------------------------------------------------------
  
  /**
   * ID des Prozesses (wird von Applikation StartStopp verwaltet)
   */
  private String m_prozessId;

  /**
   * Name des Prozesses
   */
  private String m_name;  
  
  /**
   * Name der ausführbaren Datei
   */
  private String m_ausfuehrbareDatei; 
  
  /**
   * Liste der Aufrufparameter
   */
  private List<String> m_aufrufParameter;
  
  /**
   * Startart
   */
  private StartArt m_startArt;
  
  /**
   * Liste der Startbedingungen
   */
  private List<StartBedingung> m_startBedingung;
  
  /**
   * Liste der Stoppbedingungen
   */
  private List<StoppBedingung> m_stoppBedingung;
  
  /**
   * Standardausgabe
   */
  private StandardAusgabe m_standardAusgabe = null;
  
  /**
   * Standardfehlerausgabe
   */
  private FehlerAusgabe m_standardFehlerAusgabe = null;
  
  /**
   * Startverhalten bei Fehlern
   */
  private StartFehlerVerhalten m_startVerhaltenFehler;
  
  /**
   * Stoppverhalten bei Fehlern
   */
  private StoppFehlerVerhalten m_stoppVerhaltenFehler;
  
  /**
   * Simulationsvariante
   */
  private long m_simulationsVariante = 0;
  
  /**
   * ID des Startstoppblocks zu dem dieser Prozess gehört
   */
  private String m_startStoppBlockId;
  
  /**
   * IP Adresse des Rechners auf dem die Applikation läuft
   */  
  private String m_ipAdresse;

  /**
   * Merker ob der Prozess zum Kernsystem gehört
   */
  private boolean m_kernsystem = false;

  //-----------------------------------------------------------------
  //Statusdaten
  //-----------------------------------------------------------------

  /**
   * Zustand des Prozesses
   */
  private E_ZUSTAND m_zustand = E_ZUSTAND.ANGELEGT;
  
  /**
   * Zeitpunkt erster Start
   */
  private Date m_ersterStart;
  
  /**
   * Zeitpunkt letzter Start
   */
  private Date m_letzterStart;

  /**
   * Zeitpunkt letzter Stopp
   */
  private Date m_letzterStopp;
  
  /**
   * Zeitpunkt letzte Initialisierung
   */
  private Date m_letzteInitialisierung;
  
  /**
   * Zeitpunkt Applikationsstart
   */
  private Date m_applikationsStart;
  
  /**
   * Zeitpunkt nächster Startzeitpunkt (bei zyklischem Starten)
   */
  private Date m_naechsterStart;
  
  /**
   * Anzahl Neustarts
   */
  private int m_anzahlNeustart;
  
  /**
   * Verweis auf eigentlichen Prozess
   */
  private Process m_prozess;
  
  /**
   * Verweis auf Inkarantion
   */
  private Inkarnation m_inkarnation;
  
  /**
   * Absender der die Inkarnation als letzter beeinflusst hat<br>
   * -   0: StartStopp Applikation<br>
   * -  -1: Telnet Bedienoberfläche<br>
   * sonst: andere Applikation über DaV<br>
   */
  private long m_absender = 0l;
  
  /**
   * Anzahl der fehlgeschlagenen Versuche beim Starten einer Applikation
   */
  private long m_anzFehlerStart = 0l;
  
  /**
   * Anzahl der fehlgeschlagenen Versuche beim Stoppen einer Applikation
   */
  private long m_anzFehlerStopp = 0l;
  
  /**
   * Liste mit den Klassen dich sich als Listener auf die Klasse angemeldet haben. Diese Klassen
   * werden informiert wenn sich Änderungen im Attribut "m_zustand" ereignet haben. 
   */
  private List<IProzessDatenListener> m_angemeldeteListener = new LinkedList<IProzessDatenListener>();

  /**
   * Instanz des Prozesses auf Betriebssystemebene der zu dieser Inkarnation gehört
   */  
  private BetriebssystemProzess m_betriebssystemProzess = null;
  
  /**
   * Parameterloser Konstruktor der Klasse
   */
  public  ProzessDaten()
  {
    m_startBedingung        = new ArrayList <StartBedingung>();
    m_stoppBedingung        = new ArrayList <StoppBedingung>();
    m_aufrufParameter       = new ArrayList <String>();

    m_startVerhaltenFehler  = new StartFehlerVerhalten();
    m_stoppVerhaltenFehler  = new StoppFehlerVerhalten();
  }
  
  /**
   * @return liefert die Klassenvariable m_anzahlNeustart zurück
   */
  public int getAnzahlNeustart()
  {
    return m_anzahlNeustart;
  }

  /**
   * @param anzahlNeustart setzt die Klassenvariable m_anzahlNeustart
   */
  public void setAnzahlNeustart( int anzahlNeustart )
  {
    m_anzahlNeustart = anzahlNeustart;
  }

  /**
   * @return liefert die Klassenvariable m_aufrufParameter zurück
   */
  public List<String> getAufrufParameter()
  {
    return m_aufrufParameter;
  }

  /**
   * @param aufrufParameter setzt die Klassenvariable m_aufrufParameter
   */
  public void setAufrufParameter( List<String> aufrufParameter )
  {
    m_aufrufParameter = aufrufParameter;
  }

  /**
   * Methode fügt einen Aufrufparameter hinzu
   * @param aufrufParameter Aufrufparameter hinzufügen
   */
  public void addAufrufParameter( String aufrufParameter )
  {
    m_aufrufParameter.add( aufrufParameter );
  }
  
  /**
   * @return liefert die Klassenvariable m_inkarnation zurück
   */
  public Inkarnation getInkarnation()
  {
    return m_inkarnation;
  }

  /**
   * @param inkarnation setzt die Klassenvariable m_inkarnation
   */
  public void setInkarnation( Inkarnation inkarnation )
  {
    m_inkarnation = inkarnation;
  }

  /**
   * Methode liefert den Namen der Inkarnation entsprechend den Konventionen der Klasse Tools, 
   * Methode bestimmeInkarnationsName() zurück.
   * @return Name der Inkarnation
   */
  public String getName()
  {
    return Tools.bestimmeInkarnationsName( m_name, m_simulationsVariante );
  }

  /**
   * @param name setzt die Klassenvariable m_name
   */
  public void setName( String name )
  {
    m_name = name;
  }

  /**
   * @return liefert die Klassenvariable m_prozess zurück
   */
  public Process getProzess()
  {
    return m_prozess;
  }

  /**
   * @param prozess setzt die Klassenvariable m_prozess
   */
  public void setProzess( Process prozess )
  {
    m_prozess = prozess;
  }

  /**
   * @return liefert die Klassenvariable m_prozessId zurück
   */
  public String getProzessId()
  {
    return m_prozessId;
  }

  /**
   * @param prozessId setzt die Klassenvariable m_prozessId
   */
  public void setProzessId( String prozessId )
  {
    m_prozessId = prozessId;
  }

  /**
   * @return liefert die Klassenvariable m_simulationsVariante zurück
   */
  public long getSimulationsVariante()
  {
    return m_simulationsVariante;
  }

  /**
   * @param simulationsVariante setzt die Klassenvariable m_simulationsVariante
   */
  public void setSimulationsVariante( long simulationsVariante )
  {
    m_simulationsVariante = simulationsVariante;
  }

  /**
   * @return liefert die Klassenvariable m_standardAusgabe zurück
   */
  public StandardAusgabe getStandardAusgabe()
  {
    return m_standardAusgabe;
  }

  /**
   * @param standardAusgabe setzt die Klassenvariable m_standardAusgabe
   */
  public void setStandardAusgabe( StandardAusgabe standardAusgabe )
  {
    m_standardAusgabe = standardAusgabe;
  }

  /**
   * Methode zum Setzen der Standardausgabe als einzelen Attribute
   * @param startStoppBlockId ID des StartStopp Blocks
   * @param option Option
   * @param dateiAlias Datei Alias
   */
  public void setStandardAusgabe (String startStoppBlockId, E_AUSGABE option, String dateiAlias)
  {
    m_standardAusgabe = new StandardAusgabe (startStoppBlockId, option, dateiAlias);
  }

  /**
   * @return liefert die Klassenvariable m_standardFehlerAusgabe zurück
   */
  public FehlerAusgabe getStandardFehlerAusgabe()
  {
    return m_standardFehlerAusgabe;
  }

  /**
   * @param standardFehlerAusgabe setzt die Klassenvariable m_standardFehlerAusgabe
   */
  public void setStandardFehlerAusgabe( FehlerAusgabe standardFehlerAusgabe )
  {
    m_standardFehlerAusgabe = standardFehlerAusgabe;
  }

  /**
   * Methode zum Setzen der Standardfehlerausgabe als einzelen Attribute
   * @param startStoppBlockId ID des StartStopp Blocks
   * @param option Option
   * @param dateiAlias Datei Alias
   */
  public void setStandardFehlerAusgabe (String startStoppBlockId, E_AUSGABE option, String dateiAlias)
  {
    m_standardFehlerAusgabe = new FehlerAusgabe (startStoppBlockId, option, dateiAlias);
  }

  /**
   * @return liefert die Klassenvariable m_startArt zurück
   */
  public StartArt getStartArt()
  {
    return m_startArt;
  }

  /**
   * @param startArt setzt die Klassenvariable m_startArt
   */
  public void setStartArt( StartArt startArt )
  {
    m_startArt = startArt;
  }

  /**
   * Methode zum Setzen der Startart als einzelne Attribute
   * @param option Option
   * @param neuStart Neustartverhalten
   * @param intervallZeit Intervallzeit
   */
  public void setStartArt( String option, String neuStart, String intervallZeit )
  {
    StartArt startart = new StartArt ();
    
    startart.setOption        ( E_STARTART.getEnum( option ) );
    startart.setNeuStart      ( E_NEUSTART.getEnum( neuStart ) );
    startart.setIntervallZeit ( intervallZeit );
   
    m_startArt = startart;
  }

  /**
   * @return liefert die Klassenvariable m_startBedingung zurück
   */
  public List<StartBedingung> getStartBedingung()
  {
    return m_startBedingung;
  }

  /**
   * @param startBedingung setzt die Klassenvariable m_startBedingung
   */
  public void setStartBedingung( List<StartBedingung> startBedingung )
  {
    m_startBedingung = startBedingung;
  }

  /**
   * Methode zum Hinzufügen einer Startbedingung
   * @param vorgaenger Vorgänger Prozess
   * @param warteArt Warteart
   * @param rechner Rechner auf dem Vorgängerprozess läuft
   * @param warteZeit Wartezeit in Sekunden
   */
  public void addStartBedingung(String vorgaenger ,String warteArt, String rechner, long warteZeit)
  {
    StartBedingung sb = new StartBedingung (this,
                                            rechner,
                                            vorgaenger,
                                            E_WARTEART.getEnum( warteArt ),
                                            warteZeit,
                                            m_startStoppBlockId);

    m_startBedingung.add( sb );    
  }

  /**
   * @return liefert die Klassenvariable m_startVerhaltenFehler zurück
   */
  public StartFehlerVerhalten getStartVerhaltenFehler()
  {
    return m_startVerhaltenFehler;
  }

  /**
   * @param startVerhaltenFehler setzt die Klassenvariable m_startVerhaltenFehler
   */
  public void setStartVerhaltenFehler( StartFehlerVerhalten startVerhaltenFehler )
  {
    m_startVerhaltenFehler = startVerhaltenFehler;
  }

  /**
   * Methode zum Setzen des Startfehlerverhaltens als Attribute
   * @param option Option
   * @param wiederholung Anzahl Wiederholungen
   */
  public void setStartVerhaltenFehler (E_START_FEHLER_VERHALTEN option, long wiederholung  )
  {
    m_startVerhaltenFehler.setOption( option );
    m_startVerhaltenFehler.setWiederholungen( wiederholung );
  }

  /**
   * @return liefert die Klassenvariable m_stoppBedingung zurück
   */
  public List<StoppBedingung> getStoppBedingung()
  {
    return m_stoppBedingung;
  }

  /**
   * @param stoppBedingung setzt die Klassenvariable m_stoppBedingung
   */
  public void setStoppBedingung( List<StoppBedingung> stoppBedingung )
  {
    m_stoppBedingung = stoppBedingung;
  }

  /**
   * Methode zum Hinzufügen einer Stoppbedingung
   * @param nachfolger Name des Nachfolgeprozess
   * @param rechner Rechner auf dem Nachfolgeprozess läuft
   * @param warteZeit Wartezeit in Sekunden
   */
  public void addStoppBedingung (String nachfolger, String rechner, long warteZeit)
  {
    StoppBedingung stb = new StoppBedingung (this,
                                             rechner,
                                             nachfolger,
                                             warteZeit,
                                             m_startStoppBlockId);
    
    m_stoppBedingung.add( stb );    
  }
  
  /**
   * @return liefert die Klassenvariable m_stoppVerhaltenFehler zurück
   */
  public StoppFehlerVerhalten getStoppVerhaltenFehler()
  {
    return m_stoppVerhaltenFehler;
  }

  /**
   * @param stoppVerhaltenFehler setzt die Klassenvariable m_stoppVerhaltenFehler
   */
  public void setStoppVerhaltenFehler( StoppFehlerVerhalten stoppVerhaltenFehler )
  {
    m_stoppVerhaltenFehler = stoppVerhaltenFehler;
  }

  /**
   * Methode zum Setzen des Stoppfehlerverhaltens als Attribute
   * @param option Option
   * @param wiederholungen Anzahl Wiederholungen
   */
  public void setStoppVerhaltenFehler( E_STOPP_FEHLER_VERHALTEN option, long wiederholungen )
  {
    m_stoppVerhaltenFehler.setOption( option ) ;
    m_stoppVerhaltenFehler.setWiederholungen( wiederholungen );
  }

  /**
   * @return liefert die Klassenvariable m_ausfuehrbareDatei zurück
   */
  public String getAusfuehrbareDatei()
  {
    return m_ausfuehrbareDatei;
  }

  /**
   * @param ausfuehrbareDatei setzt die Klassenvariable m_ausfuehrbareDatei
   */
  public void setAusfuehrbareDatei( String ausfuehrbareDatei )
  {
    m_ausfuehrbareDatei = ausfuehrbareDatei;
  }

  /**
   * @return liefert die Klassenvariable m_kernsystem zurück
   */
  public boolean isKernsystem()
  {
    return m_kernsystem;
  }

  /**
   * @param kernsystem setzt die Klassenvariable m_kernsystem
   */
  public void setKernsystem( boolean kernsystem )
  {
    m_kernsystem = kernsystem;
  }

  /**
   * @return liefert die Klassenvariable m_zustand zurück
   */
  public E_ZUSTAND getZustand()
  {
    return m_zustand;
  }

  /**
   * Methode setzt die Klassenvariable m_zustand und modifiziert je nach
   * Zustand die Klassenvariablen
   * -m_ersterStart<br>
   * -m_applikationsStart<br>
   * -m_letzterStart<br>
   * -m_letzteInitialisierung<br>
   * -letzterStopp<br>
   * -m_anzahlNeustart<br>
   * @param zustand setzt die Klassenvariable m_zustand<br>
   */
  public void setZustand( E_ZUSTAND zustand )
  {
    if (zustand != m_zustand)
    {
      Date d = new Date ();

      if (zustand == E_ZUSTAND.INITIALISIERT)
        m_letzteInitialisierung = d;

      if (zustand == E_ZUSTAND.GESTOPPT)
        m_letzterStopp = d;
      
      if (zustand == E_ZUSTAND.GESTARTET)
      {
        m_letzterStart      = d;
        m_applikationsStart = d;
        
        m_anzahlNeustart++;
        
        if (m_ersterStart == null)
          m_ersterStart = d;
      }
    }
    
    m_zustand = zustand;
    
    benachrichtigeListener();
  }
  
  /**
   * @return liefert die Klassenvariable m_applikationsStart zurück
   */
  public Date getApplikationsStart()
  {
    return m_applikationsStart;
  }

  /**
   * Methode liefert die Klassenvariable m_applikationsStart als String zurück
   * @return Applikationsstart als String
   */
  public String getApplikationsStartAsString()
  {
    return wandleDateInString( m_applikationsStart );
  }

  /**
   * @return liefert die Klassenvariable m_ersterStart zurück
   */
  public Date getErsterStart()
  {
    return m_ersterStart;
  }

  /**
   * Methode liefert die Klassenvariable m_ersterStart als String zurück
   * @return Applikationsstart als String
   */
  public String getErsterStartAsString()
  {
    return wandleDateInString( m_ersterStart );
  }

  /**
   * @return liefert die Klassenvariable m_letzterStart zurück
   */
  public Date getLetzterStart()
  {
    return m_letzterStart;
  }

  /**
   * Methode liefert die Klassenvariable m_letzterStart als String zurück
   * @return Applikationsstart als String
   */
  public String getLetzterStartAsString()
  {
    return wandleDateInString( m_letzterStart );    
  }

  /**
   * @return liefert die Klassenvariable m_naechsterStart zurück
   */
  public Date getNaechsterStart()
  {
    return m_naechsterStart;
  }

  /**
   * @param naechsterStart setzt die Klassenvariable m_naechsterStart
   */
  public void setNaechsterStart( Date naechsterStart )
  {
    m_naechsterStart = naechsterStart;
  }

  /**
   * @param naechsterStart setzt die Klassenvariable m_naechsterStart
   */
  public void setNaechsterStart( String naechsterStart )
  {
    m_naechsterStart = wandleStringInDate( naechsterStart );
  }

  /**
   * Methode liefert die Klassenvariable m_naechsterStart als String zurück
   * @return Applikationsstart als String
   */
  public String getNaechsterStartAsString()
  {
    return wandleDateInString( m_naechsterStart );    
  }

  /**
   * Methode löscht die Aufrufparameter
   */
  public void clearAufrufParameter()
  {
    m_aufrufParameter.clear();
  }

  /**
   * Methode löscht die Startbedingung
   */
  public void clearStartBedingung()
  {
    m_startBedingung.clear();
  }

  /**
   * Methode löscht die Stoppbedingung
   */  
  public void clearStoppBedingung()
  {
    m_stoppBedingung.clear();
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

  /**
   * @return liefert die Klassenvariable m_anzFehlerStart zurück
   */
  public long getAnzFehlerStart()
  {
    return m_anzFehlerStart;
  }

  /**
   * @param anzFehlerStart setzt die Klassenvariable m_anzFehlerStart
   */
  public void setAnzFehlerStart( long anzFehlerStart )
  {
    this.m_anzFehlerStart = anzFehlerStart;
  }

  /**
   * @return liefert die Klassenvariable m_anzFehlerStopp zurück
   */
  public long getAnzFehlerStopp()
  {
    return m_anzFehlerStopp;
  }

  /**
   * @param anzFehlerStopp setzt die Klassenvariable m_anzFehlerStopp
   */
  public void setAnzFehlerStopp( long anzFehlerStopp )
  {
    this.m_anzFehlerStopp = anzFehlerStopp;
  }

  /**
   * @return liefert die Klassenvariable m_ipAdresse zurück
   */
  public String getIpAdresse()
  {
    return m_ipAdresse;
  }

  /**
   * @param ipAdresse setzt die Klassenvariable m_ipAdresse
   */
  public void setIpAdresse( String ipAdresse )
  {
    m_ipAdresse = ipAdresse;
  }

  /**
   * @return liefert die Klassenvariable m_startStoppBlockId zurück
   */
  public String getStartStoppBlockId()
  {
    return m_startStoppBlockId;
  }

  /**
   * @param startStoppBlockId setzt die Klassenvariable m_ startStoppBlockId
   */
  public void setStartStoppBlockId( String startStoppBlockId )
  {
    m_startStoppBlockId = startStoppBlockId;
  }

  /**
   * Methode prüft ob sich eine Inkarnation in der Startphase befindet.
   * @return true: Inkarnation befindet sich in Startphase, sonst false
   */
  public boolean isInStartPhase ()
  {
    if ((m_zustand == E_ZUSTAND.STARTEN) ||
        (m_zustand == E_ZUSTAND.STARTENWARTEN ))
      return true;
    else
      return false;
  }

  /**
   * Methode prüft ob eine Inkarnation auf die Erfüllung ihrer Startbedingungen wartet .
   * @return true: Inkarnation wartet, sonst false
   */
  public boolean isInWarteStartbedingungPhase ()
  {
    if ( m_zustand == E_ZUSTAND.STARTENWARTEN )
      return true;
    else
      return false;
  }

  /**
   * Methode prüft ob eine Inkarnation gestartet wurde
   * @return true: Inkarnation gestartet, sonst false
   */
  public boolean isGestartet ()
  {
    if ((m_zustand == E_ZUSTAND.GESTARTET    ) || 
        (m_zustand == E_ZUSTAND.INITIALISIERT))
        
      return true;
    else
      return false;
  }

  /**
   * Methode prüft ob eine Inkarnation initialisiert wurde
   * @return true: Inkarnation initialisiert, sonst false
   */
  public boolean isInitialisiert ()
  {
    if (m_zustand == E_ZUSTAND.INITIALISIERT) 
      return true;
    else
      return false;
  }

  /**
   * Methode prüft ob sich eine Inkarnation in der Stoppphase befindet.
   * @return true: Inkarnation befindet sich in Stoppphase, sonst false
   */
  public boolean isInStoppPhase ()
  {
    if ((m_zustand == E_ZUSTAND.STOPPEN       ) ||
        (m_zustand == E_ZUSTAND.STOPPENWARTEN ))
      return true;
    else
      return false;
  }

  /**
   * Methode prüft ob eine Inkarnation auf die Erfüllung ihrer Stoppbedingungen wartet .
   * @return true: Inkarnation wartet, sonst false
   */
  public boolean isInWarteStoppbedingungPhase ()
  {
    if ( m_zustand == E_ZUSTAND.STOPPENWARTEN )
      return true;
    else
      return false;
  }

  /**
   * Methode prüft ob eine Inkarnation gestoppt wurde
   * @return true: Inkarnation gestoppt, sonst false
   */
  public boolean isGestoppt ()
  {
    if (m_zustand == E_ZUSTAND.GESTOPPT)
      return true;
    else
      return false;
  }

  /**
   * Methode prüft ob eine Inkarnation aktiv ist (aktiv bedeutet, sie ist
   * entweder in der Startphase, gestartet oder in der Stoppphase).
   * @return true: Inkarnation aktiv, sonst false
   */
  public boolean isAktiv ()
  {
    if (isGestartet() || isInitialisiert() || isInStoppPhase())
      return true;
    else
      return false;
  }

  /**
   * Method prüft ob die Prozessdaten plausibel sind
   * @return true: Daten plausibel, false: Daten nicth plausibel
   */
  public boolean isPlausibel ()
  {
    return isPlausibel(null);
  }
  
  /**
   * Method prüft ob die Prozessdaten plausibel sind
   * @param neueApplikationen Liste mit den Namen der Applikationen, die mitgeprüft 
   * werden sollen (für die Start- bzw. Stoppbedingung). Siehe auch Beschreibung der Methoden
   * startbedingungPlausibel bzw. stoppbedingungPlausibel.
   * @return true: Daten plausibel, false: Daten nicth plausibel
   */
  public boolean isPlausibel (List<String> neueApplikationen)
  {
    boolean plausibel = true;
    
    getMaxSpeicher();
    
    // Aufruf prüfen
    
    if (plausibel)
      if (!aufrufPlausibel(this))
        plausibel = false;
    
    // Startart prüfen
    
    if (plausibel)
      if (!startartPlausibel(this))
        plausibel = false;
    
    // Startbedingung prüfen
    
    if (plausibel)
      if (!startbedingungPlausibel(this, neueApplikationen ))
        plausibel = false;

    // Stoppbedingung prüfen
    
    if (plausibel)
      if (!stoppbedingungPlausibel( this, neueApplikationen ))
        plausibel = false;

    // Standardausgabe
    
    if (plausibel)
      if (!standardAusgabePlausibel( this ))
        plausibel = false;
    
    // Standardfehlerausgabe
    
    if (plausibel)
      if (!standardFehlerAusgabePlausibel( this ))
        plausibel = false;

    // Startfehlerverhalten prüfen
    
    if (plausibel)
      if (!startFehlerVerhaltenPlausibel(this ))
        plausibel = false;

    // Stoppfehlerverhalten prüfen
    
    if (plausibel)
      if (!stoppFehlerVerhaltenPlausibel(this ))
        plausibel = false;
    
    return plausibel;
  }

  /**
   * Methode prüft, ob im Aufruf der Applikation Fehler enthalten sind. Im Moment
   * sind dabei implementiert:<br>
   * - prüfen ob versucht wird die StartStopp Applikation durch die StartStopp Applikation noch einmal zu starten<br>
   * - prüfen ob der Aufrufparameter "-simVariante" im Aufruf der Applikation vorkommt
   * - prüfen ob der Aufrufparameter "-inkarnationsName" im Aufruf der Applikation vorkommt
   * @param daten Prozessdaten die geprüft werden
   * @return true: Daten plausibel, false: Daten nicth plausibel
   */
  private boolean aufrufPlausibel( ProzessDaten daten )
  {
    // Name der Klasse die die StartStopp Applikation enthält
    
    String klasse = "de.bsvrz.sys.startstopp.skriptvew.StartStoppApp";

    String s = null;
    String kennung = daten.getName() + " - Plausibilitätsfehler Aufruf - ";
    
    boolean plausibel = true;

    Logbuch l = Logbuch.getInstanz();

    // Prüfen ob StartStopp vorkommt
    
    if (plausibel)
    {
      if (daten.getAufrufOriginal().toLowerCase().contains( klasse.toLowerCase() ))
      {
        s = kennung + "Es wird versucht die StartStopp Applikation aus dem StartStopp Block heraus zu starten";
        
        logger.error( s );
      
        plausibel = false;
      }
    }
    
    // Prüfen ob Aufrufparameter vorkommen

    if (plausibel)
    {
      List<String> verboteneParameter = new ArrayList<String>();
      
      verboteneParameter.add("-simVariante=" );
      verboteneParameter.add("-inkarnationsName=" );
     
      for (int  i=0; i<verboteneParameter.size() && plausibel; ++i)
      {
        String s1 = daten.getAufrufOriginal().toLowerCase().replaceAll(" ", "");
        
        if (s1.contains( verboteneParameter.get( i ).toLowerCase() ))
        {
          s = kennung + "Im Aufruf der Applikation wird der Parameter \"" + verboteneParameter.get( i ) + "\" übergeben";
        
          logger.error (s);
      
          plausibel = false;
        }
      }
    }
    
    return plausibel;
  }

  /**
   * Methode prüft, ob die Startart semantisch korrekt sind
   * @param daten Prozessdaten die geprüft werden
   * @return true: Daten plausibel, false: Daten nicth plausibel
   */
  private boolean startartPlausibel (ProzessDaten daten)
  {
    String s = null;
    String kennung = daten.getName() + " - Plausibilitätsfehler Startart - ";
    
    boolean plausibel = true;

    Logbuch l = Logbuch.getInstanz();

    StartArt sa = daten.getStartArt();
    
    if (sa == null)
    {
      s = kennung + "Startart = null";
      
      logger.error( s );
    
      plausibel = false;
    }
    else
    {
      String intervall = sa.getIntervallZeit();
      if (!intervall.equals( "" ))
      {
        TimeIntervalCron t = new  TimeIntervalCron ();
        
        t.setFields( intervall );
        
        if (t.hasParseErrors())
        {
          s = kennung + "Intervallzeit " + intervall + " nicht plausibel";
        
          logger.error( s );
        
          plausibel = false;
        }
      }
    }
    
    return plausibel;
  }

  /**
   * Methode prüft, ob die Startbedingungen semantisch korrekt sind. Bei der Prüfung ob der
   * Vorgänger Prozess der StartStopp Applikation bekannt ist, werden zuerst die bereits
   * bekannten Inkarnationen überprüft. Zusätliche kann eine Liste mit Applikationsnamen
   * übergeben werden die ebenfalls in die Prüfung miteinbezogen werden sollen. Dies wird
   * vor allem dann benötigt, wenn ein neuer StartStopp Block überprüft wird, bei dem es
   * Abhängigkeiten zwischen den Prozessen innerhalb dieses StartStopp Blocks gibt (diese Prozesse
   * sind ja noch nicht der StartStoppApplikation bekannt).
   * @param daten Prozessdaten die geprüft werden
   * @param neueApplikationen Liste mit den Namen der Applikationen, die mitgeprüft 
   * werden sollen
   * @return true: Daten plausibel, false: Daten nicht plausibel
   */
  private boolean startbedingungPlausibel (ProzessDaten daten, List<String> neueApplikationen)
  {
    String s = null;
    String kennung = daten.getName() + " - Plausibilitätsfehler Startbedingung - ";
    
    boolean plausibel = true;

    Logbuch l = Logbuch.getInstanz();
    ProzessVerwaltung pv = ProzessVerwaltung.getInstanz();
    GlobaleDaten glob = GlobaleDaten.getInstanz();

    List<StartBedingung> lsb = daten.getStartBedingung();
    for (int i=0; i<lsb.size(); ++i)
    {
      StartBedingung sb = lsb.get( i );
      
      // Daten einlesen
      
      String rechner    = sb.getRechnerAlias();
      String vorgaenger = sb.getProzessName();
      String ip         = glob.getIpAdresse(m_startStoppBlockId, rechner);
      
      if (vorgaenger.equals( "" ))  // Platzhalter
        continue;

      // Rechner
      
      if (ip == null)
      {
        s = kennung + "Rechner \"" + rechner + "\" nicht im globalen Teil definiert";
        
        logger.error( s );
        
        plausibel = false;
      }
      
      // Vorgaenger
      
      // Wird auf einen Prozess des lokalen Rechners gewartet und können für
      // diesen Prozess keine Prozessdaten geladen werden, so muss ein Fehler
      // in der StartStopp.xml vorliegen.

      if (plausibel)
      {
        if (ip.equals( StartStoppApp.getRechnerAdresse()))
        {
          // Alle bisher bekannten Inkarnationen dieses Rechners laden
          
          boolean gefunden = false;
          
          List<ProzessDaten> pd = pv.getAlleProzessDaten();
          
          for (int ii=0; ii<pd.size(); ++ii)
            if (pd.get( ii ).getName().equals( vorgaenger ))
              gefunden = true;
          
          // Wurde Vorgänger nicht gefunden, so wird die zusätzliche Liste
          // überprüft
          
          if (!gefunden && (neueApplikationen != null))
          {
            for (int ii=0; ii<neueApplikationen.size(); ++ii)
              if (neueApplikationen.get( ii ).equals( vorgaenger ))
                gefunden = true;
          }
              
          if (!gefunden)
          {
            String text = "Fehler, Inkarnation wartet auf einen lokalen Prozess\n" +
                          "\"" + vorgaenger + "\" der nicht in der StartStopp.xml Datei definiert ist !";
          
            s = kennung + text;
            
            logger.error( s );
            
            plausibel = false;
          }
        }
      }
    }
    
    return plausibel;
  }

  /**
   * Methode prüft, ob die Stoppbedingungen semantisch korrekt sind. Bei der Prüfung ob der
   * Nachfolger Prozess der StartStopp Applikation bekannt ist, werden zuerst die bereits
   * bekannten Inkarnationen überprüft. Zusätliche kann eine Liste mit Applikationsnamen
   * übergeben werden die ebenfalls in die Prüfung miteinbezogen werden sollen. Dies wird
   * vor allem dann benötigt, wenn ein neuer StartStopp Block überprüft wird, bei dem es
   * Abhängigkeiten zwischen den Prozessen innerhalb dieses StartStopp Blocks gibt (diese Prozesse
   * sind ja noch nicht der StartStoppApplikation bekannt).
   * @param daten Prozessdaten die geprüft werden
   * @param neueApplikationen Liste mit den Namen der Applikationen, die mitgeprüft 
   * werden sollen
   * @return true: Daten plausibel, false: Daten nicht plausibel
   */
  private boolean stoppbedingungPlausibel (ProzessDaten daten, List<String> neueApplikationen)
  {
    String s = null;
    String kennung = daten.getName() + " - Plausibilitätsfehler Stoppbedingung - ";
    
    boolean plausibel = true;

    Logbuch l = Logbuch.getInstanz();
    ProzessVerwaltung pv = ProzessVerwaltung.getInstanz();
    GlobaleDaten glob = GlobaleDaten.getInstanz();

    List<StoppBedingung> lsb = daten.getStoppBedingung();
    for (int i=0; i<lsb.size(); ++i)
    {
      StoppBedingung sb = lsb.get( i );
      
      // Daten einlesen
      
      String rechner    = sb.getRechnerAlias();
      String nachfolger = sb.getProzessName();
      String ip         = glob.getIpAdresse(m_startStoppBlockId, rechner);

      if (nachfolger.equals( "" ))  // Platzhalter
        continue;

      // Rechner
      
      if (ip == null)
      {
        s = kennung + "Rechner \"" + rechner + "\" nicht im globalen Teil definiert";
        
        logger.error( s );
        
        plausibel = false;
      }
      
      // Wird auf einen Prozess des lokalen Rechners gewartet und können für
      // diesen Prozess keine Prozessdaten geladen werden, so muss ein Fehler
      // in der StartStopp.xml vorliegen.

      if (plausibel)
      {
        if (ip.equals( StartStoppApp.getRechnerAdresse()))
        {
          // Alle bisher bekannten Inkarnationen dieses Rechners laden
          
          boolean gefunden = false;
          
          List<ProzessDaten> pd = pv.getAlleProzessDaten();
          
          for (int ii=0; ii<pd.size(); ++ii)
            if (pd.get( ii ).getName().equals( nachfolger ))
              gefunden = true;
          
          // Wurde Prozess nicht gefunden, so wird die zusätzliche Liste
          // überprüft
          
          if (!gefunden && (neueApplikationen != null))
          {
            for (int ii=0; ii<neueApplikationen.size(); ++ii)
              if (neueApplikationen.get( ii ).equals( nachfolger ))
                gefunden = true;
          }
              
          if (!gefunden)
          {
            String text = "Fehler, Inkarnation wartet auf einen lokalen Prozess\n" +
                          "\"" + nachfolger + "\" der nicht in der StartStopp.xml Datei definiert ist !";
          
            s = kennung + text;
            
            logger.error( s );
            
            plausibel = false;
          }
        }
      }
    }
    
    return plausibel;
  }
  
  /**
   * Methode prüft, ob das Startfehlerverhalten semantisch korrekt sind
   * @param daten Prozessdaten die geprüft werden
   * @return true: Daten plausibel, false: Daten nicth plausibel
   */
  private boolean startFehlerVerhaltenPlausibel (ProzessDaten daten)
  {
    String s = null;
    String kennung = daten.getName() + " - Plausibilitätsfehler Startfehlerverhalten - ";
    
    boolean plausibel = true;

    Logbuch l = Logbuch.getInstanz();

    StartFehlerVerhalten sfv = daten.getStartVerhaltenFehler();
    
    if (sfv.getOption() == null)
    {
      s = kennung + "Fehlerhafte Option";
        
      logger.error( s );
      
      plausibel = false;
    }
    
    return plausibel;
  }

  /**
   * Methode prüft, ob das Stoppfehlerverhalten semantisch korrekt sind
   * @param daten Prozessdaten die geprüft werden
   * @return true: Daten plausibel, false: Daten nicth plausibel
   */
  private boolean stoppFehlerVerhaltenPlausibel (ProzessDaten daten)
  {
    String s = null;
    String kennung = daten.getName() + " - Plausibilitätsfehler Stoppfehlerverhalten - ";
    
    boolean plausibel = true;

    Logbuch l = Logbuch.getInstanz();

    StoppFehlerVerhalten sfv = daten.getStoppVerhaltenFehler();
    
    if (sfv.getOption() == null)
    {
      s = kennung + "Fehlerhafte Option";
        
      logger.error( s );
      
      plausibel = false;
    }
    
    return plausibel;
  }

  /**
   * Methode prüft, ob die Standardausgabe semantisch korrekt sind
   * @param daten Prozessdaten die geprüft werden
   * @return true: Daten plausibel, false: Daten nicth plausibel
   */
  private boolean standardAusgabePlausibel (ProzessDaten daten)
  {
    String s = null;
    String kennung = daten.getName() + " - Plausibilitätsfehler Standardausgabe - ";
    
    boolean plausibel = true;

    Logbuch l = Logbuch.getInstanz();

    StandardAusgabe sa = daten.getStandardAusgabe();
    
    // Option prüfen
    
    if (sa.getOption() == null)
    {
      s = kennung + "Fehlerhafte Option";
        
      logger.error( s );
      
      plausibel = false;
    }

    // Datei prüfen
    
    if (sa.getOption() != E_AUSGABE.IGNORIEREN)
    {
      String startStoppBlockId = sa.getStartStoppBlockId();
      String alias = sa.getDateiAlias();
      
      if (GlobaleDaten.getInstanz().getProtokollDateiByAlias( startStoppBlockId, alias ) == null)
      {
        s = kennung + "Fehler im Dateinamen. Alias \"" + sa.getDateiAlias() + "\" kann nicht aufgelöst werden";

        logger.error( s );
        
        plausibel = false;
      }
    }
    
    return plausibel;
  }
  
  /**
   * Methode prüft, ob die Standardfehlerausgabe semantisch korrekt sind
   * @param daten Prozessdaten die geprüft werden
   * @return true: Daten plausibel, false: Daten nicth plausibel
   */
  private boolean standardFehlerAusgabePlausibel (ProzessDaten daten)
  {
    String s = null;
    String kennung = daten.getName() + " - Plausibilitätsfehler Standardfehlerausgabe - ";
    
    boolean plausibel = true;

    Logbuch l = Logbuch.getInstanz();

    FehlerAusgabe sa = daten.getStandardFehlerAusgabe();
    
    // Option prüfen
    
    if (sa.getOption() == null)
    {
      s = kennung + "Fehlerhafte Option";
        
      logger.error( s );
      
      plausibel = false;
    }

    // Datei prüfen
    
    if (sa.getOption() != E_AUSGABE.IGNORIEREN)
    {
      String startStoppBlockId = sa.getStartStoppBlockId();
      String alias = sa.getDateiAlias();
      
      if (GlobaleDaten.getInstanz().getProtokollDateiByAlias( startStoppBlockId, alias ) == null)
      {
        s = kennung + "Fehler im Dateinamen. Alias \"" + sa.getDateiAlias() + "\" kann nicht aufgelöst werden";

        logger.error( s );
        
        plausibel = false;
      }
    }
    
    return plausibel;
  }

  //----------------------------------------------------------------------------------------------
  // Listener Funktionalitäten
  //----------------------------------------------------------------------------------------------

  /**
   * Methode zum Hinzufügen eines Listeners
   * @param listener Listener der hinzugefügt werden soll
   */
  public void addListener (IProzessDatenListener listener)
  {
    m_angemeldeteListener.add( listener );
  }
 
  /**
   * Methode zum Entfernen eines Listeners
   * @param listener Listener der entfernt werden soll
   */
  public void removeListener (IProzessDatenListener listener)
  {
    m_angemeldeteListener.remove( listener );
  }
 
  /**
   * Methode mit der die Klasse alle bei ihr angemeldeten Listener
   * über die Änderung der Daten informiert.
   */
  private void benachrichtigeListener ()
  {
    // neues Ereignis erzeugen
  
    ProzessDatenEreignis e = new ProzessDatenEreignis (this);
   
    // zu übergebende Daten eintragen
  
    Iterator<IProzessDatenListener> it = m_angemeldeteListener.iterator();
    while (it.hasNext())
    {
      IProzessDatenListener l = it.next();
      l.exec(e);
    }
  }

  /**
   * @return liefert die Klassenvariable m_letzteInitialisierung zurück
   */
  public Date getLetzteInitialisierung()
  {
    return m_letzteInitialisierung;
  }

  /**
   * Methode liefert die Klassenvariable m_letzteInitialisierung als String zurück
   * @return Zeit als String
   */
  public String getLetzteInitialisierungAsString()
  {
    return wandleDateInString( m_letzteInitialisierung );
  } 

  /**
   * @return liefert die Klassenvariable m_letzterStopp zurück
   */
  public Date getLetzterStopp()
  {
    return m_letzterStopp;
  }

  /**
   * Methode liefert die Klassenvariable m_ersterStart als String zurück
   * @return Applikationsstart als String
   */
  public String getLetzterStoppAsString()
  {
    return wandleDateInString( m_letzterStopp );
  } 
  
  /**
   * Methode wandelt die übergebene Zeit in einen String in das Textformat, das in der 
   * Klassenvariablen _sdf definiert ist, um. Wird als Zeit null übergeben, so wird der
   * Text zurückgeliefert, der in der Klassenvariable "_undefiniert" definiert ist
   * zurücgegeben.   
   * @param zeit Umzuwandelde Zeit als Date
   * @return umgewandelte zeit als String
   */
  private String wandleDateInString (Date zeit)
  {
    if (zeit == null)
      return (_undefiniert);
    else
      return _sdf.format(zeit);
  }

  /**
   * Methode prüft ob der übergebene String dem Format der Klassenvariablen _sdf entspricht,
   * und wandelt diesen String in ein Date um. 
   * @param zeit Umzuwandelde Zeit als String
   * @return umgewandelte zeit als Date wenn syntaktisch korrekt, sonst null
   */
  private Date wandleStringInDate (String zeit)
  {
    Date date = null;
    
    if (!zeit.equals( _undefiniert ))
    {
      try 
      {
        date = _sdf.parse ( zeit );  
      } 
      catch (ParseException e) 
      {            
        e.printStackTrace();        
      }
    }
    
    return date;
  }

  /**
   * @param applikationsStart setzt die Klassenvariable m_applikationsStart
   */
  public void setApplikationsStart( Date applikationsStart )
  {
    m_applikationsStart = applikationsStart;
  }

  /**
   * @param applikationsStart setzt die Klassenvariable m_applikationsStart
   */
  public void setApplikationsStart( String applikationsStart )
  {
    m_applikationsStart = wandleStringInDate( applikationsStart );
  }

  /**
   * @param letzteInitialisierung setzt die Klassenvariable m_letzteInitialisierung
   */
  public void setLetzteInitialisierung( Date letzteInitialisierung )
  {
    m_letzteInitialisierung = letzteInitialisierung;
  }

  /**
   * @param letzteInitialisierung setzt die Klassenvariable m_letzteInitialisierung
   */
  public void setLetzteInitialisierung( String letzteInitialisierung )
  {
    m_letzteInitialisierung = wandleStringInDate( letzteInitialisierung );
  }

  /**
   * @param letzterStart setzt die Klassenvariable m_letzterStart
   */
  public void setLetzterStart( Date letzterStart )
  {
    m_letzterStart = letzterStart;
  }

  /**
   * @param letzterStart setzt die Klassenvariable m_letzterStart
   */
  public void setLetzterStart( String letzterStart )
  {
    m_letzterStart = wandleStringInDate( letzterStart );
  }

  /**
   * @param letzterStopp setzt die Klassenvariable m_letzterStopp
   */
  public void setLetzterStopp( Date letzterStopp )
  {
    m_letzterStopp = letzterStopp;
  }
  
  /**
   * @param letzterStopp setzt die Klassenvariable m_letzterStopp
   */
  public void setLetzterStopp( String letzterStopp )
  {
    m_letzterStopp = wandleStringInDate( letzterStopp );
  }

  /**
   * Methode liefert die original Aufrufparameter der Inkarnation als String zurück.
   * Diese sind der StartStopp Datei entnommen 
   * @return Aufrufparameter
   */
  public String getAufrufParameterOriginalAlsString ()
  {
    String p = "";
    for (int i=0; i<getAufrufParameter().size(); ++i)
      p = p + " " + getAufrufParameter().get( i );

    return p;
  }

  /**
   * Methode liefert die tatsächlichen Aufrufparameter der Inkarnation als String zurück.
   * Diese bestehen aus den original Aufrufparametern aus der StartStopp Datei, ggfs. erweitert 
   * um die Parameter -simuVariante und -inkarnationsName
   * @return Aufrufparameter
   */
  public String getAufrufParameterAlsString ()
  {
    String p = getAufrufParameterOriginalAlsString();

    // Erweitern um die Aufrufparameter 
    // -simuVariante=
    // -inkarantionsName=

    // 'normale' Inkarnationen werden immer um die beiden Parameter erweitert
    
    if (!GlobaleDaten.getInstanz().inkarnationGehoertZumKernsystem (getName())) 
    {
      if (getSimulationsVariante() != 0)
        p = p + " -simVariante=" + getSimulationsVariante();
    
      if (getProzessId() != null)
        p = p + " -inkarnationsName=" + getProzessId();
    }
    
    // Inkarnationen des Kernsystems werden nur dann erweitert, wenn der Parameter 
    // mitInkarnationsname in der StartStopp Datei für diese Inkarnation gesetzt wurde
    
    else
    {
       Kernsystem ks = GlobaleDaten.getInstanz().getKernsystemInkarnation( getName() );
       if (ks != null)
       {
         if (ks.isMitInkarnationsname())
           if (getProzessId() != null)
             p = p + " -inkarnationsName=" + getProzessId();
       }
    }
    
    return p;
  }

  /**
   * Methode setzt das Aufrufkommando der Applikation zusammen. Es besteht aus der ausführbaren Datei 
   * plus den Aufrufparametern. Von der StartStopp Applikation werden dann noch die Aufrufparameter 
   * "-simuVariante" und "-inkarnationsName" ergänzt.<br>
   * Der Aufrufparameter "-simuVarinate" wird nur dann ergänzt, wenn die Simulationsvariante ungleich 
   * 0 ist. Diese Ergänzung erfolgt nicht bei Prozessen die zum Kernssystem gehören. 
   * @return kompletter Aufruf der Inkarantion als String
   */
  public String getAufruf ()
  {
    String aufruf = getAusfuehrbareDatei() + " " + getAufrufParameterAlsString();

    return aufruf;
  }

  /**
   * Methode setzt das Aufrufkommando der Applikation zusammen wie es sich aufgrund der Einstellungen
   * in der StartStopp.xml Datei ergibt.. Es besteht aus der ausführbaren Datei plus den definierten
   * Aufrufparametern.  
   * @return kompletter Aufruf der Inkarantion als String
   */
  public String getAufrufOriginal ()
  {
    String aufruf = getAusfuehrbareDatei() + " " + getAufrufParameterOriginalAlsString();

    return aufruf;
  }

  public long getMaxSpeicher ()
  {
    return getMaxSpeicher(getAufrufOriginal());
  }

  /**
   * Methode bestimmt den maximalen Speicher den die Inkarnation benötigt. Hierzu werden 
   * die Aufrufparameter -Xmn -Xms und -Xmx ausgewertet.
   * @return benötigter Speicher in kByte.
   */
  public long getMaxSpeicher (String aufruf)
  {
    long max  = 0l;
    long wert = 0l;
    
    JVMParameterAnalyse jvm = new JVMParameterAnalyse (aufruf);

    wert = jvm.getXmx();
//    System.out.println("MaxHeap (-Xmx) = " + wert);
    if (wert > max)
      max = wert;

    wert = jvm.getXms();
//    System.out.println("MinHeap (-Xms) = " + wert);
    if (wert > max)
      max = wert;

    wert = jvm.getXmn();
//    System.out.println("Start (-Xmn) = " + wert);
    if (wert > max)
      max = wert;
  
//    System.out.println("Max = " + max + " Bytes");
    return max;
  }

  /**
   * @return liefert die Variable betriebssystemProzess zurück
   */
  public BetriebssystemProzess getBetriebssystemProzess()
  {
    return m_betriebssystemProzess;
  }

  /**
   * @param betriebssystemProzess setzt die Variable betriebssystemProzess
   */
  public void setBetriebssystemProzess( BetriebssystemProzess betriebssystemProzess )
  {
    m_betriebssystemProzess = betriebssystemProzess;
  }

  /**
   * @return liefert die Variable fehlerText zurück
   */
  public String getFehlerText()
  {
    return m_fehlerText;
  }

  /**
   * @param fehlerText setzt die Variable fehlerText
   */
  public void setFehlerText( String fehlerText )
  {
    m_fehlerText = fehlerText;
  }

}