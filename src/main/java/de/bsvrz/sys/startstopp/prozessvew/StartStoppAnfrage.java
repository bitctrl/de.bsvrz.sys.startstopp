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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jdom.Element;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.Data.Array;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVerwaltung;
import de.bsvrz.sys.startstopp.skriptvew.StartStoppApp;
import de.bsvrz.sys.startstopp.skriptvew.StartStoppBlock;
import de.bsvrz.sys.startstopp.skriptvew.StartStoppVerwaltung;
import de.bsvrz.sys.startstopp.skriptvew.Versionierung;
import de.bsvrz.sys.startstopp.skriptvew.XMLTools;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_AUSGABE;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_FEHLER_STARTSTOPPBLOCK;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_START_FEHLER_VERHALTEN;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_STOPP_FEHLER_VERHALTEN;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_ZUSTAND;

/**
 * Klasse zum Auswerten einer StartStopp Anfrage, die über die Attributgruppe
 * att.startStoppAnfrage empfangen wurde. Die Auswertung erfolgt in einem Thread.
 * Der Thread wird automatisch durch den Konstruktor gestartet.
 * @author Dambach Werke GmbH
 *
 */
public class StartStoppAnfrage extends Thread
{
  /**
   * Debug
   */
  private static final Debug logger = Debug.getLogger();
  
  /**
   * Debug Einstellungen
   */
  
  private boolean _debug = false;

  /**
   * Definierter Text zum Starten einer Applikation / eines StartStopp Blocks
   */
  private static final String _AKTION_START    = "starten"; 

  /**
   * Definierter Text zum Stoppen einer Applikation / eines StartStopp Blocks
   */
  private static final String _AKTION_STOPP    = "stoppen";
  
  /**
   * Definierter Text zum Neustarten einer Applikation / eines StartStopp Blocks
   */
  private static final String _AKTION_NEUSTART = "neustart";

  /**
   * Definierter Text zum Löschen einer Applikation / eines StartStopp Blocks
   */
  private static final String _AKTION_LOESCHEN = "löschen";

  /**
   * Verweis auf Singelton Instanz der Klasse ProzessVerwaltung  
   */
  private ProzessVerwaltung m_prozessVerwaltung = null;
  
  /**
   * Verweis auf Singelton Instanz der Klasse Logbuch  
   */
  private Logbuch m_logbuch = null;
  
  /**
   * Verweis auf Singelton Instanz der Klasse DaVKommunikation  
   */
  private DaVKommunikation m_daVKommunikation = null;
  
  /**
   * Auszuwertende Daten vom Typ "atg.startStoppAnfrage"
   */
  private ResultData m_data = null;
  
  /**
   * Merker ob der Thread zum Auswerten der Attributgruppe bereits gestartet
   * wurde
   */
  private boolean m_threadGestartet = false;

  /**
   * Id des Absenderprozesses  
   */
  private long m_absenderId = -1;
  
  /**
   * Absender Zeichen, Kennung innerhalb des Absenderprozesses, mit dem der Absender
   * die Antwort zu der Anfrage zuordnen kann
   */
  private String m_absenderZeichen = null;
  
  /**
   * Konstruktor der Klasse. Der Thread  der die Auswertung der Attributgruppe vornimmt,
   * wird durch den Konstruktor automatisch gestartet
   * @param data ResultData vom Typ "atg.startStoppAnfrage"
   */
  public StartStoppAnfrage (ResultData data)
  {
    m_data = data;  
    
    start();
  }
    
