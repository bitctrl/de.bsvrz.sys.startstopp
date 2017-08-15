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

import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ListIterator;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.skriptvew.GlobaleDaten;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums;
import de.bsvrz.sys.startstopp.skriptvew.StartStoppApp;
import de.bsvrz.sys.startstopp.skriptvew.StartStoppBlock;
import de.bsvrz.sys.startstopp.skriptvew.StartStoppPräferenz;
import de.bsvrz.sys.startstopp.skriptvew.StartStoppVerwaltung;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_AUSGABE;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_NEUSTART;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_STARTART;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_START_FEHLER_VERHALTEN;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_STOPP_FEHLER_VERHALTEN;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_ZUSTAND;

/**
 * Klasse realisiert eine Inkarnation. In dieser Klasse wird eine Inkarnation (Prozess aus
 * dem StartStopp Block) gestartet und gewartet bis der Prozess beendet ist. Start- und
 * Stopbedingungen des Prozesse 
 * @author Dambach Werke GmbH
 *
 */
public class Inkarnation implements Serializable
{
  private static final long serialVersionUID = 4181666756366500501L;

  /**
   * Debug Einstellungen
   */
  private static final Debug logger = Debug.getLogger();
  
  /**
   * Debug Einstellung
   */
  private final boolean _debug = false;
  
  /**
   * Singelton Instanz für Klasse ProzessVerwaltung
   */
  private ProzessVerwaltung m_prozessVerwaltung;
  
  /**
   * Prozessdaten der Inkarantion
   */
  private ProzessDaten m_pd;

  /**
   * eigentlicher gestarteter Process
   */
  private Process m_proc = null;
  
  /**
   * Singelton Instanz für Klasse DaVKommunikation
   */
  private DaVKommunikation m_daVKommunikation = null;
  
  /**
   * Singelton Instanz für Klasse GlobaleDaten
   */
  private GlobaleDaten m_globaleDaten = null;
  
  /**
   * Prozess Id
   */
  private String m_prID = null;
  
  /**
   * Zustand der Inkarnation
   */
  private E_ZUSTAND m_zustand = null;
  
  /**
   * Alter Zustand der Inkarantion
   */
  private E_ZUSTAND m_oldZustand = null;
  
  /**
   * Merker zum Beenden des Startvorgangs
   */
  private boolean m_beendeStart = false;

  /**
   * Merker zum Beenden des Stoppvorgangs
   */
  private boolean m_beendeStopp = false;

  /**
   * Singelton Instanz für Klasse Logbuch
   */
  private Logbuch m_logbuch = null;
  
  /**
   * Name der Inkarnation
   */
  private String m_name = null;

  /**
   * Merker ob Inkarnation gestoppt werden soll
   */
  private boolean m_stoppen;
  
  /**
   * Thread zum Starten der Inkarnation
   */
  private ThreadStarteInkarnation m_threadStarteInkarnation = null;

  /**
   * Thread zum Stoppen der Inkarnation
   */
  private ThreadStoppeInkarnation m_threadStoppeInkarnation = null;

  /**
   * Thread zum Neustarten der Inkarnation
   */
  private ThreadNeuStartInkarnation m_threadNeuStartInkarnation = null;

  /**
   * Thread zum zyklischen Starten der Inkarnation
   */
  private ThreadStarteInkarnationZyklisch m_threadStarteInkarnationZyklisch = null;

  /**
   * Verweis auf Betriebssystemebene der Inkarantion
   */
  private BetriebssystemProzess m_betriebssystemProzess = null;

  /**
   * Instanz für Preferenzeinstellungen
   */
  private StartStoppPräferenz m_startStoppSperren = null;

  /**
   * Konstruktor der Klasse
   * @param prID ProzessID der Inkarnation
   */
  public Inkarnation (String prID)
  {
    m_prozessVerwaltung = ProzessVerwaltung.getInstanz();
    
    m_daVKommunikation = DaVKommunikation.getInstanz();
    
    m_globaleDaten = GlobaleDaten.getInstanz();
    
    m_logbuch = Logbuch.getInstanz();
    
    m_prID = prID;

    m_pd = m_prozessVerwaltung.getProzessDaten( m_prID ); 

    m_startStoppSperren = new StartStoppPräferenz (m_pd);
    
    m_name = m_pd.getName();

    m_beendeStart = false;
    
    m_beendeStopp = false;

    setzeZustand (E_ZUSTAND.ANGELEGT, 0);
    
    // Prüfen ob bereits ein Prozess von dieser Inkarnation läuft, wenn ja
    // wird der Prozess beendet.
    
    m_betriebssystemProzess= new BetriebssystemProzess (m_pd);
    
    m_pd.setBetriebssystemProzess( m_betriebssystemProzess );
    
    while (m_betriebssystemProzess.isGestartet())
    {
      logger.error(m_name + " ist bereits aktiv und wird beendet (Pid " + 
                         m_betriebssystemProzess.getPid() + ") !");
      
      m_betriebssystemProzess.beendeProzess();
    }
  }
  
