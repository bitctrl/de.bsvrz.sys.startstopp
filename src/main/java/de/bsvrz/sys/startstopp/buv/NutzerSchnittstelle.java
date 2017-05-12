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

package de.bsvrz.sys.startstopp.buv;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.prozessvew.BetriebssystemProzess;
import de.bsvrz.sys.startstopp.prozessvew.DaVKommunikation;
import de.bsvrz.sys.startstopp.prozessvew.ProzessDaten;
import de.bsvrz.sys.startstopp.prozessvew.ProzessVerwaltung;
import de.bsvrz.sys.startstopp.prozessvew.StartBedingung;
import de.bsvrz.sys.startstopp.prozessvew.StoppBedingung;
import de.bsvrz.sys.startstopp.prozessvew.TimeIntervalCron;
import de.bsvrz.sys.startstopp.skriptvew.GlobaleDaten;
import de.bsvrz.sys.startstopp.skriptvew.StartStoppApp;
import de.bsvrz.sys.startstopp.skriptvew.StartStoppBlock;
import de.bsvrz.sys.startstopp.skriptvew.StartStoppEigenschaften;
import de.bsvrz.sys.startstopp.skriptvew.StartStoppVerwaltung;
import de.bsvrz.sys.startstopp.skriptvew.Versionierung;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_NEUSTART;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_STARTART;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_STATUS_STARTSTOPPBLOCK;

/**
 * Klasse realisiert die Nutzerschnittstelle (Telnet Interface) der StartStopp Applikation
 * @author Dambach Werke GmbH
 */
public class NutzerSchnittstelle implements Runnable 
{
  /**
   * Debug
   */
  private static final Debug logger = Debug.getLogger();
  
	/**
	 * Max. Zeit der Inaktivität, nach der der Server die Verbindung
	 * zum Client beendet.
	 */
  
	public static int SEND_TIMEOUT = 60 * 5;

  /**
   * Kommando Interpreter
   */
	private CmdInterpreter cmdInt;
  
  /**
   * Port
   */
	private int cmdPort;
  
  /**
   * Timeout
   */
  private int timeout;

  /**
   * Untermenü StartStopp Blöcke
   */
  private CmdMenu m_subMenuStartStoppBloecke = null;

  /**
   * Untermenü Prozessdaten
   */
  private CmdMenu m_subMenuProzesse = null;

  /**
   * Untermenü Prozessdaten Remoterechner
   */
  private CmdMenu m_subMenuProzesseRemote = null;

  private int m_counterUpdateProzesse = 0;

  private int m_counterUpdateProzesseRemote = 0;