  /**
   * Run Methode des Threads
   */
  public void run ()
  {
    String pid = m_data.getDataDescription().getAttributeGroup().getPid();
    
    if (!pid.equals("atg.startStoppAnfrage"))
    {
      System.err.println("StartStoppAnfrage: Falsche Attributgruppe: " + pid);
      return;
    }

    if (m_threadGestartet)
    {
      System.err.println("StartStoppAnfrage: Thread läuft bereits");
      return;
    }
    
    m_threadGestartet = true;
    
    // Singelton instanzieren
    
    m_prozessVerwaltung = ProzessVerwaltung.getInstanz();
    
    m_logbuch = Logbuch.getInstanz();
    
    m_daVKommunikation = DaVKommunikation.getInstanz();

    // Datensatz auswerten
    
    m_absenderId = Long.parseLong( m_data.getData().getTextValue( "absenderId" ).getValueText());


    // Absender Zeichen
    
    m_absenderZeichen = m_data.getData().getTextValue( "absenderZeichen" ).getValueText();

    if (m_data.getData().getArray( "V1" ).getLength() == 1)
      updateVariante1 (m_data.getData().getArray( "V1" ).getItem( 0 ));

    if (m_data.getData().getArray( "V2" ).getLength() == 1)
      updateVariante2 (m_data.getData().getArray( "V2" ).getItem( 0 ));

    if (m_data.getData().getArray( "V3" ).getLength() == 1)
      updateVariante3 (m_data.getData().getArray( "V3" ).getItem( 0 ));

    if (m_data.getData().getArray( "V4" ).getLength() == 1)
      updateVariante4 (m_data.getData().getArray( "V4" ).getItem( 0 ));

    m_threadGestartet = false;
  }
  