  //---------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------
  //
  // Methoden/Klassen zum Starten einer Inkarnation
  //
  //---------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------
  
  /**
   * Methode zum Starten einer Inkarantion. Die Methode startet einen Thread der das eigentliche
   * Starten der Inkarnation veranlasst. Dieser Thread wartet dann auf des Ende der Inkarnation.
   * @param absender Absender der das Starten der Inkarnation veranlasst hat
   * @param delay Verzögerung des Starts in Sekunden
   */
  public void starteInkarnation (long absender, long delay)
  {
    // Manueller Start (absender = -1) bzw. Start durch DaV (absender > 0) löscht das Flag wieder
    // (Automatischer Start nicht).
    
    if (absender != 0)
    {
      m_beendeStart = false;
      m_stoppen     = false;
    }
    
    m_pd.setAnzFehlerStopp (0L);

    // Thread läuft noch
    
    if (m_threadStarteInkarnation != null)
    {
      m_threadStarteInkarnation.m_absender       = absender;
      m_threadStarteInkarnation.m_delay          = delay;
      
      m_threadStarteInkarnation.m_neuerDurchlauf = true;
    }
    else
    {
      m_threadStarteInkarnation = new ThreadStarteInkarnation (absender, delay);
      m_threadStarteInkarnation.start();
      
      if (m_name != null)
        m_threadStarteInkarnation.setName( "starteInkarantion " + m_name );
    }
  }

  /**
   * Interne Klasse zum Starten einer Inkarnation. Der Startvorgang wird als eigener Thread 
   * ausgeführt um zeitliche Abhängigkeiten (wie sleeps) zu berücksichtigen. Neben den 
   * Inkarnationsspezifischen Prüfungen wird auch geprüft, ob das Kernsystem läuft (bzw. 
   * darauf gewartet)
   */

  private class ThreadStarteInkarnation extends Thread
  {
    private long m_absender = 0l;
    
    private long m_delay = 0l;
    
    private boolean m_neuerDurchlauf = false;
    
    /**
     * Konstruktor der Klasse
     * @param absender Absender
     */
    public ThreadStarteInkarnation (long absender, long delay)
    {
      m_absender = absender;
      
      m_neuerDurchlauf = false; 
    }
    
    /**
     * Run Methode des Threads
     */
    public void run ()
    {
      while (m_betriebssystemProzess.isGestartet())
      {
        logger.error(m_name + " ist bereits aktiv und wird beendet (Pid " + 
                           m_betriebssystemProzess.getPid() + ") !");
        
        m_betriebssystemProzess.beendeProzess();
      }

      // Beim Starten der Inkarnation wird geprüft, ob das Kernsystem bereits
      // läuft. Wenn nicht, dann wird hier gewartet.

      if (!m_globaleDaten.inkarnationGehoertZumKernsystem (m_pd.getName()))
      {
        while (!m_prozessVerwaltung.isKernSystemGestartet() && !m_stoppen)
          mySleep (1000);
        
        // Beim ersten Starten der Inkarnation wird geprüft, ob die Inkarnation auf dem Rechner
        // schon läuft.
      }

      if (!m_stoppen)
      {
        do
        {
          // Verzögerungszeit
          
          if (m_delay > 0l)
            mySleep( m_delay * 1000 );
          
          m_neuerDurchlauf = false;
          
          if (!m_stoppen)
            starteInkarnation (m_absender);
        }
        while (m_neuerDurchlauf && !m_stoppen);
        
        m_threadStarteInkarnation = null;
      }
    }

