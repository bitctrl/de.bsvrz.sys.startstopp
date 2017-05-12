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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.bsvrz.sys.startstopp.prozessvew.ProzessVerwaltung.Inner;

/**
 * Klasse zum Abspeichern der Meldungen der Standardausgabe bzw. der Standardfehlerausgabe
 * in einer Datei. Die Klasse wird als Singelton ausgeführt um den gemeinsamen Zugriff
 * auf eine Datei, von mehreren Applikationen aus, synchronisiert zu ermöglichen.
 * @author Dambach Werke GmbH
 *
 */
public class SchreibeProtokoll
{
  /**
   * Name der aktuellen Datei
   */
  private String m_aktDatei = null;
  
  /**
   * Datei 1
   */
  private File m_datei1;
  
  /**
   * Datei 2
   */
  private File m_datei2;
  
  /**
   * Konstruktor der Klasse
   */
  private SchreibeProtokoll ()
  {
    
  }

  /**
   * Methode liefert die einzige Instanz der Klasse
   * @return einzige Instanz der Klasse
   */
  public synchronized static SchreibeProtokoll getInstanz ()
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
    private static SchreibeProtokoll INSTANCE = new SchreibeProtokoll();
  }
  
  /**
   * Synchronisiertes Schreiben in eine Datei
   * @param dateiName Name der Datei 
   * @param inhalt Protokolldaten
   * @param maxGrösse Maximale Grösse der Datei in MB
   */
  public synchronized void schreibeSynchronisiertInDatei( String dateiName, String inhalt, long maxGrösse )
  {
    schreibeInDatei (dateiName, inhalt, maxGrösse);
  }

  /**
   * unsynchronisiertes Schreiben in eine Datei
   * @param dateiName Name der Datei 
   * @param inhalt Protokolldaten
   * @param maxGrösse Maximale Grösse der Datei in MB
   */
  public void schreibeUnSynchronisiertInDatei( String dateiName, String inhalt, long maxGrösse ) 
  {
    schreibeInDatei (dateiName, inhalt, maxGrösse);
  }

  /**
   * Hilfmethode zum Schreiben in eine Datei. In dieser Methode wird geprüft ob die Datei
   * die Extension .txt hat, wenn nicht wird die Extension angehängt. Die erzeugten 
   * Dateien haben dann letztendlich die Extensionen datei_1.txt bzw. datei_2.txt. 
   * @param dateiName Dateiname
   * @param inhalt Inhalt
   * @param maxGrösse max. Grösse der Datei in MB
   */
  private void schreibeInDatei ( String dateiName, String inhalt, long maxGrösse )
  {
    String dateiTmp = null;
    
    // Prüfen ob die Datei die Extension ".txt" hat

    if (!dateiName.toLowerCase().endsWith( ".txt" ))
      dateiTmp = dateiName + ".txt";
    else
      dateiTmp = dateiName;
    
    String dateiName1 = dateiTmp.replace( ".txt", "_1.txt" );
    String dateiName2 = dateiTmp.replace( ".txt", "_2.txt" );
    
    String aktDatei = bestimmeAktuelleDatei (dateiName1, dateiName2, maxGrösse);
    
    m_aktDatei = aktDatei;

    FileWriter fw = null;
    try
    {
      fw = new FileWriter(aktDatei , true);
    }
    catch ( IOException e )
    {
      e.printStackTrace();
    }
    
    try
    {
      fw.write( inhalt );
    }
    catch ( IOException e )
    {
      e.printStackTrace();
    }
    try
    {
      fw.close();
    }
    catch ( IOException e )
    {
      e.printStackTrace();
    }
  }

  /**
   * Methode bestimmt abhängig von der aktuellen Dateigrösse ob in Datei 1 oder in
   * Datei 2 geschrieben werden soll. Vor dem Umschalten zwischen den Datei wird die
   * Datei in die nun geschrieben werden soll, vorher gelöscht.
   * @param datei_1 Datei 1
   * @param datei_2 Datei 2
   * @param maxGrösse max. Grösse der Datei in MB
   * @return Datei in die geschrieben werden soll
   */
  public String bestimmeAktuelleDatei (String datei_1, String datei_2, long maxGrösse)
  {
    // Prüfen welches die aktuelle Datei ist (Datei mit aktuellerm Zeitstempel)
    
    // Daten Datei 1
    
    File f1 = new File (datei_1);
    long time1 = f1.lastModified();
    long size1 = f1.length();
    
    m_datei1 = f1;
    
    // Daten Datei 2
    
    File f2 = new File (datei_2);
    long time2 = f2.lastModified();
    long size2 = f2.length();

    m_datei2 = f2;
    
    long maxGr = maxGrösse * 1048576 ;
    
    // Bestimmen der zu verwendenten Datei
    
    String aktuell = null;
    String andere  = null;
    
    long size;
    
    if (time1 > time2)
    {
      aktuell = datei_1;
      size    = size1;

      andere  = datei_2;
    }
    else
    {
      aktuell = datei_2;
      size    = size2;

      andere  = datei_1;
    }
     
    if (size < maxGr)
      return aktuell;    // weiterhin in die aktuelle Datei schreiben
    
    // Dateien umschalten
    
    FileWriter fw = null;
    try
    {
      fw = new FileWriter (andere);
    }
    catch ( IOException e )
    {
      e.printStackTrace();
    }
    
    try
    {
      fw.close ();
    }
    catch ( IOException e )
    {
      e.printStackTrace();
    }

    return andere;
  }

  /**
   * Methode liefert den Namen der aktuell verwendeten Datei
   * @return Name der aktuell verwendeten Datei
   */
  public File getAktDatei()
  {
    if (m_datei1 != null)
      if (m_datei1.getAbsolutePath().equals( m_aktDatei ))
        return m_datei1;

    if (m_datei2 != null)
      if (m_datei2.getAbsolutePath().equals( m_aktDatei ))
        return m_datei2;

    return null;
  }

  /**
   * @return liefert die Variable datei1 zurück
   */
  public File getDatei1()
  {
    return m_datei1;
  }

  /**
   * @return liefert die Variable datei2 zurück
   */
  public File getDatei2()
  {
    return m_datei2;
  }
}
