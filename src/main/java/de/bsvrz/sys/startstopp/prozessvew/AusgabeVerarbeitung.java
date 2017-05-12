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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.bsvrz.sys.startstopp.skriptvew.StartStoppApp;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_AUSGABE;

/**
 * Klasse zum Einlesen bzw. Auswerten der Standardausgabe bzw. Standardfehlerausgabe
 * einer Inkarnation. Entsprechend den Vorgaben aus der Datei startStopp.xml werden
 * die Daten in Dateien gespeichert oder ignoriert. Das Einlesen der Daten erfolgt in
 * einem Thread. Der Thread wird automatisch durch den Konstruktor der Klasse gestartet.
 */
public class AusgabeVerarbeitung extends Thread implements Serializable
{
  private static final long serialVersionUID = 1L;

  /**
   * Ausgaben zusätzlich auf der Konsole der StartStopp Applikation ausgeben 
   */
  private boolean m_ausgabeKonsole = false;
  
  /**
   * Verweis auf Inkarnation für die Ausgaben ausgewertet werden
   */
  private String m_inkarnation;
  
  /**
   * Stream Standardausgabe
   */
  private InputStream m_standardAusgabe;
  
  /**
   * Dateiname für Standardausgabe
   */
  private String m_dateiStandardAusgabe;
  
  /**
   * max. Grösse der Datei für die Standardausgabe
   */
  private long m_groesseStandardAusgabe;
  
  /**
   * Option für Standardaausgabe
   */
  private E_AUSGABE m_optionenStandardAusgabe;

  /**
   * Stream Standardfehlerausgabe
   */
  private InputStream m_standardFehlerAusgabe;
  
  /**
   * Dateiname für Standardfehlerausgabe
   */
  private String m_dateiStandardFehlerAusgabe;

  /**
   * max. Grösse der Datei für die Standardfehlerausgabe
   */
  private long m_groesseStandardFehlerAusgabe;
  
  /**
   * Option für Standardaausgabe
   */
  private E_AUSGABE m_optionenStandardFehlerAusgabe;

  /**
   * Singelton Instanz für Klasse ProzessVerwaltung
   */
  private ProzessVerwaltung m_prozessVerwaltung = null;

  /**
   * Singelton Instanz für Klasse SchreibeProtokoll
   */

  private SchreibeProtokoll m_schreibeProtokoll = null;
  {
    
  }

  /**
   * Konstruktor der Klasse, starten automatisch den Thread der die Eingangsströmer
   * verarbeitet.
   * @param inkarnation Name der Inkarnation
   * @param standardAusgabe Datenstrom Standardausgabe
   * @param dateiStandardAusgabe Dateiname für die Standardausgane
   * @param groesseStandardAusgabe max. Dateigrösse für die Standardausgabe
   * @param optionenStandardAusgabe Option für das Bearbeiten der Standardausgabe
   * @param standardFehlerAusgabe Datenstrom Standardfehlerausgabe
   * @param dateiStandardFehlerAusgabe Dateiname für die Standardfehlerausgane
   * @param groesseStandardFehlerAusgabe max. Dateigrösse für die Standardfehlerausgabe
   * @param optionenStandardFehlerAusgabe Option für das Bearbeiten der Standardfehlerausgabe
   */
  public AusgabeVerarbeitung (String inkarnation,
      
                              InputStream standardAusgabe,
                              String dateiStandardAusgabe,
                              long groesseStandardAusgabe,
                              E_AUSGABE optionenStandardAusgabe,

                              InputStream standardFehlerAusgabe,
                              String dateiStandardFehlerAusgabe,
                              long groesseStandardFehlerAusgabe,
                              E_AUSGABE optionenStandardFehlerAusgabe)
  {
    // Singleton initialisieren
    
    m_prozessVerwaltung = ProzessVerwaltung.getInstanz();
    m_schreibeProtokoll = SchreibeProtokoll.getInstanz();
    
    // Membervariablen initialisieren
    
    m_inkarnation = inkarnation;
    
    m_standardAusgabe         = standardAusgabe;
    m_dateiStandardAusgabe    = dateiStandardAusgabe;
    m_groesseStandardAusgabe  = groesseStandardAusgabe;
    m_optionenStandardAusgabe = optionenStandardAusgabe;

    m_standardFehlerAusgabe         = standardFehlerAusgabe;
    m_dateiStandardFehlerAusgabe    = dateiStandardFehlerAusgabe;
    m_groesseStandardFehlerAusgabe  = groesseStandardFehlerAusgabe;
    m_optionenStandardFehlerAusgabe = optionenStandardFehlerAusgabe;

    //----------------------------------------------------------------------
    //Thread starten
    //----------------------------------------------------------------------
    
    this.start();
    this.setName( "Ausgabeverarbeitung " + inkarnation );
  }