    /**
     * Methode zum Starten einer Inkarnation unter Berücksichtigung der
     * Startbedinung
     */
    private void starteInkarnation (long absender)
    {
      logbuch ("Starte Inkarnation");

      if (m_beendeStart)
      {
        return;
      }
      
      if (m_pd.isGestartet() || m_pd.isInStartPhase())
      {
        return;
      }

      // Speicherbedarf prüfen
      
      String s = "";
      
      long maxSpeicher = m_pd.getMaxSpeicher();

      s = m_name + " benötigter Speicher: " + maxSpeicher / 1024 + " KB ";

      long freierSpeicher = new Memory().getFreierSpeicher();
      
      s += " freier Speicher: ";
        
      if (freierSpeicher != -1)
        s += freierSpeicher / 1024 + " KB";
      else
        s += "nicht ermittelbar";
        
      logbuch(s);

      if (freierSpeicher != -1)
      {
        if (maxSpeicher > freierSpeicher)
        {
          s =  m_name + " Nicht genügend Speicher zum Starten der Inkarnation frei ! " +
               "Benötigter Speicher: " + maxSpeicher / 1024 + " KB, " +
               "freier Speicher: " + freierSpeicher / 1024 + " KB";
          
          logger.error(s);
        }
      }
        
      // Sonderfall: Inkarnationen die zum Kernsystem gehören werden in der
      // Reihenfolge gestartet wie sie im StartStopp Block definiert sind,
      // hier werden die Start- bzw. Stoppbedingungen nicht ausgewertet.
      
      if (!m_globaleDaten.inkarnationGehoertZumKernsystem (m_pd.getName()))
      {
        logbuch ("Prüfe Startbedingung");
        
        // Warte bis alle Startbedingung erfüllt sind
  
        if (!isStartBedingungErfuellt())
        {
          setzeZustand (E_ZUSTAND.STARTENWARTEN, absender);
          
          logbuch ("Warte bis Startbedingungen erfüllt sind");
        }
  
        while (!isStartBedingungErfuellt() && !m_beendeStart && !StartStoppApp.isStartStoppWirdBeendet())
          mySleep (1000);

        if (m_beendeStart)
        {
          logbuch ("Warten auf Startbedingung wurde abgebrochen");
          
          setzeZustand (E_ZUSTAND.GESTOPPT, absender);
          return;
        }
      }

      if (StartStoppApp.isStartStoppWirdBeendet())
      {
        logbuch ("StartStoppApplikation wird beendet");
        
        setzeZustand (E_ZUSTAND.GESTOPPT, absender);
        return;
      }
      
      setzeZustand (E_ZUSTAND.STARTEN, absender);
      
      // Starten der Anwendung
      
      m_proc = StarteProzesse.getInstanz().start( m_pd );
 
      if (m_proc == null)
      {
        if (StartStoppApp.isStartStoppWirdBeendet())
        {
          logbuch ("StartStoppApplikation wird beendet");
          
          setzeZustand (E_ZUSTAND.GESTOPPT, absender);
          return;
        }
          
        setzeZustand (E_ZUSTAND.FEHLER, absender);
        
        if (!m_stoppen)
          startVerhaltenBeiFehler (absender);
      }
      else
      {
        logbuch(m_name + " gestartet (Pid "  + m_betriebssystemProzess.getPid() + " ).");
        
        setzeZustand (E_ZUSTAND.GESTARTET, absender);

        // Prozess selbst in Prozessdaten übernehmen
        
        m_pd.setProzess( m_proc );

        // Umlenken der Standard- und Standardfehlerausgabe

        InputStream streamStandardAusgabe = m_proc.getInputStream();  
        InputStream streamFehlerAusgabe   = m_proc.getErrorStream();

        new AusgabeVerarbeitung ( m_pd.getName(),
             
                                  streamStandardAusgabe, 
                                  m_pd.getStandardAusgabe().getDateiName(),
                                  m_pd.getStandardAusgabe().getMaxGroesse(),
                                  m_pd.getStandardAusgabe().getOption(),
                                   
                                  streamFehlerAusgabe,
                                  m_pd.getStandardFehlerAusgabe().getDateiName(),
                                  m_pd.getStandardFehlerAusgabe().getMaxGroesse(),
                                  m_pd.getStandardFehlerAusgabe().getOption());

        // Warten auf Prozessende
        
        ueberwacheProzess (absender);     

      } // if (proc != null)
    }

    /**
     * eigene Sleep Methode
     * @param dauer Dauer in Millisekunden
     */
    private void mySleep (long dauer)
    {
      try
      {
        sleep (dauer);
      }
      catch (InterruptedException e)
      {
      }
    }
  }

  //---------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------
  //
  // Methoden/Klassen zum zyklischen Starten einer Inkarnation (Intervall)
  //
  //---------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------

  /**
   * Methode zum zyklischen Starten einer Inkarantion. Die Methode startet einen Thread der 
   * das eigentliche zyklische Starten der Inkarnation veranlasst. Dieser Thread läuft
   * solange, bis die Applikation beendet wird oder die Startart der Inkarnation geändert
   * wird.
   * @param absender Absender
   */
  public void starteInkarnationZyklisch (long absender)
  {
    if (m_threadStarteInkarnationZyklisch != null)
    {
      // Thread läuft schon, kein Eingreifen notwendig
    }
    else
    {    
      m_threadStarteInkarnationZyklisch = new ThreadStarteInkarnationZyklisch (absender);
      m_threadStarteInkarnationZyklisch.start();
      
      if (m_name != null)
        m_threadStarteInkarnationZyklisch.setName( "starteInkarantionZyklisch " + m_name );

    }
  }

  /**
   * Interne Klasse zum zyklischen Starten einer Inkarnation. Der Startvorgang wird als eigener Thread 
   * ausgeführt
   */

  private class ThreadStarteInkarnationZyklisch extends Thread
  {
    private long m_absender = 0l;
    
    /**
     * Konstruktor der Klasse
     * @param absender Absender
     */
    public ThreadStarteInkarnationZyklisch (long absender)
    {
      m_absender = absender;
    }
    
