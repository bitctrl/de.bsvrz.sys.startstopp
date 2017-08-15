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
import java.text.SimpleDateFormat;
import java.util.Date;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.skriptvew.StartStoppApp;

/**
 * Klasse realisiert ein Logbuch in dem Systemmeldungen von StartStopp gespeichert werden. 
 * Die Klasse wird als Singelton ausgeführt. Für jeden Tag wird eine neue Datei angelegt.  
 * @author Dambach Werke GmbH
 *
 */
public class Logbuch
{
  private static final Debug logger = Debug.getLogger();
  
  /**
   * Pfad in dem das Logbuch abgelegt wird
   */
  private String     m_pfad = null;
  
  /**
   * Logbuch Datei
   */
  private File       m_file = null;
  
  /**
   * Filewriter für diese Datei
   */
  private FileWriter m_fileWriter = null;

  /**
   * Konstruktor der Klasse
   */
  private Logbuch ()
  {
    m_pfad = StartStoppApp.getStartStoppKonfiguration();
    if (m_pfad == null)
    {
      System.err.println("Logbuch: kein Verzeichnis für Logdateien angegeben");
      m_pfad = null;
      return;
    }
    
    File directory = new File (m_pfad);
    if (!directory.exists())
    {
      System.err.println("Logbuch: Verzeichnis \"" + m_pfad + "\" existiert nicht");
      m_pfad = null;
      return;
    }
  }

  /**
   * Methode liefert die einzige Instanz der Klasse zurück
   * @return einzige Instanz der Klasse
   */
  public static Logbuch getInstanz ()
  {
    return Inner.INSTANCE;
  }

  /**
   * Innere Klasse zum Sicherstellen, dass wirklich nur eine Instanz der Klasse
   * gebildet wird
   * @author Dambach Werke GmbH
   */
  public static class Inner
  {
    private static Logbuch INSTANCE = new Logbuch ();
  }
  
  /**
   * Methode zum Schreiben in das Versionierung. Der übergebene Text wird mit Datum
   * und Uhrzeit versehen und am Ende mit \r\n erweitert (DOS-Format).
   * @param text zu schreibender Text
   */
  public synchronized void schreibe (String text)
  {
    schreibe ("", text); 
  }
  
  /**
   * Methode zum Schreiben in das Logbuch. Der übergebene Text wird mit Datum
   * und Uhrzeit versehen und am Ende mit \r\n erweitert (DOS-Format).
   * @param kennung Kennung nach der Uhrzeit nach der später in der Datei
   * gefiltert werden kann
   * @param text zu schreibender Text
   */
  public synchronized void schreibe (String kennung, String text)
  {
    logger.fine(kennung + " " + text);
    
//    if (m_pfad == null)
//      return;
//    
//    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//    String uhrzeit = sdf.format(new Date());
//    
//    String m_datei = m_pfad + "/" + "logbuch_" + uhrzeit + ".txt";
//    
//    m_file = new File (m_datei);
//    if (m_file.exists())
//    
//    sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
//    uhrzeit = sdf.format(new Date());
//
//    String buffer = uhrzeit + ";" + kennung + ";" + text + "\r\n";
//
//    try
//    {
//      m_fileWriter = new FileWriter (m_file, true);
//    }
//    catch (IOException e)
//    {
//      m_fileWriter = null;
//      e.printStackTrace();
//    }
//    
//    if (m_fileWriter != null)
//    {
//      BufferedWriter bw = new BufferedWriter(m_fileWriter);
//  
//      try
//      {
//        bw.write (buffer);
//        bw.close ();
//      }
//      catch (IOException e)
//      {
//        e.printStackTrace();
//      }
//    }
  }
}