  /**
   * Auswerten der Attributgruppe "atg.startStoppAnfrage" Variante 1
   * @param data Datenteil Variante 1 der Attributgruppe "atg.startStoppAnfrage" 
   */
  private void updateVariante1( Data data )
  {
    String aktion    = data.getTextValue  ( "Aktion" ).getValueText();
    String prozessId = data.getTextValue  ( "ProzessID" ).getValueText();
    String name1     = data.getTextValue  ( "Name"  ).getValueText();

    String buffer = "Schalte über DaV: Variante 1 - Aktion = " + aktion.toString() + " PrID = " + prozessId + " Name = " + name1;                      ;
    m_logbuch.schreibe( buffer );
    
    if (_debug)
    {
      System.out.println("-------------------------------------------");
      System.out.println( buffer );
    }
    
    if (!pruefeAktionV1PlusV4( m_absenderId, prozessId, name1, aktion ))
      return;

    // Neue Prozessdaten anlegen
    
    ProzessDaten pd = new ProzessDaten ();
    
    // Bei Aktion "Starten" (neurt Prozess) prüfen ob es bereits einen Prozess mit diesem Namen gibt
  
    String name = null;
    
    if (aktion.equalsIgnoreCase( _AKTION_START ))
      name = m_prozessVerwaltung.bildeEindeutigenNamen (name1);
    else
      name = name1;

    pd.setName ( name );
    
    // Ausführbare Datei
    
    pd.setAusfuehrbareDatei( data.getTextValue( "AusfuehrbareDatei" ).getValueText() );

    // Aufrufparameter
    
    Array p = data.getArray ("AufrufParameter" );
    
    pd.clearAufrufParameter();
    for (int a = 0; a < p.getLength(); a++) 
      pd.addAufrufParameter( p.getItem( a ).getTextValue( "AufrufparameterWert" ).getValueText() );
 
    // StartArt
    
    Data d = data.getItem( "StartArt" );
    
    String option        = d.getScaledValue( "OptionStart" ).getValueText();
    String neustart      = d.getScaledValue( "NeuStart" ).getValueText();
    String intervall     = d.getTextValue  ( "Intervall" ).getValueText();
    
    pd.setStartArt( option, neustart, intervall );

    // Startbedingung
    
    p = data.getArray ("StartBedingung");
    
    pd.clearStartBedingung();
    for (int a = 0; a < p.getLength(); a++) 
    {    
      String vorgaenger = p.getItem( a ).getTextValue( "Vorgaenger" ).getValueText();
      String warteart   = p.getItem( a ).getTextValue( "WarteArt" ).getValueText();
      String rechner    = p.getItem( a ).getTextValue( "Rechner" ).getValueText(); 

      long warteZeit = p.getItem( a ).getTimeValue( "WarteZeit" ).getMillis();
      
      pd.addStartBedingung( vorgaenger, warteart, rechner, warteZeit );
    }
    
    // Stoppbedingung
    
    p = data.getArray ("StoppBedingung");
    
    pd.clearStoppBedingung();
    for (int a = 0; a < p.getLength(); a++) 
    {    
      String nachfolger = p.getItem( a ).getTextValue( "Nachfolger" ).getValueText();

      String rechner = p.getItem( a ).getTextValue( "Rechner" ).getValueText(); 

      long warteZeit = p.getItem( a ).getTimeValue( "WarteZeit" ).getMillis(); 

      pd.addStoppBedingung( nachfolger, rechner, warteZeit );
    }
    
    // Standardausgabe

    d = data.getItem( "StandardAusgabe" );

    E_AUSGABE optionSa = E_AUSGABE.IGNORIEREN;
    
    if (d.getScaledValue( "OptionStandardAusgabe" ).getValueText().equalsIgnoreCase( E_AUSGABE.EIGENEDATEI.toString() ) )
      optionSa = E_AUSGABE.EIGENEDATEI;

    if (d.getScaledValue( "OptionStandardAusgabe" ).getValueText().equalsIgnoreCase( E_AUSGABE.GEMEINSAMEDATEI.toString() ) )
      optionSa = E_AUSGABE.GEMEINSAMEDATEI;

    String aliasSa = d.getTextValue( "Datei" ).getValueText();
       
    // In Variante 1 wird kein globaler Teil übergeben. Die Dateinamen werden daher dem 1. StartStoppBlock entnommen
    
    String id = StartStoppVerwaltung.getInstanz().getOrignalStartStoppBlockId();
    
    pd.setStandardAusgabe( id, optionSa, aliasSa);
    
    // Standardfehlerausgabe

    d = data.getItem( "FehlerAusgabe" );

    E_AUSGABE optionSFA = E_AUSGABE.IGNORIEREN;
    
    if (d.getScaledValue( "OptionFehlerAusgabe" ).getValueText().equalsIgnoreCase( E_AUSGABE.EIGENEDATEI.toString() ) )
      optionSFA = E_AUSGABE.EIGENEDATEI;

    if(d.getScaledValue( "OptionFehlerAusgabe" ).getValueText().equalsIgnoreCase( E_AUSGABE.GEMEINSAMEDATEI.toString() ) )
      optionSFA = E_AUSGABE.GEMEINSAMEDATEI;
    
    String aliasSfa = d.getTextValue(  "Datei" ).getValueText();

    pd.setStandardFehlerAusgabe( id, optionSFA, aliasSfa );
   
    // StartVerhaltenFehler

    d = data.getItem ("StartVerhaltenFehler");
    
    E_START_FEHLER_VERHALTEN startVer= E_START_FEHLER_VERHALTEN.IGNORIEREN;
    if( d.getScaledValue( "StartVerhaltenFehlerOption" ).getValueText().equalsIgnoreCase( E_START_FEHLER_VERHALTEN.ABBRUCH.toString() ) )
    {
      startVer= E_START_FEHLER_VERHALTEN.ABBRUCH;
    }
    if( d.getScaledValue( "StartVerhaltenFehlerOption" ).getValueText().equalsIgnoreCase( E_START_FEHLER_VERHALTEN.BEENDEN.toString() ) )
    {
      startVer= E_START_FEHLER_VERHALTEN.BEENDEN;
    }

    long wiederholung = ( d.getScaledValue( "Wiederholrate" ).longValue() );

    pd.setStartVerhaltenFehler( startVer, wiederholung );
 
    
    //setzen von StoppVerhaltenFehler

    d = data.getItem ("StoppVerhaltenFehler");

    E_STOPP_FEHLER_VERHALTEN stoppVer= E_STOPP_FEHLER_VERHALTEN.STOPP;
    if( d.getScaledValue( "StoppVerhaltenFehlerOption" ).getValueText().equalsIgnoreCase( E_STOPP_FEHLER_VERHALTEN.ABBRUCH.toString() ) )
    {
      stoppVer= E_STOPP_FEHLER_VERHALTEN.ABBRUCH;
    }
    if( d.getScaledValue( "StoppVerhaltenFehlerOption" ).getValueText().equalsIgnoreCase( E_START_FEHLER_VERHALTEN.IGNORIEREN.toString() ) )
    {
      stoppVer= E_STOPP_FEHLER_VERHALTEN.IGNORIEREN;
    }

    long wiederholungen = (d.getScaledValue( "Wiederholrate" ).longValue() );

    pd.setStoppVerhaltenFehler( stoppVer, wiederholungen );

    // Prüfen ob die Daten dieses Prozesses plausibel sind
    
    if (!pd.isPlausibel())
    {
      sendeStartStoppAntwort (E_FEHLER_STARTSTOPPBLOCK.FEHLER_XML, "Daten nicht plausibel: ");
      return;
    }

    // Starten eines zusätzlichen Start/Stop Blocks
    
    E_ZUSTAND z = null;
    
    if (aktion.equalsIgnoreCase( _AKTION_START ))
    {
      StartStoppVerwaltung ssv = StartStoppVerwaltung.getInstanz();

      // StartStopp Block anlegen

      StartStoppBlock ssb = ssv.addStartStoppBlock (null, null);

      // Neue Pid für Prozess bestimmen
      
      String key = m_prozessVerwaltung.bestimmeProzessPid( ssb.getStartStoppBlockId(), pd.getName());

      m_prozessVerwaltung.setProzessDaten( key, pd );

      pd.setProzessId( key );

      ssb.addProzess( key );

      pd.setStartStoppBlockId( ssb.getStartStoppBlockId() );

      if (!StartStoppApp.isTestBetrieb())
        m_prozessVerwaltung.starteProzess ( pd.getProzessId(), m_absenderId, 0l);

      z = E_ZUSTAND.GESTARTET;

      sendeStartStoppAntwort (pd.getProzessId(), z);
    }
    else
    {
      // bisherige Prozessdaten des Prozess laden
      
      ProzessDaten pdOrg = m_prozessVerwaltung.getProzessDaten( prozessId );
      
      if (pdOrg == null)
      {
        sendeStartStoppAntwort (E_FEHLER_STARTSTOPPBLOCK.FEHLER_PROZESS_ID, "Prozess ID nicht gefunden: " + prozessId);
        return;
      }

      // neue Prozessdaten eintragen
      
      pdOrg.setAusfuehrbareDatei( pd.getAusfuehrbareDatei() );
      
      pdOrg.setAufrufParameter ( pd.getAufrufParameter() );
      
      pdOrg.setStartArt( pd.getStartArt() );

      pdOrg.setStartBedingung( pd.getStartBedingung() );
      
      pdOrg.setStoppBedingung( pd.getStoppBedingung() );
      
      pdOrg.setStandardAusgabe( pd.getStandardAusgabe() );      
      
      pdOrg.setStandardFehlerAusgabe( pd.getStandardFehlerAusgabe() );      

      pdOrg.setStartVerhaltenFehler( pd.getStartVerhaltenFehler() );
      
      pdOrg.setStoppVerhaltenFehler( pd.getStoppVerhaltenFehler() );
      
      pdOrg.setSimulationsVariante( pd.getSimulationsVariante() );
      
      if (_debug)
        System.out.println("---> " + pdOrg.getProzessId());
      
      if (aktion.equalsIgnoreCase( _AKTION_STOPP ))
      {
        m_prozessVerwaltung.stoppeProzess ( pdOrg.getProzessId(), m_absenderId, false );
  
        z = E_ZUSTAND.GESTOPPT;
      }
  
      if (aktion.equalsIgnoreCase( _AKTION_NEUSTART ))
      {
        m_prozessVerwaltung.neuStartProzess ( pdOrg.getProzessId(), m_absenderId, pd.getSimulationsVariante() );
        
        z = E_ZUSTAND.GESTARTET;
      }

      if (aktion.equalsIgnoreCase( _AKTION_LOESCHEN ))
      {
        m_prozessVerwaltung.loescheProzess ( pdOrg.getProzessId(), m_absenderId);
        
        z = E_ZUSTAND.GELOESCHT;
      }

      sendeStartStoppAntwort (pdOrg.getProzessId(), z);
    }
  }