    /**
     * Run Methode des Threads
     */
    public void run ()
    {
      if (!m_globaleDaten.inkarnationGehoertZumKernsystem (m_pd.getName()))
      {
        while (!m_prozessVerwaltung.isKernSystemGestartet() && !m_stoppen)
          mySleep (1000);
      }

      starteInkarnationZyklisch ();
      
      m_threadStarteInkarnationZyklisch = null;
    }

    /**
     * Methode zum Zyklischen Starten einer Inkarnation unter Berücksichtigung der
     * Startbedingung
     */
    private void starteInkarnationZyklisch ()
    {
      // Zeitpunkt des nächsten Starts bestimmen
      
      logbuch ("Starte Inkarnation zyklisch");

      String intervall = m_pd.getStartArt().getIntervallZeit();
      
      TimeIntervalCron t = new  TimeIntervalCron ();
      
      t.setFields( intervall );
      
      if (t.hasParseErrors())
      {
        logger.error(m_pd.getName() + " Fehlerhafte Intervallzeit: " + intervall);
        return;
      }
      else
      {
        SimpleDateFormat sdf = new SimpleDateFormat("mm");  // nur Minute
        String oldMinute = sdf.format(new Date());
        String aktMinute = null;
        
        boolean inDieserMinuteGestartet = false; // Merker damit die Applikation nicht innerhalb einer
                                                 // Minute mehrfach gestartet wird
        
        while (!StartStoppApp.isStartStoppWirdBeendet() &&
               (m_pd.getStartArt().getOption() == E_STARTART.INTERVALL) &&
               !StartStoppApp.isStartStoppWirdBeendet())
        {
          // Prüfen ob neue Minute
          
          aktMinute = sdf.format(new Date());
          
          if (!oldMinute.equals( aktMinute))
          {
            oldMinute = aktMinute;
            inDieserMinuteGestartet = false;
          }
          
          // Prüfen auf Änderung der Intervallzeit (z.B. durch Telnet)
          
          if (!m_pd.getStartArt().getIntervallZeit().equals( intervall ))
          {
            intervall = m_pd.getStartArt().getIntervallZeit();
            
            t.setFields( intervall );
            
            if (t.hasParseErrors())
            {
              logger.error(m_pd.getName() + " Fehlerhafte Intervallzeit: " + intervall);
              return;
            }
          }
          
          Calendar c = t.getNextRun( null );
          
          if (c == null)
          {
            logger.error(m_pd.getName() + " Keine Schaltung möglich: " + intervall);
            return;
          }
          else
          {
            m_pd.setNaechsterStart( t.getNextRun( null ).getTime() );
            
            if (t.shouldRun( new Date () ))
            {
              if (!m_pd.isGestartet()                   && 
                  !m_pd.isInStartPhase()                && 
                  !inDieserMinuteGestartet              && 
                  !StartStoppApp.isStartStoppWirdBeendet())
              {
                starteInkarnation( m_absender, 0l );
                
                m_pd.setAnzahlNeustart( m_pd.getAnzahlNeustart() + 1 );
                
//                System.out.println("---> Start");
                
                inDieserMinuteGestartet = true;
              }
            }
          }

          mySleep (1000);
          
        } // while
      }
      
      logbuch("Thread zyklisch beendet");
    }

    /**
     * eigene Sleep Methode
     * @param dauer Dauer in Millisekunden
     */
    private void mySleep (long dauer)
    {
      try
      {
        sleep (dauer);
      }
      catch (InterruptedException e)
      {
      }
    }
  }

  //---------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------
  //
  // Methoden/Klassen zum Stoppen einer Inkarnation
  //
  //---------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------

  /**
   * Methode zum Stoppen einer Inkarantion. Die Methode startet einen Thread der 
   * das eigentliche Stoppen der Inkarnation veranlasst. Abhängig von dem Parameter
   * stoppeSofort wartet der Thread wird die Stoppbedingung ausgewertet oder nicht. 
   * @param absender Absender der das Stoppen auslöst
   * @param stoppeSofort Sofort abbrechen oder Stoppbedinung bzw. Verzögerungszeit
   * noch berücksichtigen
   */
  public void stoppeInkarnation (long absender, boolean stoppeSofort)
  {
    try // MO 27.08.12 Bug 26
    {
      m_pd.setAnzFehlerStart (0L);
      
      if (absender != 0)
        m_beendeStopp = false;
      
      if (m_beendeStopp)
        return;
  
      //  Thread läuft bereits --> Sofort Stoppen an Thread übergeben
      
      if (m_threadStoppeInkarnation != null) 
      {
        m_threadStoppeInkarnation.m_stoppeSofort = stoppeSofort;
      }
      else
      {
        m_threadStoppeInkarnation = new ThreadStoppeInkarnation(absender, stoppeSofort, m_proc);
      
        m_threadStoppeInkarnation.start();
        
        if (m_name != null)
          m_threadStoppeInkarnation.setName( "stoppeInkarantion " + m_name );
  
      }
    }
    catch (Exception m_e)
    {
    }
  }