  /**
   * Run Methode des Threads. In dieser Methode wird zyklisch (sekündlich) geprüft ob neue
   * Eingangsdaten vorliegen und wenn ja, werden diese entsprechend den Vorgaben verarbeitet.
   */
  public void run ()
  {
    // Prozessdaten der Inkarnation laden
    
    String pid = m_prozessVerwaltung.getPidByName( m_inkarnation );
    ProzessDaten pd = m_prozessVerwaltung.getProzessDaten(  pid );
    
    BufferedReader stdInput = new BufferedReader (new InputStreamReader (m_standardAusgabe));
    BufferedReader stdError = new BufferedReader (new InputStreamReader (m_standardFehlerAusgabe));
    
    // Einlesen im Endlosloop
    
    while (pd.isGestartet())
    {
      String input = null;
      String error = null;
      
      try
      {
        if (stdInput.ready())
          input = stdInput.readLine();
        
        if (stdError.ready())
          error = stdError.readLine();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
      
      if (input != null)
      {
        if (m_ausgabeKonsole || StartStoppApp.isAusgabeAufKonsole())
          System.out.println(input);
        
        auswerteEingangsStrom (m_inkarnation, 0, input, m_dateiStandardAusgabe, m_groesseStandardAusgabe, m_optionenStandardAusgabe );
      }
      
      if (error != null)
      {
        if (m_ausgabeKonsole || StartStoppApp.isFehlerAusgabeAufKonsole())
          System.err.println(pd.getName() + " Fehler: " + error);
        
        auswerteEingangsStrom (m_inkarnation, 1, error, m_dateiStandardFehlerAusgabe, m_groesseStandardFehlerAusgabe, m_optionenStandardFehlerAusgabe );
      }

      // Nur wenn keine Daten vorliegen wird ein Sleep ausgeführt.
      
      if (input == null && error == null)
        mySleep( 1000 );
      
    } // while
  }
  
  /**
   * Methode zum den Thread schlafend zu legen
   * @param ms
   */
  private void mySleep (int ms)
  {
    try
    {
      sleep (ms);
    }
    catch ( InterruptedException e )
    {
      e.printStackTrace();
    }
  }
   
  /**
   * Methode zum Verarbeiten der Eingangsstöme entsprechend den Vorgaben aus der
   * Datei startStopp.xml.
   * @param inkarnation Inkarnation
   * @param art 0 - Standardausgabe, 1 - Standardfehlerausgabe
   * @param text Text der gespeichert werden soll
   * @param datei Dateiname in die gespeichert werden soll
   * @param groesse max. Grösse der Datei in MB
   * @param option Option aus der StartStopp.xml
   */
  private void auswerteEingangsStrom( String inkarnation, int art, String text, String datei, 
                                      long groesse, E_AUSGABE option )
  {
    // Kennzeichnen ob der Text von der Standardausgabe oder der
    // Standardfehlerausgabe kommt
    
    if (art == 0)
      inkarnation += ";S";
    else
      inkarnation += ";F";
        
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy;HH:mm:ss");
    String uhrzeit = sdf.format(new Date());
  
    // Text fertig formatieren
    
    String inhalt = uhrzeit + ";" + inkarnation + ";" + text + "\r\n";

    // Ausgabe ignorieren
    
    if (option == E_AUSGABE.IGNORIEREN)
    {
      // nichts tun
    }
    else
    {
      
      // Prüfen ob eine Datei mehrfach verwendet wird. Wenn ja, dann wird synchronisiert
      // in diese Datei geschrieben.
      
      boolean mehrfach = m_prozessVerwaltung.wirdDateiMehrfachVerwendet(datei);
      
      // Ausgabe in mehrfachgenutzte Datei
      
      if ((option == E_AUSGABE.GEMEINSAMEDATEI) || mehrfach)
      {
        m_schreibeProtokoll.schreibeSynchronisiertInDatei(datei, inhalt, groesse);
      }
      else
      {
        // Ausgabe in exklusive Datei
    
        if (option == E_AUSGABE.EIGENEDATEI)
        {
          m_schreibeProtokoll.schreibeUnSynchronisiertInDatei(datei, inhalt, groesse);
        }
      }
    }
  }
}