  /**
   * Auswerten der Attributgruppe "atg.startStoppAnfrage" Variante 2
   * @param data Datenteil Variante 2 der Attributgruppe "atg.startStoppAnfrage" 
   */
  private void updateVariante2( Data data )
  {
    long simuVariante = data.getScaledValue( "SimulationsVariante" ).longValue();
    
    String startStoppId  = data.getTextValue  ( "StartStoppID" ).getValueText();
    String aktion        = data.getTextValue  ( "Aktion" ).getValueText();

    String buffer = "Schalte über DaV: Variante 2 - Aktion = " + aktion.toString() + " StartStoppId = " + startStoppId + " simVariante = " + simuVariante;
    m_logbuch.schreibe( buffer );
    
    if (_debug)
    {
      System.out.println("-------------------------------------------");
      System.out.println( buffer );
    }

    if (!pruefeAktionV2PlusV3(m_absenderId, startStoppId, aktion, simuVariante))
      return;

    String xmlString = data.getTextValue  ( "xml" ).getValueText();

    String hilfsdatei = "";

    if (!xmlString.equals( "" ))
    {
      // XML-String in Hilfsdatei schreiben
      
      hilfsdatei = StartStoppApp.getStartStoppKonfiguration() + "/" + "tmp.xml";
      
      File f =new File (hilfsdatei);
  
      FileWriter fw;
      
      try
      {
        fw = new FileWriter (f);
  
        BufferedWriter bw = new BufferedWriter(fw);
  
        bw.write(xmlString);
  
        bw.close();
      }
  
      catch (IOException e)
      {
        hilfsdatei = "";

        e.printStackTrace();
      }
    }

    auswerteVariante2und3 (m_absenderId, hilfsdatei, aktion, startStoppId, simuVariante);
  }