  /**
   * Interne Klasse zum Stoppen einer Inkarnation. Der Stoppvorgang wird als eigener Thread 
   * ausgeführt
   */

  private class ThreadStoppeInkarnation extends Thread
  {
    private long    m_absender = 0l;
    
    private boolean m_neuerDurchlauf = false;
    
    private boolean m_stoppeSofort = false;
    
    private Process m_proc = null;
    
    /**
     * Konstruktor der Klasse
     * @param absender Absender
     * @param stoppeSofort Soll Applikation sofort (d.h. ohne Prüfung der Stoppbedingungen
     * und der Delayzeit) beendet werden
     */
    public ThreadStoppeInkarnation (long absender, boolean stoppeSofort, Process proc)
    {
      m_stoppeSofort = stoppeSofort;
      
      m_absender = absender;
      
      m_neuerDurchlauf = false;
      
      m_proc = proc;
    }

    /**
     * Run Methode des Threads
     */
    public void run ()
    {
      do
      {
        m_neuerDurchlauf = false;
        
        stoppeInkarnation (m_absender);
      }
      while (m_neuerDurchlauf);

      m_threadStoppeInkarnation = null;
    }

    /**
     * Methode zum eigentlichen Stoppen der Inkarnation
     * @param absender Absender
     */
    private void stoppeInkarnation (long absender)
    {
      logbuch ("Stoppe Inkarnation");

      // Merker setzen
      
      m_stoppen = true;
      
      // Prüfen ob Inkarnation sich in der Wartephase der Startbedingungen befindet
      // wenn ja dann wird der Startvorgang abgebrochen
      
      if (m_pd.isInStartPhase())
      {
        m_beendeStart = true;
      }

      // Prüfen ob Inkarnation überhaupt läuft
      
      if (!m_pd.isGestartet())
      {
        logbuch ("Inkarnation nicht gestartet --> Return");
        return;
      }
      
      if (m_pd.isGestoppt())
      {
        logbuch ("Inkarnation bereits gestoppt --> Return");
        return;
      }

      // Sonderfall: Inkarnationen die zum Kernsystem gehören werden in der
      // Reihenfolge gestartet wie sie im StartStopp Block definiert sind,
      // hier werden die Start- bzw. Stoppbedingungen nicht ausgewertet.
      // Warte bis alle Stoppbedingung erfüllt sind

      if (!m_globaleDaten.inkarnationGehoertZumKernsystem (m_pd.getName()))
      {
        logbuch ("Prüfe Stoppbedingung");
        
        if  (!isStoppBedingungErfuellt() && !m_stoppeSofort)
        {
          setzeZustand (E_ZUSTAND.STOPPENWARTEN, absender);
          logbuch ("Warte bis Stoppbedingungen erfüllt sind");
        }
  
        while (!isStoppBedingungErfuellt() && !m_stoppeSofort && !m_beendeStopp)
        {
          try
          {
            sleep (1000);
          }
          catch (InterruptedException e)
          {
          }
        }
      }

      if (m_beendeStopp)
      {
        logbuch ("Stoppen: Stoppvorgang abgebrochen");
        return;
      }

      boolean fertig = true;
      
      do
      {
        // Loop beenden, wenn neuer Prozess gestartet wird
        
        if ((Inkarnation.this.m_proc != null) && (!Inkarnation.this.m_proc.equals(m_proc)))
        {
          logger.error("Stoppe Inkarantion " + m_name + " durch Vergleich beendet !");

          break;
        }
        
        // Prozess beenden
  
        m_proc.destroy();
  
        // Max. 5 Sekunden warten auf Beenden von Prozess
        
        int counter = 0;
        while (!m_pd.isGestoppt() && (counter < 5))
        {
          m_proc.destroy();
  
          try
          {
            sleep (1000);
          }
          catch (InterruptedException e1)
          {
          }
          
          counter++;
        }
        
        // Zustand gestoppt nicht erreicht
        // --> Stoppverhalten bei Fehler
        
        if (!m_pd.isGestoppt())
        {
          if (m_stoppeSofort)
          {
            m_betriebssystemProzess.beendeProzess();
          }
          else
          {
            logger.error("Inkarantion " + m_name + " lässt sich nicht beenden !");
            fertig = stoppVerhaltenBeiFehler (absender);
          }
        }
        
      } while (!fertig);
    }
  }

  //---------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------
  //
  // Methoden/Klassen zum Neustarten einer Inkarnation
  //
  //---------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------

