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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jdom.Element;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.prozessvew.Logbuch;

/**
 * Klasse realisiert die Versionierung der StartStopp Dateien. Die Versionierung kann auf 
 * folgende Arten durchgeführt werden:<br>
 * 1. Versionierung einer StartStopp Datei. Hierbei wird der Name einer StartStopp Datei übergeben.
 * Die Attribute erstelltDurch bzw. aenderungsGrund werden dann der Datei entnommen.<br>
 * 2. Versionierung durch die Bedienoberfläche (Telnet). Die Attribute erstelltDurch bzw. aenderungsGrund
 * werden dann übergeben. 
 * @author Dambach Werke GmbH
 */
public class Versionierung
{
  /**
   * Debug
   */ 
  private static final Debug logger = Debug.getLogger();

  /**
   * Name der StartStopp Datei ohne Extension
   */
  
  public final static String _startStoppName = "startStopp";
  
  /**
   * Extension der StartStopp Datei
   */
  
  public final static String _startStoppExtension = ".xml";
  
  /**
   * Vollständiger Name der StartStopp Datei (Name + Extension)
   */
  
  public final static String _startStoppDatei = _startStoppName + _startStoppExtension;
  
  /**
   * Bei Versionierung trat ein Fehler auf
   */
  
  private boolean m_fehler = false;
  
  /**
   * Fehlermeldung im Fehlerfall
   */
  private String m_fehlerText = null;
  
  /**
   * Datei die versioniert werden soll
   */
  private static String m_datei = null;
  
  /**
   * Konstruktor für die Versionierung einer neuen StartStopp Datei
   * @param datei Name der zu versionierenden StartStopp Datei
   */
  public Versionierung (String datei)
  {
    m_datei = datei;
    
    logbuch ("Versionierung: Datei = " + datei );
    
    Element root = XMLTools.leseRootElement( datei );
    
    if (root != null)
      initialize (root);
    else
    {
      m_fehler = true;
      m_fehlerText = XMLTools.getFehlerText();
    }
  }

  /**
   * Konstuktor für die Versionierung durch die Bedienoberfläche
   * @param benutzer Benutzername des Benutzers der die Versionierung durchführt
   * @param ursache Ursache warum Versionierung durchgeführt wird.
   */
  public Versionierung (String benutzer, String ursache)
  {
    m_datei = null;
    
    logbuch( "Versionierung: aktuelle Einstellungen" );

    // aktuelle Einstellungen in eine XML-Struktur schreiben
    
    StartStoppXML xml = new StartStoppXML ();
    
    Element root = xml.getRoot();
    
    Element startStoppElement = root.getChild("startStopp");
    
    startStoppElement.setAttribute ("ErstelltDurch",      benutzer );
    startStoppElement.setAttribute ("Aenderungsgrund",    ursache  );
    
    initialize (root);
  }

  /**
   * Methode zum Versionieren einer StartStopp Struktur als XML-Struktur
   * @param root Rootelement der XML-Struktur
   */
  private void initialize (Element root)
  {
    Logbuch l = Logbuch.getInstanz();
    
    String buffer = null;
    String pfad = StartStoppApp.getStartStoppKonfiguration();

    // Prüfen ob sich in dem Pfad bereits eins StartStopp Datei befindet, wenn ja
    // dann wird von dieser Datei eine Sicherheitskopie angefertigt.
    
    String startStoppDatei = pfad + "/" + _startStoppDatei;
    
    if ((new File (startStoppDatei)).exists())
    {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
      String uhrzeit = sdf.format(new Date());
      
      String tmpDatei = pfad + "/" + _startStoppName + "_" + uhrzeit + _startStoppExtension;
      
      if (copyFile (startStoppDatei, tmpDatei))
      {
        logbuch ("Bisherige StartStopp Konfiguration in Datei \"" + tmpDatei + "\" gesichert");
        
        m_fehler     = false;
        m_fehlerText = "";
      }
      else
      {
        logbuch ("Versuch bisherige StartStopp Konfiguration in Datei \"" + tmpDatei + "\" zu sichern fehlgeschlagen !");

        m_fehler = true;
        m_fehlerText = buffer;
      }
    }
    
    if (!m_fehler)
    {
      Element startStoppElement = root.getChild("startStopp");

      // Versionsnummer anpassen
      
      int version = StartStoppHistorie.getInstanz().getHoechsteVersionsNummer() + 1;
      
      System.out.println("Versionsnummer = " + version);

      startStoppElement.setAttribute("Versionsnummer", "" + version );

      // Datum anpassen
      
      SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
      String datum = sdf.format( new Date() );
      
      startStoppElement.setAttribute ("ErstelltAm",  datum );

      // Neue StartStopp Datei erzeugen

      StrukturSchreiben s = new StrukturSchreiben (0, root, startStoppDatei);
      
      // Hashwert der Datei bestimmen
      
      String hashwert = Hashwert.berechneHashwert( "MD5", startStoppDatei );
      
      // Daten versionieren
      
      StartStoppHistorie.getInstanz().addHistory( startStoppElement.getAttribute ("Versionsnummer").getValue(),
                                        startStoppElement.getAttribute ("ErstelltAm").getValue(),
                                        startStoppElement.getAttribute ("ErstelltDurch").getValue(),
                                        startStoppElement.getAttribute ("Aenderungsgrund").getValue(),
                                        hashwert);
    }
  }

  /**
   * Methode zum Kopieren einer Datei in eine andere
   * @param quelle
   * @param ziel
   * @return Kennung ob Kopieren erfolgreich war oder nicht
   */
  private static boolean copyFile (String quelle, String ziel)
  {
    FileInputStream fis;
    BufferedInputStream bis;
    FileOutputStream fos;
    BufferedOutputStream bos;
    
    byte[] b;
    
    // Quelle öffnen
    
    try
    {
      fis = new FileInputStream  (quelle);
      fos = new FileOutputStream (ziel);
    }
    catch (FileNotFoundException e)
    {
      System.out.println("Datei nicht gefunden ");
      return false;
    }
    
    // Dateien umlenken auf Puffer
    
    bis = new BufferedInputStream (fis);
    bos = new BufferedOutputStream (fos);
    
    try
    {
      b = new byte [bis.available()];
      bis.read (b);
      bos.write( b );
      
      bis.close();
      bos.close();
    }
    catch (IOException e)
    {
      logbuch ("Dateien wurden nicht kopiert");
      return false;
    }
    
    return true;
  }

  /**
   * @return liefert die Klassenvariable m_fehler zurück
   */
  public boolean isFehler()
  {
    return m_fehler;
  }
  
  /**
   * @return liefert die Klassenvariable m_fehlerText zurück
   */
  public String getFehlerText()
  {
    return m_fehlerText;
  }

  /**
   * Methode gibt einen Debugtext aus in Abhängigkeit der Klassenvariablen m_datei auf stdout bzw. den
   * Datebverteilerlogger
   * @param text Text der ausgegeben werden soll
   */
  private static void logbuch (String text)
  {
    if (m_datei == null)
      logger.info (text);
    else
      System.out.println(text);
  }
}