  /**
   * Auswerten der Attributgruppe "atg.startStoppAnfrage" Variante 3
   * @param data Datenteil Variante 3 der Attributgruppe "atg.startStoppAnfrage" 
   */
  private void updateVariante3( Data data )
  {
    long simuVariante = data.getScaledValue( "SimulationsVariante" ).longValue();
    
    String startStoppId  = data.getTextValue  ( "StartStoppID" ).getValueText();
    String name          = data.getTextValue  ( "xmlDatei" ).getValueText();
    String aktion        = data.getTextValue  ( "Aktion" ).getValueText();

    String buffer = "Schalte über DaV: Variante 3 - Aktion = " + aktion.toString() + 
                                            " StartStoppId = " + startStoppId + 
                                            " Datei = " + name + 
                                            " simVariante = " + simuVariante;
    m_logbuch.schreibe( buffer );
    
    if (_debug)
    {
      System.out.println("-------------------------------------------");
      System.out.println( buffer );
    }

    if (!pruefeAktionV2PlusV3(m_absenderId, startStoppId, aktion, simuVariante))
      return;

    // Feststellen ob Pfad oder Dateiname übergeben wurde
    
    boolean isDatei = (name.toLowerCase().contains( ".xml" ));
    
    String datei = null;
    
    // Dateinamen bestimmen
    
    if (!isDatei)
      datei = name + "/" + Versionierung._startStoppDatei;
    else
      datei = name;
      
    // Feststellen ob Pfad absolut oder realitiv zu dem in -startStoppKonfiguration übergebenen
    // Pfad ist
    
    if (!(new File (datei)).exists())
    {
      String dateiRelativ = StartStoppApp.getStartStoppKonfiguration() + "/" + datei;
      
      if (!(new File (dateiRelativ)).exists())
      {
        sendeStartStoppAntwort (E_FEHLER_STARTSTOPPBLOCK.FEHLER_DATEI, "Datei \"" + datei + "\" nicht gefunden !");
        
        return;
      }
      
      datei = dateiRelativ;
    }
    
    auswerteVariante2und3 (m_absenderId, datei, aktion, startStoppId, simuVariante);
  }