  /**
   * Methode zum Neustarten einer Inkarantion. Die Methode startet einen Thread der 
   * das eigentliche Neustarten der Inkarnation veranlasst. Beim Neustart wird 
   * 
   */
  public void neuStartInkarnation (long absender, long simulationsVariante)
  {
    m_pd.setAnzFehlerStart (0L);
    m_pd.setAnzFehlerStopp (0L);
    
    if (m_threadNeuStartInkarnation != null)
    {
      
    }
    else
    {
      m_threadNeuStartInkarnation = new ThreadNeuStartInkarnation (absender, simulationsVariante);
      m_threadNeuStartInkarnation.start();
      
      if (m_name != null)
        m_threadNeuStartInkarnation.setName( "neuStartInkarantion " + m_name );

    }
  }

  /**
   * Interne Klasse zum Neustarten einer Inkarnation. Der Neustart wird als eigener Thread 
   * ausgeführt
   */

  private class ThreadNeuStartInkarnation extends Thread
  {
    private long m_absender = 0l;
    
    private long m_simulationsVariante = 0l;
    
    /**
     * Konstruktor der Klasse
     * @param absender Absender
     * @param simulationsVariante Simulationsvariante
     */
    public ThreadNeuStartInkarnation (long absender, long simulationsVariante)
    {
      m_absender = absender;
      
      m_simulationsVariante = simulationsVariante;
    }
    
    /**
     * Run Methode des Threads
     */
    public void run ()
    {
      neuStartInkarnation ();

      m_threadNeuStartInkarnation = null;
    }

    /**
     * Eigentliche Methode zum Neustarten der Inkarnation
     */
    private void neuStartInkarnation ()
    {
      if (m_pd.isInStartPhase() || m_pd.isGestartet() || m_pd.isInitialisiert())
      {
        logbuch ("Neustart Inkarnation -> Stoppen");
        
        stoppeInkarnation( m_absender, false );
        
        logbuch ("Neustart Inkarnation -> Warten bis getoppt");
        
        while (m_pd.isInStartPhase() || m_pd.isGestartet())
        {
          try
          {
            sleep (1000);
          }
          catch (InterruptedException e)
          {
          }
        }
      }
      
      logbuch ("Neustart Inkarnation -> Starten");

      m_pd.setSimulationsVariante( m_simulationsVariante );
      
      starteInkarnation( m_absender, 0l );
    }
  }
  
  //---------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------
  //
  // Methoden/Klassen zum Überwachen des laufenden Prozesses
  //
  //---------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------

  /**
   * Methode die wartet, bis ein Prozess beendet ist
   *
   */
  private void ueberwacheProzess(long absender)
  {
    logbuch ("Überwachen des Prozess");

    try
    {
      m_proc.waitFor();
      
      setzeZustand (E_ZUSTAND.GESTOPPT, absender);
      
      logbuch ("Prozess beendet");
    }
    catch ( InterruptedException e )
    {
      logger.error("ueberwacheProzess: " + e.getMessage());
    }

    // Prüfen wie lange die Applikation lief
    
    Date end = new Date ();
    
    Date start = m_pd.getApplikationsStart ();
    
    long differenz = end.getTime() - start.getTime();
    differenz /= 1000; // Sekunden
    
    if (_debug)
      System.out.println("Differenz = " + differenz + " Sekunden");
    
    if ((!m_stoppen) && (!StartStoppApp.isStartStoppWirdBeendet()))
    {
      // Wird innerhalb der hier angegebenen Zeit der Prozess beendet, so
      // wird davon ausgegangen, dass der Start des Prozesses fehlschlug, bzw.
      // dass der Prozess abgestürzt ist. Auch in diesem Fall, wird das 
      // "Startverhalten bei Fehlern" durchlaufen. 
      
      int d = 1; // Zeit in Sekunden
      
      if (differenz < d)
      {
        String s = "Prozess \"" + m_name + "\" lief nur " + differenz + " Sekunden --> StartVerhaltenFehler ";

        logger.error(s);
        logbuch ( s );
        
        startVerhaltenBeiFehler (absender);
      }
      else
      {
        // Bei Startart "automatisch" wird nach Beendigung einer Inkarnation
        // geprüft, ob die Inkarnation erneut gestartet werden soll, wenn
        // ja dann erfolgt hier der Neustart

        if (m_pd.getStartArt().getOption() == E_STARTART.AUTOMATISCH)
        {
          if (m_pd.getStartArt().getNeuStart() == E_NEUSTART.Ja)
          {
            logbuch ("Nachstarten");
            
            starteInkarnation( absender, 0l );
          }
        }
      }
    }
  }

  //---------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------
  //
  // Methoden/Klassen zum Setzen des Zustand der Inkarnation
  //
  //---------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------