	/**
	 * Erzeugt den Telnet Server. Startet ihn jedoch nicht - dazu muss {@link #start()} aufgerufen werden.
	 * @param port Port des Telnet Servers
	 */
	public NutzerSchnittstelle(int port)
  {
    //Hier erstellen der Struktur
    
    timeout = 10;
    cmdPort = port;
    
    cmdInt = new CmdInterpreter(cmdPort, timeout);
    
    //----------------------------------------------------------------------
    // Hauptmenue
    //----------------------------------------------------------------------
    
    final CmdMenu root = new CmdMenu(" Bedienung Start/Stopp","");

    //----------------------------------------------------------------------
    // Menuepunkt Übersicht StartStopp Blöcke
    //----------------------------------------------------------------------
    
    m_subMenuStartStoppBloecke = new CmdMenu("Uebersicht StartStopp Bloecke","");
    m_subMenuStartStoppBloecke.addCmd(new Command("Aktualisieren", "")
    {
      public void execute() throws Exception 
      {
        erzeugeStartStoppEintraege ();
      }
    });

    erzeugeStartStoppEintraege ();
    
    root.addNode( m_subMenuStartStoppBloecke );

    //----------------------------------------------------------------------
    // Menuepunkt Übersicht Prozesse
    //----------------------------------------------------------------------
    
    m_subMenuProzesse = new CmdMenu("Uebersicht Prozesse","");
    m_subMenuProzesse.addCmd(new Command("Aktualisieren", "")
    {
      public void execute() throws Exception 
      {
        erzeugeProzessEintraege ();
      }
    });

    erzeugeProzessEintraege ();
    
    root.addNode( m_subMenuProzesse );

    //----------------------------------------------------------------------
    // Menuepunkt Übersicht Prozesse Remote
    //----------------------------------------------------------------------
    
    m_subMenuProzesseRemote = new CmdMenu("Uebersicht Prozesse Remoterechner","");
    m_subMenuProzesseRemote.addCmd(new Command("Aktualisieren", "")
    {
      public void execute() throws Exception 
      {
        erzeugeProzessEintraegeRemote ();
      }
    });

    erzeugeProzessEintraegeRemote ();
    
    root.addNode( m_subMenuProzesseRemote );

    //----------------------------------------------------------------------
    // Menuepunkt Versionieren
    //----------------------------------------------------------------------
    
    CmdMenu subMenuVersionieren = new CmdMenu("Versionieren","");
    
    subMenuVersionieren.addCmd(new Command ("Aktuelle Einstellungen versionieren", "" )
    {
      public void execute() throws Exception
      {
        String eingabe = null;
        
        do
        {
          printlnPlain ("Sind Sie sicher ? [J(a)|N(ein)|E(nde)]:");
        
          eingabe = eingabeOhneSteuerzeichen ( readln() );
        }
        while (!eingabe.equalsIgnoreCase( "J" ) && 
               !eingabe.equalsIgnoreCase( "N" ) && 
               !eingabe.equalsIgnoreCase( "E" ));
        
        if (eingabe.equalsIgnoreCase( "J" ))
        {
          printlnPlain ("Benutzer: ");
          String benutzer = eingabeOhneSteuerzeichen ( readln() );
          
          printlnPlain ("Ursache: ");
          String ursache = eingabeOhneSteuerzeichen ( readln() );
          
          Versionierung v = new Versionierung (benutzer, ursache);

          if (v.isFehler())
          {
            printlnPlain ("Versionierung fehlgeschlagen");
            printlnPlain (v.getFehlerText());
          }
          else
          {
            printlnPlain ("Versionierung durchgeführt !");
          }
        }
      }
    });

    root.addNode( subMenuVersionieren );

    //----------------------------------------------------------------------
    // Menuepunkt Eigenschaften
    //----------------------------------------------------------------------
    
    root.addCmd(new Command ("Eigenschaften von startStopp.xml", "" )
    {
      public void execute() throws Exception
      {
        GlobaleDaten glob = GlobaleDaten.getInstanz();
        
        String id = StartStoppVerwaltung.getInstanz().getOrignalStartStoppBlockId();
        
        StartStoppEigenschaften e = glob.getStartStoppEigenschaften (id); 
        
        printlnPlain( "Eigenschaften:" );
        printlnPlain( "==============" );
        printlnPlain( "" );
        
        printlnPlain( "Version:        " + e.getVersion() );
        printlnPlain( "Erstellt am:    " + e.getErstelltAm() );
        printlnPlain( "Erstellt durch: " + e.getErstelltDurch() );
        printlnPlain( "Ursache:        " + e.getAenderungsGrund() );
      }
    });

    //----------------------------------------------------------------------
    // StartStopp beenden
    //----------------------------------------------------------------------
    
    root.addCmd(new Command ("StartStopp beenden", "" )
    {
      public void execute() throws Exception
      {
        String eingabe = null;
        
        do
        {
          printlnPlain ("Mit diesem Befehl beenden sie die StartStopp Applikation");
          printlnPlain ("und alle von ihr gestarteten Prozesse.");
          printlnPlain ("Sind Sie sicher ? [J(a)|N(ein)]:");
        
          eingabe = eingabeOhneSteuerzeichen ( readln() );
        }
        while (!eingabe.equalsIgnoreCase( "J" ) && 
               !eingabe.equalsIgnoreCase( "J_Programm" ) && // StartStopp wird durch BeendenBefehlsSender beendet
               !eingabe.equalsIgnoreCase( "N" ));
        
        if (eingabe.equalsIgnoreCase( "J" ) || eingabe.equalsIgnoreCase( "J_Programm" ))
        {
          // Meldung an Meldungsverwaltung

          String s = null;
          
          if (eingabe.equalsIgnoreCase("J_Programm")) // StartStopp wird durch BeendenBefehlsSender beendet
          {
            s = "StartStopp Applikation wird durch BefehlsSender beendet";
          }
          else // StartStopp wird durch Benutzer beendet
          {
            s = "StartStopp Applikation wird durch Benutzer beendet";
          }

          DaVKommunikation.getInstanz().sendeBetriebsmeldung( s );
              
          // Alle Prozesse + Datenverteiler beenden
          
          StartStoppApp.beendeStartStopp( "StartStopp beendet über Telnetverbindung" );

          if (eingabe.equalsIgnoreCase("J_Programm"))
          {
            while (true) // warten bis Applikation beendet wird
            {
              Thread.sleep(1000);
            }
          }
          else
          {
            printlnPlain ("StartStopp Applikation wird heruntergefahren !");

            while (true) // warten bis Applikation beendet wird
            {
              Thread.sleep(1000);
            }
          }
        }
      }        
    });

    //----------------------------------------------------------------------
    //Hauptmenuepunkt Service
    //----------------------------------------------------------------------
    
    cmdInt.setMenu(root);
    
    //----------------------------------------------------------------------
    // Thread starten für das Update des Baums
    //----------------------------------------------------------------------
    
    // Thread starten zum zyklischen Bestimmen der Daten
    
    Thread thread = new Thread(this);
    thread.setName (this.getClass().getName());
    
    thread.start();
    thread.setName( "Aktualisierung Nutzerschnittstelle (Telnet )");
	}
	
  /**
	 * Startet den Telnet Server.
	 */
	public void start()
  {
    logger.info("Telnetserver start: "+toString());
		cmdInt.start();
		cmdInt.setName( "Telnet Server" );
	}

	/**
	 * Methode mit der die anderen Threads der Benutzeroberfläche mitteilen, dass sich die Prozessdaten
	 * geändert haben. Dies löst in einem Thread aus, dass die Klassenvariabel m_subMenuProzesse neu gebildet
	 * wird. Befindet man sich nämlich in einem Submenü dieses Menüs, würden beim Betätigen von Aufwärts die
	 * Prozessdaten nicht mehr aktuell sein.
	 */
	public void aktualisiereProzessEintraege ()
	{
    m_counterUpdateProzesse++;
	}
	