  /**
   * Methode realisiert den gemeinsamen Teil von Variante 2 und 3
   * @param absenderId Absender ID
   * @param datei Datei mit XML-Stream
   * @param aktion Aktion
   * @param startStoppBlockId ID des StartStoppBlocks
   * @param simuVariante Simulationsvariante
   */
  private void auswerteVariante2und3 (long absenderId, String datei, String aktion, String startStoppBlockId, long simuVariante)
  {
    StartStoppVerwaltung ssv = StartStoppVerwaltung.getInstanz();
    
    // nur bei "Starten" wird die XML-Struktur ausgewertet  
    
    if (aktion.equalsIgnoreCase( _AKTION_START ))
    {
      if (datei != null)
      {
        // XML-Datei einlesen und syntaktisch prüfen
        
        Element root = XMLTools.leseRootElement( datei );
        if (root == null)
        {
          sendeStartStoppAntwort(E_FEHLER_STARTSTOPPBLOCK.FEHLER_XML, XMLTools.getFehlerText() );
          return;
        }

        // XML-Datei auswerten und semantisch prüfen
        
        SkriptVerwaltung sv = new SkriptVerwaltung (datei, absenderId, simuVariante);
        
        // StartStopp Block starten
        
        String id = sv.getStartStoppBlockId();

        if (sv.isFehlerStartStopp())
        {
          ssv.loescheStartStoppBlock( id, absenderId );

          sendeStartStoppAntwort (E_FEHLER_STARTSTOPPBLOCK.FEHLER_XML, "Plausibilitätsfehler");
          
          logger.error ("Plausibilitätsfehler, StartStopp Block kann nicht gestartet werden !");
          return;
        }
        
        // StartStopp Block laden
        
        StartStoppBlock ssb = ssv.getStartStoppBlock( id );
        
        if (ssb.isPlausibel())
        {
          ssb.starteProzesse ( absenderId, simuVariante );
          
          sendeStartStoppAntwort (id, E_ZUSTAND.GESTARTET);
        }
        else
        {
          ssv.loescheStartStoppBlock( id, absenderId );
          
          sendeStartStoppAntwort (E_FEHLER_STARTSTOPPBLOCK.FEHLER_XML, "Plausibilitätsfehler");
          
          logger.error ("Plausibilitätsfehler, StartStopp Block kann nicht gestartet werden !");
        }
      }
    }
    
    if (aktion.equalsIgnoreCase( _AKTION_STOPP ))
    {
      StartStoppBlock ssb = ssv.getStartStoppBlock( startStoppBlockId );
      if (ssb != null)
      {
        ssb.stoppeProzesse(  absenderId  );
        
        sendeStartStoppAntwort (startStoppBlockId, E_ZUSTAND.GESTOPPT);
      }
      else
        sendeStartStoppAntwort (E_FEHLER_STARTSTOPPBLOCK.FEHLER_BLOCK_ID, "Falsche StartStopp ID übergeben: " + startStoppBlockId);
    }

    if (aktion.equalsIgnoreCase( _AKTION_NEUSTART ))
    {
      StartStoppBlock ssb = ssv.getStartStoppBlock( startStoppBlockId );
      
      if (ssb != null)
      {
        ssb.neuStarteProzesse( absenderId, simuVariante );

        sendeStartStoppAntwort (startStoppBlockId, E_ZUSTAND.GESTARTET);
      }
      else
        sendeStartStoppAntwort (E_FEHLER_STARTSTOPPBLOCK.FEHLER_BLOCK_ID, "Falsche StartStopp ID übergeben: " + startStoppBlockId);
    }

    if (aktion.equalsIgnoreCase( _AKTION_LOESCHEN ))
    {
      StartStoppBlock ssb = ssv.getStartStoppBlock( startStoppBlockId );
      
      if (ssb != null)
      {
        ssv.loescheStartStoppBlock( startStoppBlockId, absenderId );
        
        sendeStartStoppAntwort (startStoppBlockId, E_ZUSTAND.GELOESCHT);
      }
      else
        sendeStartStoppAntwort (E_FEHLER_STARTSTOPPBLOCK.FEHLER_BLOCK_ID, "Falsche StartStopp ID übergeben: " + startStoppBlockId);
    }

  }
    