  /**
   * Methode zum Setzen des Status einer Inkarnation und Publizieren des
   * Status über den DaV. Bei Änderungen des Status wird eine entsprechende
   * Betriebsmeldung versendet.
   * @param zustand neuer Status
   * @param absender Absender
   */
  public void setzeZustand (E_ZUSTAND zustand, long absender)
  {
    // Kontrollausgabe
    
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    String uhrzeit = sdf.format(new Date());

    String buffer = " Inkarnation \"" + m_pd.getName() + "\" Status: " + zustand.getText() + " Absender: ";
    if (absender == -1)
      buffer += " Nutzerschnittstelle";
    else
      buffer += "Pid: " + absender;  
    
    logbuch( buffer );
    
    if (_debug)
      System.out.println(uhrzeit + buffer);

    // Status in Membervariablen merken
    
    m_zustand = zustand;

    // Betriebsmeldung absetzen bei Änderung
    
    if (m_oldZustand == null)
      m_daVKommunikation.sendeBetriebsmeldung( buffer );
    else      
      if (m_zustand != m_oldZustand)
        m_daVKommunikation.sendeBetriebsmeldung( buffer );
  
    m_oldZustand = m_zustand;
    
    // Status setzen
    
    m_pd.setZustand ( zustand );
    
    // Status publizieren
    
    m_daVKommunikation.sendeProzessInfo( );
      
    // Telnet Oberfläche aktualisieren
    
    if (StartStoppApp.getNutzerSchnittstelle() != null)
      StartStoppApp.getNutzerSchnittstelle().aktualisiereProzessEintraege();
    
    // Preferenz Einträge

    if (m_zustand == E_ZUSTAND.GESTARTET)
      m_startStoppSperren.merkePid( m_pd.getBetriebssystemProzess().getPid() );
    
    if (m_zustand == E_ZUSTAND.GESTOPPT)
      m_startStoppSperren.loescheApplikationGestartet();
  }

  //---------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------
  //
  // Methoden/Klassen zum Prüfen der Startbedingungen
  //
  //---------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------

  /**
   * Methode rüft ob alle Startbedingungen eines Prozesses erfüllt sind. 
   * @return true Startbedingungen sind erfüllt, false mindestens ein Startbedinung 
   * ist nicht erfüllt.
   */
  public boolean isStartBedingungErfuellt ()
  {
    int anzBedingungen          = 0;
    int anzErfuellteBedingungen = 0;
    
    ListIterator<StartBedingung> liste = m_pd.getStartBedingung().listIterator();
    while (liste.hasNext())
    {
      StartBedingung b = liste.next();
      
      // Bedingungen prüfen
      
      anzBedingungen++;

      if (b.isErfuellt())
        anzErfuellteBedingungen++;
    }
      
    if (_debug)
      System.out.println("Starten " + m_name + " AnzBedingungen = " + anzBedingungen + " Anz erfüllt = " + anzErfuellteBedingungen);
    
    if (anzBedingungen == anzErfuellteBedingungen)
      return true;
    else
      return false;
  }

  //---------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------
  //
  // Methoden/Klassen zum Prüfen der Stoppbedingungen
  //
  //---------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------

  /**
   * Methode rüft ob alle Stoppbedingungen eines Prozesses erfüllt sind 
   * @return true Stoppbedingungen sind erfüllt, false mindestens ein Stoppbedinung 
   * ist nicht erfüllt.
   */
  public boolean isStoppBedingungErfuellt ()
  {
    int anzBedingungen          = 0;
    int anzErfuellteBedingungen = 0;
    
    ListIterator<StoppBedingung> liste = m_pd.getStoppBedingung().listIterator();
    while (liste.hasNext())
    {
      StoppBedingung b = liste.next();

      // Bedingungen prüfen
      
      anzBedingungen++;
      
      if (b.isErfuellt())
        anzErfuellteBedingungen++;
    }
      
    if (_debug)
      System.out.println("Stoppen " + m_name + " AnzBedingungen = " + anzBedingungen + " Anz erfüllt = " + anzErfuellteBedingungen);
    
    if (anzBedingungen == anzErfuellteBedingungen)
      return true;
    else
      return false;
  }
  
  /**
   * Methode zum Auswerten des Startverhaltens bei Fehler
   * @param absender Absender
   */
  private void startVerhaltenBeiFehler(long absender)
  {
    String buffer = null;
    
    m_pd.setAnzFehlerStart( m_pd.getAnzFehlerStart() + 1 );

    buffer = "Fehler beim Starten der Inkarnation: " + m_name + " Startversuch = " + m_pd.getAnzFehlerStart() +
             " Anzahl Versuche = " + m_pd.getStartVerhaltenFehler().getWiederholungen();
    
    logbuch (buffer);
    
    logger.error(buffer);

    if (m_pd.getAnzFehlerStart() < m_pd.getStartVerhaltenFehler().getWiederholungen())
    {
      long delay = 10;
      
      starteInkarnation( absender, delay );
      return;
    }
   
    // Initialisieren für nächsten Start
    
    m_pd.setAnzFehlerStart(0L);
    
    // Gesamter Start-Vorgang abbrechen und alle gestarteten Applikationen beenden
    
    if (m_pd.getStartVerhaltenFehler().getOption() == E_START_FEHLER_VERHALTEN.BEENDEN)
    {
      buffer = "startVerhaltenBeiFehler: beenden";
      
      logbuch (buffer);
      
      m_prozessVerwaltung.beendeStartVorgang (true);
    }
    
    // Gesamten Start-Vorgang abbrechen und alle bereits gestarteten Applikationen laufen lassen
    
    if (m_pd.getStartVerhaltenFehler().getOption() == E_START_FEHLER_VERHALTEN.ABBRUCH)
    {
      buffer = "startVerhaltenBeiFehler: abbrechen";
      
      logbuch (buffer);
      
      m_prozessVerwaltung.beendeStartVorgang (false);
    }
    
    // Problem ignorieren und nächste Applikation starten
    
    if (m_pd.getStartVerhaltenFehler().getOption() == E_START_FEHLER_VERHALTEN.IGNORIEREN)
    {
      buffer = "startVerhaltenBeiFehler: ignorieren";
      
      logbuch (buffer);
    }
  }