  /**
   * Methode zum Erstellen der Prozesseinträge als Menü. Methode muss
   * immer dann aufgerufen werden, wenn sich an der Anzahl der 
   * Inkarnationen was ändert.
   *
   */
  private synchronized void erzeugeProzessEintraege ()
  {
//    System.out.println(this.getClass().toString() + " --->  Aktualisieren");
    
    final ProzessVerwaltung pv = ProzessVerwaltung.getInstanz();
    
    List<ProzessDaten> l1 = pv.getAlleProzessDaten();
    List<ProzessDaten> l = sortProzessDaten (l1);

    m_subMenuProzesse.clearNode();
    
    for (int p = 0; p < l.size(); ++p)
    {
      String nameInkarnation = l.get(p).getName();

      String desc = l.get(p).getName();
      
      String pid = pv.getPidByName(nameInkarnation);
      
      if (pid != null)
      {
        ProzessDaten pd = l.get( p ); // pv.getProzessDaten( pid );
        
        if (pd != null)
        {
          int ausgabeFormatierung = StartStoppApp.getAusgabeFormatierung();
          
          if (ausgabeFormatierung != 0)
          {
            String format = "%-13.13s - %-" + ausgabeFormatierung + "." + ausgabeFormatierung + "s";
            
            desc = String.format(format, pd.getZustand(), desc);
          }
          else
          {
            desc = String.format("%-13.13s - %s", pd.getZustand(), desc);
          }
          
          int bsPid = -1;
          
          BetriebssystemProzess psp = pd.getBetriebssystemProzess();
          if (psp != null)
          {
            bsPid = pd.getBetriebssystemProzess().getPid();
          }
          
          if (bsPid != -1)
          {
            if (StartStoppApp.getAusgabeFormatierung() != 0)
            {
              desc += String.format(" (Pid %5d)", bsPid);
            }
            else
            {
              desc += " (Pid " + bsPid + ")";
            }
          }
          else
          {
            desc += "            ";
          }
          
          if (StartStoppApp.isAusgabeNachstarts())
          {
            desc += String.format(" [%4d]", pd.getAnzahlNeustart());
          }
          
          if (StartStoppApp.isAusgabeLetzteStartzeit())
          {
            desc += " " + pd.getLetzterStartAsString();
          }
          
//          if (pd.getSimulationsVariante() != 0)
//            name += " (Simulationsvariante " + pd.getSimulationsVariante() + ")";
        }
      }
      
      final CmdMenu subMenu = new CmdMenu(desc, "", nameInkarnation);
      
      // Details
      
      subMenu.addCmd (new Command("Details", "")
      {
        public void execute() throws Exception 
        {
          ProzessDaten pd = bestimmeProzessDatenInkarnation (subMenu.getInkarnationsName());
          
          if (pd != null)
          {
            printlnPlain("Details:" );
            printlnPlain("========");
            printlnPlain("");
            
            printlnPlain("Inkarnationsname    : " + pd.getName());
            printlnPlain("Start/StoppID       : " + pd.getProzessId());
            printlnPlain("StartStoppBlockId   : " + pd.getStartStoppBlockId());
            printlnPlain("Simulationsvariante : " + pd.getSimulationsVariante());
            printlnPlain("Applikationsname    : " + pd.getAusfuehrbareDatei());
            printlnPlain("Aufrufparameter     : " + pd.getAufrufParameterAlsString());
            printlnPlain("Startart            : " + pd.getStartArt().getOption().getText());
            printlnPlain("Nachstarten         : " + pd.getStartArt().getNeuStart().getText());
            printlnPlain("Intervall           : " + pd.getStartArt().getIntervallZeit());
            printlnPlain("Erster Start        : " + pd.getErsterStartAsString());
            printlnPlain("Letzter Start       : " + pd.getLetzterStartAsString());
            printlnPlain("Letzter Stopp       : " + pd.getLetzterStoppAsString());
            printlnPlain("Letzte Initialis.   : " + pd.getLetzteInitialisierungAsString());
            printlnPlain("Applikationsstart   : " + pd.getApplikationsStartAsString());
            printlnPlain("Naechster Start     : " + pd.getNaechsterStartAsString());
            printlnPlain("Anzahl Neustarts    : " + pd.getAnzahlNeustart());
            printlnPlain("Zustand             : " + pd.getZustand());
            
            printlnPlain("");
          }
        }
      });

      // Anzeigen Startbedingungen
      
      subMenu.addCmd (new Command("Startbedingungen", "")
      {
        public void execute() throws Exception 
        {
          ProzessDaten pd = bestimmeProzessDatenInkarnation (subMenu.getInkarnationsName());
          
          if (pd != null)
          {
            // Auflisten der Startbedingungen
            
            List<StartBedingung> l = pd.getStartBedingung();
            
            printlnPlain("Startbedingungen:" );
            printlnPlain("=================");
            printlnPlain("");
            
            for (int i=0; i<l.size(); ++i)
            {
              StartBedingung sb = l.get( i );
              String s = sb.getRechnerAlias() + "/" + sb.getProzessName() + " (" + sb.getWarteArt().getText() + ") ";
              
              if (sb.isErfuellt())
                s = s + " --> erfuellt";
              else
              {
                s = s + " --> nicht erfuellt";
                
                if (sb.getFruehesterStartZeitpunkt() != null)
                {
                  SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                  String uhrzeit = sdf.format(sb.getFruehesterStartZeitpunkt());

                  s = s + " (" + uhrzeit + ")";
                }
              }
              
              printlnPlain("Bedingung " + (i+1) + ": " + s);
            }
            
            printlnPlain("");
          }
        }
      });

      // Anzeigen Stoppbedingungen
      
      subMenu.addCmd (new Command("Stoppbedingungen", "")
      {
        public void execute() throws Exception 
        {
          ProzessDaten pd = bestimmeProzessDatenInkarnation (subMenu.getInkarnationsName());
          
          if (pd != null)
          {
            // Auflisten der Startbedingungen
            
            List<StoppBedingung> l = pd.getStoppBedingung();
            
            printlnPlain("Stoppbedingungen:" );
            printlnPlain("=================");
            printlnPlain("");
            
            for (int i=0; i<l.size(); ++i)
            {
              StoppBedingung sb = l.get( i );
              String s = sb.getRechnerAlias() + "/" + sb.getProzessName() ;
              
              if (sb.isErfuellt())
                s = s + " --> erfuellt";
              else
              {
                s = s + " --> nicht erfuellt";
                
                if (sb.getFruehesterStoppZeitpunkt() != null)
                {
                  SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                  String uhrzeit = sdf.format(sb.getFruehesterStoppZeitpunkt());

                  s = s + " (" + uhrzeit + ")";
                }
              }

              
              printlnPlain("Bedingung " + (i+1) + ": " + s);
            }
            
            printlnPlain("");
          }
        }
      });

      // Starten
      
      subMenu.addCmd (new Command("Starten", "")
      {
        public void execute() throws Exception 
        {
          ProzessDaten pd = bestimmeProzessDatenInkarnation (subMenu.getInkarnationsName());
          
          if (pd != null)
          {
            if (pd.isInStartPhase() || pd.isGestartet() || pd.isInitialisiert())
              println ("Prozess ist bereits gestartet, Starten daher nicht moeglich !");
            else
            {
              printlnPlain("Simulationsvariante ? [" + pd.getSimulationsVariante() + "|e]");
              
              String eingabe = eingabeOhneSteuerzeichen ( readln() );
              
              if (eingabe.equalsIgnoreCase( "e"))
                return;

              long simulationsVariante = 0l;
              
              try
              {
                simulationsVariante = Long.parseLong( eingabe );
              }
              catch ( Exception e )
              {
                
              }
              
              pd.getInkarnation().neuStartInkarnation( -1, simulationsVariante );
            }
          }
        }
      });

      // Stoppen
      
      subMenu.addCmd (new Command("Stoppen", "")
      {
        public void execute() throws Exception 
        {
          ProzessDaten pd = bestimmeProzessDatenInkarnation (subMenu.getInkarnationsName());
          
          if (pd != null)
          {
            if (!pd.isGestartet() && !pd.isInitialisiert())
              println ("Prozess ist nicht gestartet, Stoppen daher nicht moeglich !");
            else
            {
              printlnPlain("Soll der Prozess gestoppt werden? [j|n]");
              
              if (eingabeOhneSteuerzeichen ( readln() ).equalsIgnoreCase( "j"))
              {
                pd.setFehlerText( "" );
                pd.getInkarnation().stoppeInkarnation( -1, false );
                
                // Warten bis Stoppbedingungen erfüllt sind

                boolean ende = false;
                int counter = 0;
                int punkteProZeile = 60;
                
                if (!pd.getInkarnation().isStoppBedingungErfuellt())
                {
                  printlnPlain("Warten auf Erfüllung der Stoppbedingungen (Warten abbrechen mit Return)\n\r");
  
                  while (pd.isInWarteStoppbedingungPhase())
                  {
                    printPlain (".");
                    counter++;
                    if (counter == punkteProZeile)
                    {
                      printPlain( "\n\r" );
                      counter = 0;
                    }

                    Thread.sleep( 1000 );
                    
                    if (read () > 0)  // Taste betätigt
                    {
                      ende = true;
                      break;
                    }
                  }
                }

                // Warten auf Ende der Inkarnation
                
                if (!ende && pd.isAktiv())
                {
                  printlnPlain("Warten auf Ende des Prozesses (Warten abbrechen mit Return)\n\r");

                  counter = 0;
                  while (pd.isAktiv())
                  {
                    printPlain (".");
                    counter++;
                    if (counter == punkteProZeile)
                    {
                      printPlain( "\n\r" );
                      counter = 0;
                    }
                    
                    Thread.sleep( 1000 );
                    
                    if (read () > 0)  // Taste betätigt
                      break;
                    
                    if (!pd.getFehlerText().equals( "" ))
                    {
                      printlnPlain( pd.getFehlerText());
                      
                      break;
                    }
                  }
                  
                } // if (!ende)
                
                if (pd.isGestoppt())
                  printlnPlain("Prozess gestoppt !");
                
                erzeugeProzessEintraege();
              }
            }
          }
        }
      });

      // Neustart
      
      subMenu.addCmd (new Command("Neustart", "")
      {
        public void execute() throws Exception 
        {
          ProzessDaten pd = bestimmeProzessDatenInkarnation (subMenu.getInkarnationsName());
          
          if (pd != null)
          {
            printlnPlain("Soll der Prozess neugestartet werden? [j|n]");
            
            if (eingabeOhneSteuerzeichen ( readln() ).equalsIgnoreCase( "j"))
            {
              printlnPlain("Simulationsvariante ? [" + pd.getSimulationsVariante() + "|e]");
              
              String eingabe = eingabeOhneSteuerzeichen ( readln() );
              
              if (eingabe.equalsIgnoreCase( "e"))
                return;

              long simulationsVariante = 0l;
              
              try
              {
                simulationsVariante = Long.parseLong( eingabe );
              }
              catch ( Exception e )
              {
                
              }
              
              pd.getInkarnation().neuStartInkarnation( -1, simulationsVariante );
            }
          }
        }
      });

      // Ändern
      
      subMenu.addCmd (new Command("Aendern", "")
      {
        public void execute() throws Exception 
        {
          ProzessDaten pd = bestimmeProzessDatenInkarnation (subMenu.getInkarnationsName());
          ProzessVerwaltung pv = ProzessVerwaltung.getInstanz();
          
          if (pd != null)
          {
            String eingabe = null;

            //------------------------------------------------------------------------------------------
            // Startart
            //------------------------------------------------------------------------------------------
            
            E_STARTART oldStartart = pd.getStartArt().getOption();
            
            printlnPlain("Startart (" + oldStartart.getText() + ")");
            
            do
            {
              printlnPlain("Bitte den neuen Wert eingeben [A(utomatisch)|M(anuell)|I(ntervall)|E(nde)]:");
            
              eingabe = eingabeOhneSteuerzeichen ( readln() );
            }
            while (!eingabe.equalsIgnoreCase( "A" ) && 
                   !eingabe.equalsIgnoreCase( "M" ) && 
                   !eingabe.equalsIgnoreCase( "I" ) &&
                   !eingabe.equalsIgnoreCase( "E" ) &&
                   !eingabe.equalsIgnoreCase( "" ));
            
            if (eingabe.equalsIgnoreCase( "E" ))  // Ende
              return;
            
            if (eingabe.equalsIgnoreCase( "A" ))
              pd.getStartArt().setOption(E_STARTART.AUTOMATISCH);

            if (eingabe.equalsIgnoreCase( "M" ))
              pd.getStartArt().setOption(E_STARTART.MANUELL);
            
            if (eingabe.equalsIgnoreCase( "I" ))
              pd.getStartArt().setOption(E_STARTART.INTERVALL);

            printlnPlain("Startart: " + pd.getStartArt().getOption().getText());

            // Prüfen ob Änderung in der Startart
            
            if (oldStartart != pd.getStartArt().getOption())
            {
              if (pd.getStartArt().getOption() == E_STARTART.AUTOMATISCH) 
              {
                if (pd.isGestoppt())
                  pv.starteProzess( pd.getProzessId(), -1, pd.getSimulationsVariante() );
              }
              
              if (pd.getStartArt().getOption() == E_STARTART.MANUELL) 
              {
                if (pd.isGestartet())
                  pv.stoppeProzess( pd.getProzessId(), -1, true);
              }

              if (pd.getStartArt().getOption() == E_STARTART.INTERVALL) 
              {
                if (pd.isGestartet())
                  pv.stoppeProzess( pd.getProzessId(), -1, true);
                
                pv.starteProzessZyklisch( pd.getProzessId(), -1, pd.getSimulationsVariante() );
              }
            }

            //------------------------------------------------------------------------------------------
            // Nachstarten
            //------------------------------------------------------------------------------------------
            
            E_NEUSTART oldNachstarten = pd.getStartArt().getNeuStart();
            
            printlnPlain("Nachstarten (" + oldNachstarten.getText() + ")");
            
            do
            {
              printlnPlain("Bitte den neuen Wert eingeben [J(a)|N(ein)|E(nde)]:");
            
              eingabe = eingabeOhneSteuerzeichen ( readln() );
            }
            while (!eingabe.equalsIgnoreCase( "J" ) && 
                   !eingabe.equalsIgnoreCase( "N" ) && 
                   !eingabe.equalsIgnoreCase( "E" ) &&
                   !eingabe.equalsIgnoreCase( "" ));
            
            if (eingabe.equalsIgnoreCase( "E" ))  // Ende
              return;
            
            if (eingabe.equalsIgnoreCase( "J" ))
              pd.getStartArt().setNeuStart( E_NEUSTART.Ja );

            if (eingabe.equalsIgnoreCase( "N" ))
              pd.getStartArt().setNeuStart( E_NEUSTART.Nein );
            
            printlnPlain("Nachstarten: " + pd.getStartArt().getNeuStart().getText());
            
            // Prüfen ob Änderung im Nachstartverhalten
            
            if (oldNachstarten != pd.getStartArt().getNeuStart())
            {
              if (pd.getStartArt().getNeuStart() == E_NEUSTART.Ja)
              {
                if (pd.isGestoppt())
                  pv.starteProzess( pd.getProzessId(), -1, pd.getSimulationsVariante() );
              }
            }
            
            //------------------------------------------------------------------------------------------
            // Intervallzeit
            //------------------------------------------------------------------------------------------
            
            TimeIntervalCron t;
            
            do
            {
              printlnPlain("Intervall (" + pd.getStartArt().getIntervallZeit() + ")");
              
              printlnPlain("Bitte den neuen Wert eingeben [E(nde)]:");
            
              eingabe = eingabeOhneSteuerzeichen ( readln() );
  
              if (eingabe.equalsIgnoreCase( "E" ))  // Ende
                return;
              
              if (eingabe.equalsIgnoreCase( "" ))
              {
                eingabe = pd.getStartArt().getIntervallZeit();
              }
            
              t = new TimeIntervalCron ();
              
              t.setFields( eingabe );
            }
            while (t.hasParseErrors());
            
            pd.getStartArt().setIntervallZeit( eingabe );            
            printlnPlain("Intervall: " + pd.getStartArt().getIntervallZeit());
            
          }
        }
       });

      // Löschen
      
      subMenu.addCmd (new Command("Loeschen", "")
      {
        public void execute() throws Exception 
        {
          ProzessDaten pd = bestimmeProzessDatenInkarnation (subMenu.getInkarnationsName());
          
          if (pd != null)
          {
            printlnPlain("Soll der Prozess geloescht werden? [j|n]");
            
            if (eingabeOhneSteuerzeichen ( readln() ).equalsIgnoreCase( "j"))
            {
              ProzessVerwaltung.getInstanz().loescheProzess( pd.getProzessId(), -1 );
            }
          }
        }
      });

      // Fehlermeldung
      
      ProzessDaten pd = bestimmeProzessDatenInkarnation (subMenu.getInkarnationsName());

      if (pd != null)
      {
        if (!pd.getFehlerText().equals( "" ))
        {
          subMenu.addCmd (new Command("Fehlermeldung", "")
          {
            public void execute() throws Exception 
            {
              ProzessDaten pd = bestimmeProzessDatenInkarnation (subMenu.getInkarnationsName());
              
              if (pd != null)
              {
                printlnPlain("Fehlermeldung:");
                printlnPlain(pd.getFehlerText());
              }
            }
          });
        }
      }

      m_subMenuProzesse.addNode(subMenu);
    }
  }

