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

/**
 * Klasse zum Auswerten der Parameter die beim Aufruf eines Prozesses an die Java Virtuell Machine
 * übergeben werden.
 * @author Dambach Werke GmbH
 */
public class JVMParameterAnalyse
{
  /**
   * Anzahl Bytes in einem KB
   */
  private static long _KB = 1024;
  
  /**
   * Anzahl Bytes in einem MB
   */
  private static long _MB = 1024 * _KB;
  
  /**
   * Anzahl Bytes in einem GB
   */
  private static long _GB = 1024 * _MB;
  
  /**
   * Anzahl Bytes in einem TB
   */
  private static long _TB = 1024 * _GB;

  /**
   * Aufrufparameter
   */
  private String m_aufruf = null;

  /**
   * Konstruktor der Klasse
   * @param aufruf Aufruf der JVM
   */
  public JVMParameterAnalyse (String aufruf)
  {
    m_aufruf = aufruf;   
  }
  
  /**
   * Methode wertet den Parameter -Xmx im Aufruf einer JVM aus.
   * -Xmx maximale Heapgröße (Virtuell+Reserviert), bsp. -Xmx1024m auf 1024 MB 
   * @return max. Heapgröße in Byte
   */
  public long getXmx ()
  {
    return analysiereSpeicherParameter( "-Xmx" );    
  }

  /**
   * Methode wertet den Parameter -Xms im Aufruf einer JVM aus.
   * -Xms Mindest-Heapgröße (immer fest Reserviert) 
   * @return Mindest-Heapgröße in Byte
   */
  public long getXms ()
  {
    return analysiereSpeicherParameter( "-Xms" );    
  }

  /**
   * Methode wertet den Parameter -Xmn im Aufruf einer JVM aus.
   * -Xmn Speicher beim Start
   * @return Xmn Wert
   */
  public long getXmn()
  {
    return analysiereSpeicherParameter( "-Xmn" );    
  }

  /**
   * Methode sucht den Speicherparameter der im Parameter kennung übergeben wurde und liefert
   * die dazugehörenden Grösse in Bytes zurück.
   * @param kennung Kennung des Parameter (z.B. -Xms)
   * @return Wert des Parameters in Bytes
   */
  private long analysiereSpeicherParameter (String kennung)
  {
    long groesse = 0L;
    String s = null;
    
    try
    {
      s = m_aufruf.substring (m_aufruf.indexOf (kennung) );
    }
    catch (IndexOutOfBoundsException e)
    {
      s = null;
    }
   
    if (s != null)
    {
      String z = Tools.bestimmeErsteZahl( s );
      
      if (z != null)
      {
        long wert = 0l;
        
        try
        {
          wert = Long.parseLong( z );
        }
        catch (NumberFormatException e)
        {
          wert = 0l;
        }
  
        // Einheit bestimmen, hierzu wird der String ab der Zahl z bestimmt, die Ziffern in
        // diesem String entfern und die führenden Leerzeichen entfernt.

        String z1 = s.substring (s.indexOf (z)).replaceAll ("[0-9]", "").trim();
        String einheit = " ";
        
        if (!z1.equals( "" ))
          einheit = "" + z1.charAt( 0 );

        // ohne Einheit 
        
        if (einheit.toLowerCase().equals( " " ))
          groesse = wert;
        
        // Kilobyte
        
        if (einheit.toLowerCase().equals( "k" ))
          groesse = wert * _KB;
  
        // Megabyte
        
        if (einheit.toLowerCase().equals( "m" ))
          groesse = wert * _MB;
        
        // Gigabyte
        
        if (einheit.toLowerCase().equals( "g" ))
          groesse = wert * _GB;
      }
    }

    return groesse;
  }
}