  /**
   * Methode zum Auswerten des Stoppverhaltens bei Fehler
   * @param absender Absender
   * @return true: Stoppen erneut veruschen, false: Stoppvorgang beendet
   */
  private boolean stoppVerhaltenBeiFehler(long absender)
  {
    String buffer = null;
    
    m_pd.setAnzFehlerStopp( m_pd.getAnzFehlerStopp() + 1 );

    buffer = "Fehler beim Stoppen der Inkarnation: " + m_name + " Stoppversuch = " + m_pd.getAnzFehlerStopp() +
             " Anzahl Versuche = " + m_pd.getStoppVerhaltenFehler().getWiederholungen();
    
    logbuch (buffer);
    
    if (m_pd.getAnzFehlerStopp() < m_pd.getStoppVerhaltenFehler().getWiederholungen())
    {
      return false; // Nächster Versuch
    }

    // Initialisieren für nächsten Start
    
    m_pd.setAnzFehlerStopp (0L);

    // Fehlermeldung für Oberfläche
    
    String fehlerMeldung = "Prozess konnte nicht gestoppt werden\n\r";
    
    // Gesamter Stopp-Vorgang abbrechen
    
    if (m_pd.getStoppVerhaltenFehler().getOption() == E_STOPP_FEHLER_VERHALTEN.ABBRUCH)
    {
      buffer = "stoppVerhaltenBeiFehler: Stoppvorgang abbrechen";
      m_pd.setFehlerText( fehlerMeldung +
                          "Verhalten im Fehlerfall: Stoppvorgang abbrechen !\n" );
      
      logbuch (buffer);
      
      m_prozessVerwaltung.beendeStoppVorgang ();
    }
    
    // Stopp der Applikation erzwingen
    
    if (m_pd.getStoppVerhaltenFehler().getOption() == E_STOPP_FEHLER_VERHALTEN.STOPP)
    {
      buffer = "stoppVerhaltenBeiFehler: Stopp der Applikation erzwingen";
      m_pd.setFehlerText( fehlerMeldung +
                          "Verhalten im Fehlerfall: Stoppvorgang durch Betriebssystem erzwingen !\n" );

      logbuch (buffer);
      
      m_betriebssystemProzess.beendeProzess();
    }
    
    // Problem ignorieren
    
    if (m_pd.getStoppVerhaltenFehler().getOption() == E_STOPP_FEHLER_VERHALTEN.IGNORIEREN)
    {
      buffer = "stoppVerhaltenBeiFehler: ignorieren";
      m_pd.setFehlerText( fehlerMeldung +
                          "Verhalten im Fehlerfall: Fehler ignorieren !\n" );
      
      logbuch (buffer);
    }
    
    return true;
  }

  // Methode zum Beenden des Startvorgangs einer Inakrantion. Der Startvorgang
  // kann nur beendet werden, wenn eine Inkarnation in der Phase ist, in der 
  // sie auf Ihre Startbedinung, bwz. auf die Delayzeit beim Starten wartet.
  
  public void beendeStart ()
  {
    logbuch ("Startvorgang beendet");
    
    m_beendeStart = true;
  }

  /**
   * Methode zum Beenden des Stoppvorgangs
   */
  public void beendeStopp()
  {
    logbuch ("Stoppvorgang beendet");
    
    m_beendeStopp = true;
  }

  /**
   * Inkarantionsspezifischen Eintag in Logbuch schreiben.
   * @param text Text für Logbuch
   */
  private void logbuch (String text)
  {
    logger.info( m_pd.getName(), text );
  }

  /**
   * @return liefert die Variable name zurück
   */
  public String getName()
  {
    return m_name;
  }

  /**
   * @param name setzt die Variable name
   */
  public void setName( String name )
  {
    m_name = name;
  }

  /**
   * @return liefert die Variable pd zurück
   */
  public ProzessDaten getPd()
  {
    return m_pd;
  }

  /**
   * Methode liefert den eigentlichen Prozess zurück
   * @return Prozess
   */
  public Process getProzess()
  {
    return m_proc;
  }
}