  /**
   * Methode zum alphabetischen Sortieren der einzelnen Prozesse
   * @param alleProzessDaten
   * @return Liste mit den IDs der sortierten Prozesse
   */
  private List<ProzessDaten> sortProzessDaten( List<ProzessDaten> alleProzessDaten )
  {
    List<ProzessDaten> sortierteDaten = new ArrayList<ProzessDaten>();
    
    TreeMap<String, ProzessDaten> sort = new TreeMap<String, ProzessDaten> ();
    
    // Sortieren
    
    for (int i=0; i<alleProzessDaten.size(); ++i)
    {
      ProzessDaten pd = alleProzessDaten.get( i );
    
      String key = pd.getName();
      
      // Name schon vorhanden
      
      if (sort.containsKey ( key ))
      {
        String keyNeu = null;
        
        int counter = 1;
        do
        {
          keyNeu = key + " (" + counter + ")";
        }
        while (sort.containsKey( keyNeu ));
        
        key = keyNeu;
      }
      
      sort.put( key, pd );
    }
    
    // Wieder umkopieren
    
    Iterator it = sort.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry me = (Map.Entry)it.next();
      
      sortierteDaten.add( (ProzessDaten ) me.getValue());
    }
    
    return sortierteDaten;
  }

  /**
   * Methode mit der die anderen Threads der Benutzeroberfläche mitteilen, dass sich die Prozessdaten
   * geändert haben. Dies löst in einem Thread aus, dass die Klassenvariabel m_subMenuProzesse neu gebildet
   * wird. Befindet man sich nämlich in einem Submenü dieses Menüs, würden beim Betätigen von Aufwärts die
   * Prozessdaten nicht mehr aktuell sein.
   */
  public void aktualisiereProzessEintraegeRemote ()
  {
    m_counterUpdateProzesseRemote++;
  }

  /**
   * Methode zum Erstellen der Prozesseinträge der Remoterechner als Menü. 
   * Methode muss immer dann aufgerufen werden, wenn sich an der Anzahl der 
   * Inkarnationen was ändert.
   *
   */
  private synchronized void erzeugeProzessEintraegeRemote ()
  {
    m_subMenuProzesseRemote.clearNode();
    
    final ProzessVerwaltung pv = ProzessVerwaltung.getInstanz();
    
    List<ProzessDaten> l = pv.getAlleProzessDatenRemote();
      
    for (int p = 0; p<l.size(); ++p)
    {
      String name = l.get( p ).getName();
      String ip   = l.get( p ).getIpAdresse();
      
      String pid = pv.getPidByNameRemote( name, ip );
      
      if (pid != null)
      {
        ProzessDaten pd = pv.getProzessDatenRemote( pid );
        
        if (pd != null)
          name = String.format( "%-13.13s - %-15s %s", pd.getZustand(), name, pd.getIpAdresse());
      }
      
      final CmdMenu subMenu = new CmdMenu(name, "");
      
      // Details
      
      subMenu.addCmd (new Command("Details", "")
      {
        public void execute() throws Exception 
        {
          ProzessDaten pd = bestimmeProzessDatenRemote (subMenu.getDesc());
          
          if (pd != null)
          {
            printlnPlain("Details:" );
            printlnPlain("========");
            printlnPlain("");
            
            printlnPlain("Inkarnationsname    : " + pd.getName());
            printlnPlain("Start/StoppID       : " + pd.getProzessId());
            printlnPlain("Zustand             : " + pd.getZustand());
            printlnPlain("Letzter Start       : " + pd.getLetzterStartAsString());
            printlnPlain("Letzter Stopp       : " + pd.getLetzterStoppAsString());
            printlnPlain("Letzte Initialis.   : " + pd.getLetzteInitialisierungAsString());
            printlnPlain("Naechster Start     : " + pd.getNaechsterStartAsString());
            
            printlnPlain("");
          }
        }
      });

      m_subMenuProzesseRemote.addNode(subMenu);
    }
  }

  /**
   * Methode zum Bestimmen der Prozessdaten eines Prozesses. Für den Prozess
   * wird ein String übergeben, der den Namen einer gültigen Inkarnation 
   * enthalten muss
   * @param inkarnationsName Text der den Namen einer gültigen Inkarnation enthalten muss
   * @return null Inkarnation nicht gefunden sonst Prozessdaten der Inkarnation
   */
  private ProzessDaten bestimmeProzessDatenInkarnation (String inkarnationsName)
  {
    ProzessVerwaltung pv = ProzessVerwaltung.getInstanz();

    List<ProzessDaten> l = pv.getAlleProzessDaten();
    
//    inkarnationsName = inkarnationsName + " ";
    
    for (int i=0; i<l.size(); ++i)
    {
      ProzessDaten pd = l.get( i );
      
      String name = pd.getName();
      
      // Wichtig Blank nach Namen da sonst Doppeldeutigkeit möglich
      
//      if (inkarnationsName.contains( " " + name + " " ))
      if (inkarnationsName.equals(name))
        return pd;
//      {
//        String pid = pv.getPidByName( name );
//        
//        if (pid != null)
//        {
//          ProzessDaten pd = pv.getProzessDaten( pid );
//          
//          return pd;
//        }
//      }
    }
    
    return null;
  }

  /**
   * Methode zum Bestimmen der Prozessdaten eines Remoteprozesses. Für den Prozess
   * wird ein String übergeben, der den Namen einer gültigen Inkarnation 
   * enthalten muss
   * @param text Text der den Namen einer gültigen Inkarnation enthalten muss
   * @return null Inkarnation nicht gefunden sonst Prozessdaten der Inkarnation
   */
  private ProzessDaten bestimmeProzessDatenRemote (String text)
  {
    ProzessVerwaltung pv = ProzessVerwaltung.getInstanz();

    List<ProzessDaten> l = pv.getAlleProzessDatenRemote();
    
    text = text + " ";
    
    for (int i=0; i<l.size(); ++i)
    {
      String name = l.get(i).getName();
      String ip   = l.get(i).getIpAdresse();
      
      // Wichtig Blank nach Namen da sonst Doppeldeutigkeit möglich
      
      if (text.contains( " " + name + " " ))
      {
        String pid = pv.getPidByNameRemote ( name, ip );
        
        if (pid != null)
        {
          ProzessDaten pd = pv.getProzessDatenRemote( pid );
          
          return pd;
        }
      }
    }
    
    return null;
  }

  /**
   * Methode entfernt die Steuerzeichen aus dem Eingabestring
   * @param eingabe Eingabe mit Steuerzeichen
   * @return Eingabe ohne Steuerzeichen
   */
  private String eingabeOhneSteuerzeichen (String eingabe)
  {
    String neu = "";
    
    for (int i=0; i<eingabe.length(); ++i)
    {
      if (eingabe.charAt( i ) >= ' ')
        neu += eingabe.charAt( i );
    }
    
    return neu;
  }
  
  /**
   * Methode zum Erstellen der StartStopp Block Einträge als Menü. 
   * Methode muss immer dann aufgerufen werden, wenn sich an der Anzahl der 
   * StartStopp Blöcke was ändert.
   *
   */
  public synchronized void erzeugeStartStoppEintraege ()
  {
    m_subMenuStartStoppBloecke.clearNode();
    
    final StartStoppVerwaltung ssv = StartStoppVerwaltung.getInstanz();
    
    List<StartStoppBlock> l = ssv.getAllStartStoppBloecke();
    
    l = sortStartStoppBloecke( l );
      
    for (int p = 0; p<l.size(); ++p)
    {
      StartStoppBlock ssb = l.get( p );
      
      String name = String.format( "%-10.10s - %s",
                    ssb.getStatus().getText(),
                    ssb.getStartStoppBlockId());
    
      final CmdMenu subMenu = new CmdMenu (name, "");
      
      // Details
      
      subMenu.addCmd (new Command("Details", "")
      {
        public void execute() throws Exception 
        {
          StartStoppBlock ssb = bestimmeStartStoppBlock (subMenu.getDesc());
          
          if (ssb != null)
          {
            printlnPlain("Details:" );
            printlnPlain("========");
            printlnPlain("");
            
            printlnPlain("ID                    : " + ssb.getStartStoppBlockId());
            printlnPlain("Zustand               : " + ssb.getStatus().getText());
            printlnPlain("Zeitpunkt             : " + ssb.getStatusZeitpunktAlsString());
            
            printlnPlain("");
          }
        }
      });

      // Starten

      subMenu.addCmd (new Command("Starten", "")
      {
        public void execute() throws Exception 
        {
          StartStoppBlock ssb = bestimmeStartStoppBlock (subMenu.getDesc());
          
          if (ssb != null)
          {
            if (ssb.getStatus() == E_STATUS_STARTSTOPPBLOCK.GESTARTET)
              println ("StartStopp Block ist bereits gestartet !");
            else
            {
              printlnPlain("Simulationsvariante ? [" + ssb.getSimulationsVariante() + "|e]");
              
              String eingabe = eingabeOhneSteuerzeichen ( readln() );
              
              if (eingabe.equalsIgnoreCase( "e"))
                return;

              long simulationsVariante = 0l;
              
              try
              {
                simulationsVariante = Long.parseLong( eingabe );
              }
              catch ( Exception e )
              {
                
              }
              
              ssb.neuStarteProzesse ( -1, simulationsVariante );
            }
          }
        }
      });

      // Stoppen
      
      subMenu.addCmd (new Command("Stoppen", "")
      {
        public void execute() throws Exception 
        {
          StartStoppBlock ssb = bestimmeStartStoppBlock (subMenu.getDesc());
          
          if (ssb != null)
          {
            if (ssb.getStatus() == E_STATUS_STARTSTOPPBLOCK.GESTOPPT)
              println ("StartStopp Block ist bereits gestoppt !");
            else
            {
              printlnPlain("Sollen die Prozesse des StartStopp Blocks gestoppt werden? [j|n]");
              
              if (eingabeOhneSteuerzeichen ( readln() ).equalsIgnoreCase( "j"))
              {
                ssb.stoppeProzesse ( -1 );
              }
            }
          }
        }
      });

      // Neustart
      
      subMenu.addCmd (new Command("Neustart", "")
      {
        public void execute() throws Exception 
        {
          StartStoppBlock ssb = bestimmeStartStoppBlock (subMenu.getDesc());
          
          if (ssb != null)
          {
            printlnPlain("Sollen die Prozesse des StartStopp Blocks neu gestartet werden? [j|n]");
            
            if (eingabeOhneSteuerzeichen ( readln() ).equalsIgnoreCase( "j"))
            {
              printlnPlain("Simulationsvariante ? [" + ssb.getSimulationsVariante() + "|e]");
              
              String eingabe = eingabeOhneSteuerzeichen ( readln() );
              
              if (eingabe.equalsIgnoreCase( "e"))
                return;

              long simulationsVariante = 0l;
              
              try
              {
                simulationsVariante = Long.parseLong( eingabe );
              }
              catch ( Exception e )
              {
                
              }
              
              ssb.neuStarteProzesse ( -1, simulationsVariante );
            }
          }
        }
      });

      // Löschen
      
      subMenu.addCmd (new Command("Loeschen", "")
      {
        public void execute() throws Exception 
        {
          StartStoppBlock ssb = bestimmeStartStoppBlock (subMenu.getDesc());
          
          if (ssb != null)
          {
            printlnPlain("Soll der StartStopp Block und seine Prozesse geloescht werden? [j|n]");
              
            if (eingabeOhneSteuerzeichen ( readln() ).equalsIgnoreCase( "j"))
            {
              StartStoppVerwaltung.getInstanz().loescheStartStoppBlock( ssb.getStartStoppBlockId(), -1);
            }
          }
        }
      });

      m_subMenuStartStoppBloecke.addNode(subMenu);
    }
  }

  /**
   * Methode zum alphabetischen Sortieren der StartStopp Blöcke
   * @param alleStartStoppBloecke StartStopp Blöcke unsortiert
   * @return StartStopp Blöcke sortiert
   */
  private List<StartStoppBlock> sortStartStoppBloecke( List<StartStoppBlock> alleStartStoppBloecke )
  {
    List<StartStoppBlock> sortierteDaten = new ArrayList<StartStoppBlock>();
    
    TreeMap<String, StartStoppBlock> sort = new TreeMap<String, StartStoppBlock> ();
    
    // Sortieren
    
    for (int i=0; i<alleStartStoppBloecke.size(); ++i)
    {
      StartStoppBlock ssb = alleStartStoppBloecke.get( i );
    
      String key = ssb.getStartStoppBlockId();
      
      // Name schon vorhanden
      
      if (sort.containsKey ( key ))
      {
        String keyNeu = null;
        
        int counter = 1;
        do
        {
          keyNeu = key + " (" + counter + ")";
        }
        while (sort.containsKey( keyNeu ));
        
        key = keyNeu;
      }
      
      sort.put( key, ssb );
    }
    
    // Wieder umkopieren
    
    Iterator it = sort.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry me = (Map.Entry)it.next();
      
      sortierteDaten.add( (StartStoppBlock ) me.getValue());
    }
    
    return sortierteDaten;
  }

  /**
   * Methode zum Bestimmen der StartStoppBlock Daten. Für den StartStopp Block
   * wird ein String übergeben, der den Namen eines gültigen StartStopp Blocks 
   * enthalten muss
   * @param text Text der den Namen eines gültigen StartStoppBlocks enthalten muss
   * @return null StartStopp Block nicht gefunden sonst StartStopp Block
   */
  private StartStoppBlock bestimmeStartStoppBlock (String text)
  {
    StartStoppVerwaltung ssv = StartStoppVerwaltung.getInstanz();
    
    List<StartStoppBlock> l = ssv.getAllStartStoppBloecke();
    
    text = text + " ";
    
    for (int i=0; i<l.size(); ++i)
    {
      String id = l.get(i).getStartStoppBlockId();
      
      if (text.contains( " " + id + " " ))
        return ssv.getStartStoppBlock( id );
    }
    
    return null;
  }

  /**
   * Thread prüft alle 10 Sekunden ob sich die Prozesseinträge geändert haben, wenn ja
   * werden die Prozesseinträge aktualisiert.
   */
  public void run()
  {
    while (true)
    {
      try
      {
        Thread.sleep(  10000 );
      }
      catch (InterruptedException e)
      {
      }
      
      if (m_counterUpdateProzesse != 0)
      {
        int counter = m_counterUpdateProzesse;
        erzeugeProzessEintraege();
        m_counterUpdateProzesse -= counter;
      }
      
      if (m_counterUpdateProzesseRemote != 0)
      {
        int counter = m_counterUpdateProzesseRemote;
        erzeugeProzessEintraegeRemote();
        m_counterUpdateProzesseRemote -= counter;
      }
    }
  }
}