  /**
   * Auswerten der Attributgruppe "atg.startStoppAnfrage" Variante 4
   * @param data Datenteil Variante 4 der Attributgruppe "atg.startStoppAnfrage" 
   */
  private void updateVariante4( Data data )
  {
    if (m_prozessVerwaltung == null)
      m_prozessVerwaltung = ProzessVerwaltung.getInstanz();

    String prozessId = data.getTextValue( "ProzessID" ).getValueText(); 
    String aktion    = data.getTextValue( "Aktion" ).getValueText(); 
    String name      = data.getTextValue( "Name" ).getValueText();  

    String buffer = "Schalte über DaV: Variante 4 - Aktion = " + aktion.toString() + " PrID = " + prozessId + " " + name;
    m_logbuch.schreibe( buffer );
    
    if (_debug)
     System.out.println( buffer );

    if (!pruefeAktionV1PlusV4( m_absenderId, prozessId, name, aktion ))
      return;

    // Plausibilitätsprüfung
    
    if (aktion.equalsIgnoreCase( _AKTION_START ))
    {
      String id = m_prozessVerwaltung.getPidByName( name );
      
      if (id == null)
      {
        sendeStartStoppAntwort (E_FEHLER_STARTSTOPPBLOCK.FEHLER_PROZESS, "Prozess mit Name \"" + name + "\" nicht bekannt !");
        return;
      }

      prozessId = id;
    }
      
    // Prüfen ob Id stimmt   
        
    ProzessDaten pd = m_prozessVerwaltung.getProzessDaten( prozessId );
    
    if (pd == null)
    {
      sendeStartStoppAntwort (E_FEHLER_STARTSTOPPBLOCK.FEHLER_PROZESS_ID, "Prozess mit Id \"" + prozessId + "\" nicht bekannt !");
      return;
    }

    E_ZUSTAND z = null;
    
    if (aktion.equalsIgnoreCase( _AKTION_START ))
    {
      m_prozessVerwaltung.starteProzess ( prozessId, m_absenderId, pd.getSimulationsVariante() );
      
      z = E_ZUSTAND.GESTARTET;
    }

    if (aktion.equalsIgnoreCase( _AKTION_STOPP ))
    {
      m_prozessVerwaltung.stoppeProzess ( prozessId, m_absenderId, false );

      z = E_ZUSTAND.GESTOPPT;
    }

    if (aktion.equalsIgnoreCase( _AKTION_LOESCHEN ))
    {
      m_prozessVerwaltung.loescheProzess( prozessId, m_absenderId );

      z = E_ZUSTAND.GELOESCHT;
    }

    if (aktion.equalsIgnoreCase( _AKTION_NEUSTART ))
    {
      m_prozessVerwaltung.neuStartProzess ( prozessId, m_absenderId, pd.getSimulationsVariante() );

      z = E_ZUSTAND.GESTARTET;
    }

    sendeStartStoppAntwort (pd.getProzessId(), z);
  }
  
