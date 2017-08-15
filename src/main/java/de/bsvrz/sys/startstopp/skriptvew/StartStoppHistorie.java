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
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.jdom.Element;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.prozessvew.Logbuch;

/**
 * Klasse zum Verwalten der Historie der StartStopp Dateien. Die Klasse wird als Singelton
 * ausgelegt.
 * @author Dambach Werke GmbH
 */
public class StartStoppHistorie 
{
  private static final long serialVersionUID = 1L;

  /**
   * Debug
   */
  private static final Debug logger = Debug.getLogger();

  /**
   * Name der Datei in der die History abgeelgt wird
   */
  private final String _startStoppHistory = "startStoppHistory.xml";
  
  /**
   * Höchste bisher vergebene Versionsnummer
   */
  private int m_hoechsteVersionsNummer = 0;

  /**
   * Liste mit bisherigen Historydaten
   */
  private List<HistoryData> m_historie = new ArrayList<HistoryData> ();
  
  /**
   * Privater Konstruktor der Klasse (Singelton)
   */
  private StartStoppHistorie()
  {
  }

  /**
   * Geschachtelte Innere Klasse (wird verwendet um zu garantieren, dass
   * wirklich nur eine Instanz der Klasse angelegt wird).
   * @author Dambach Werke GmbH
   *
   */
  private static class Inner
  {
    private static StartStoppHistorie INSTANCE = new StartStoppHistorie ();
  }

  /**
   * Methode liefert die einzige Instanz der Klasse
   * @return einzige Instanz der Klasse
   */
  public static StartStoppHistorie getInstanz()
  {
    return Inner.INSTANCE;
  }
  
  /**
   * Methode liest die bisherige Historydatei ein. Über den Parameter "erforderlich"
   * kann eingestellt werden, ob die Datei für den Start von StartStopp erforderlich
   * ist (z.B. beim normalen Starten) oder nicht erforderlich ist (z.B. beim 1. Start
   * für die Versionierung)
   * @param erforderlich Datei erforderlich oder nicht
   */
  public void leseHistory (boolean erforderlich)
  {
    String datei = StartStoppApp.getStartStoppKonfiguration() + "/" + _startStoppHistory; 
    
    if (erforderlich)
    {
      if (!(new File (datei)).exists())
      {
        StartStoppApp.beendeStartStoppWegenFehler( "Fehler: Historydatei \"" + datei + "\" nicht gefunden ! ");      
      }
    }

    Element root = XMLTools.leseRootElement( datei );
    
    if (root == null)
    {
      StartStoppHistoryXML xml = new StartStoppHistoryXML ();
      
      root  = xml.getRoot();
      
      StrukturSchreiben s = new StrukturSchreiben (1, root, datei);
    }
    
    ListIterator<Element> listeKinder = root.getChildren().listIterator();
    
    while (listeKinder.hasNext())
    {
      Element child = listeKinder.next();
      
      if (child.getName() == "startStoppVersioniert")
      {
        String versionsnummer   = child.getAttribute( "Versionsnummer" ).getValue();
        String erstelltAm       = child.getAttribute( "ErstelltAm" ).getValue();
        String erstelltDurch    = child.getAttribute( "ErstelltDurch" ).getValue();
        String aenderungsGrund  = child.getAttribute( "Aenderungsgrund" ).getValue();
        String checksumme       = child.getAttribute( "Checksumme" ).getValue();
        
        int v = 0;
        
        try
        {
          v = Integer.parseInt( versionsnummer );
        }
        catch ( Exception e )
        {
          v = 0;
        }                
        
        if (m_hoechsteVersionsNummer < v)
          m_hoechsteVersionsNummer = v;
        
        HistoryData data = new HistoryData (versionsnummer, erstelltAm, erstelltDurch, aenderungsGrund, checksumme);
        
        m_historie.add( data );
      }
      
    } // while
    
//    String s = "Höchste Version = " + m_hoechsteVersionsNummer;
//    System.out.println(s);
  }
  
  /**
   * Methode zum Hinzufügen der Eigenschaften eines StartStopp Blocks zu der Historie
   * @param version Version des StartStopp Blocks
   * @param datum Datum des StartStopp Blocks
   * @param benutzer Benutzer des StartStopp Blocks
   * @param ursache Ursache des StartStopp Blocks
   * @param hashwert Hashwert des StartStopp Blocks
   */
  public void addHistory (String version, String datum, String benutzer, String ursache, String hashwert)
  {
    HistoryData h = new HistoryData (version, datum, benutzer, ursache, hashwert);
    
    m_historie.add( h );
    
    StartStoppHistoryXML xml = new StartStoppHistoryXML ();
    
    Element root  = xml.getRoot();

    String datei = StartStoppApp.getStartStoppKonfiguration() + "/" + _startStoppHistory; 

    StrukturSchreiben s = new StrukturSchreiben (1, root, datei);
    
    int v;
    
    try
    {
      v = Integer.parseInt( version );
    }
    catch ( Exception e )
    {
      v = 0;
    }                
    
    if (m_hoechsteVersionsNummer < v)
      m_hoechsteVersionsNummer = v;

  }

  /**
   * @return liefert die Klassenvariable m_hoechsteVersionsNummer zurück
   */
  public int getHoechsteVersionsNummer()
  {
    return m_hoechsteVersionsNummer;
  }
  
  /**
   * Methode zum Lesen der bisherigen Historie
   * @return bisherige Historie
   */
  public List<HistoryData> getHistory ()
  {
    return m_historie;
  }
  
  /**
   * Methode liefert den Hashwert der letzten StartStopp Datei
   * (Datei mit höchster Versionsnummer)
   * @return Hashwert
   */
  public String getAktuellerHashwert ()
  {
    for (int i=0; i<m_historie.size(); ++i)
    {
      HistoryData h = m_historie.get( i );
      
      if (h.getVersion().equals( "" + m_hoechsteVersionsNummer))
        return h.getHashwert();
    }
    
    return null;
  }
}