  /**
   * Methode prüft, ob als Aktion ein zulässiges Schlüsselwort
   * übergeben wurde. Ebeso wird geprüft, ob die Kombination Aktion - Pid - Name korrekt
   * nach folgendem Schema verwendet wurden:
   * - Start: Pid = null, Name != null
   * - Stopp: Pid != null, Name = null
   * - Neustart: Pid != null, Name = null
   * @param absender Absender
   * @param prozessId Prozess ID
   * @param name Name 
   * @param aktion zu prüfende Aktion
   * @return true Aktion bzw. Kombination ok, sonst false
   */
  private boolean pruefeAktionV1PlusV4 (long absender, String prozessId, String name, String aktion)
  {
    if (!aktion.equalsIgnoreCase( _AKTION_START    ) &&
        !aktion.equalsIgnoreCase( _AKTION_NEUSTART ) &&
        !aktion.equalsIgnoreCase( _AKTION_STOPP    ) &&
        !aktion.equalsIgnoreCase( _AKTION_LOESCHEN ))
    {
      sendeStartStoppAntwort (E_FEHLER_STARTSTOPPBLOCK.FEHLER_AKTION, "Falsche Aktion: " + aktion);
      return false;
    }
    
    // Bei Aktion "Starten" muss die ID leer sein und ein Name übergeben werden
    
    if (aktion.equalsIgnoreCase( _AKTION_START ))
    {
      if (!(prozessId.equals("")))
      {
        sendeStartStoppAntwort (E_FEHLER_STARTSTOPPBLOCK.FEHLER_AKTION_PID_NAME, "Fehler Pid wurde bei Aktion \"starten\" übergeben");
        return false;
      }
      
      if (name.equals(""))
      {
        sendeStartStoppAntwort (E_FEHLER_STARTSTOPPBLOCK.FEHLER_AKTION_PID_NAME, "Fehler kein Name wurde bei Aktion \"starten\" übergeben");
        return false;
      }
    }

    // Bei Aktion "Stoppen bzw. Neustart" muss eine ID übergeben werden und Name leer sein
    
    if (aktion.equalsIgnoreCase( _AKTION_NEUSTART ) ||
        aktion.equalsIgnoreCase( _AKTION_STOPP    ) ||
        aktion.equalsIgnoreCase( _AKTION_LOESCHEN ))
    {
      ProzessDaten pd = m_prozessVerwaltung.getProzessDaten( prozessId );
      if (pd == null)
      {
        sendeStartStoppAntwort (E_FEHLER_STARTSTOPPBLOCK.FEHLER_PROZESS_ID, "Falsche Prozess ID: " + prozessId);
        return false;
      }
      
      if (!name.equals(""))
      {
        if (!pd.getName().equals( name ))
        {
          sendeStartStoppAntwort (E_FEHLER_STARTSTOPPBLOCK.FEHLER_AKTION_PID_NAME, "Fehler Name wurde bei Aktion \"" + aktion +  "\" übergeben");
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Methode prüft, ob als Aktion ein zulässiges Schlüsselwort
   * übergeben wurde. Ebenso wird geprüft, ob die Kombination Aktion - Pid - Name korrekt
   * nach folgendem Schema verwendet wurden:
   * - SimulationsVariante 0 bis 999
   * - Start: Pid = null
   * - Stopp: Pid != null
   * - Neustart: Pid != null
   * @param absender Absender
   * @param startStoppId ID des StartStopp Blocks
   * @param aktion zu prüfende Aktion
   * @return true Aktion bzw. Kombination ok, sonst false
   */
  private boolean pruefeAktionV2PlusV3 (long absender, String startStoppId, String aktion, long simuVariante)
  {
    if ((simuVariante < 0) || (simuVariante > 999))
    {
      sendeStartStoppAntwort(E_FEHLER_STARTSTOPPBLOCK.FEHLER_AKTION, "Falsche SimulationsVariante: " + simuVariante);
    	return false;
    }

    if (!aktion.equalsIgnoreCase( _AKTION_START    ) &&
        !aktion.equalsIgnoreCase( _AKTION_NEUSTART ) &&
        !aktion.equalsIgnoreCase( _AKTION_STOPP    ) &&
        !aktion.equalsIgnoreCase( _AKTION_LOESCHEN ))
    {
      sendeStartStoppAntwort (E_FEHLER_STARTSTOPPBLOCK.FEHLER_AKTION, "Falsche Aktion: " + aktion);
      return false;
    }
    
    // Bei Aktion "Starten" muss die ID leer sein 
    
    if (aktion.equalsIgnoreCase( _AKTION_START ))
    {
      if (!(startStoppId.equals("")))
      {
        sendeStartStoppAntwort (E_FEHLER_STARTSTOPPBLOCK.FEHLER_AKTION_PID_NAME, "Fehler Pid wurde bei Aktion \"starten\" übergeben");
        return false;
      }
    }

    // Bei Aktion "Stoppen bzw. Neustart" muss eine ID übergeben werden
    
    if (aktion.equalsIgnoreCase( _AKTION_NEUSTART ) ||
        aktion.equalsIgnoreCase( _AKTION_STOPP    ) || 
        aktion.equalsIgnoreCase( _AKTION_LOESCHEN )) 
    {
      if (startStoppId.equals(""))
      {
        sendeStartStoppAntwort (E_FEHLER_STARTSTOPPBLOCK.FEHLER_AKTION_PID_NAME, "Fehler keine Pid bei Aktion \"" + aktion +  "\" übergeben");
        return false;
      }
    }

    return true;
  }

  /**
   * Methode erzeugt die StartStopp Antwort (att.startStoppAntwort) und sendet diese an den DaV.
   * @param fehler Kodierung des Fehler (de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_FEHLER_STARTSTOPPBLOCK)
   * @param fehlerText Fehlertext (nur für Debug bzw. Logbuch)
   */
  private void sendeStartStoppAntwort (E_FEHLER_STARTSTOPPBLOCK fehler, String fehlerText)
  {
    // Debugausgabe
    
//    System.err.println (fehlerText);
    
    // Logbuch
    
    m_logbuch.schreibe(fehlerText );

    // Antwort an DaV
    
    m_daVKommunikation.sendeStartStoppAntwort( m_absenderId, 
                                               m_absenderZeichen,
                                               "",
                                               E_ZUSTAND.FEHLER,
                                               fehler);
  }

  /**
   * Methode erzeugt die StartStopp Antwort (att.startStoppAntwort) im Normalfall und sendet diese
   * an den DaV.
   * @param id Id der Inkarnation bzw. des StartStopp Blocks
   * @param zustand Zustand der Inkarantion bzw. des StartStopp Blocks (de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.Zustand)
   */
  private void sendeStartStoppAntwort (String id, E_ZUSTAND zustand)
  {
    // Antwort an DaV
    
    m_daVKommunikation.sendeStartStoppAntwort( m_absenderId, 
                                               m_absenderZeichen,
                                               id,
                                               zustand,
                                               E_FEHLER_STARTSTOPPBLOCK.KEIN);
  }
}
