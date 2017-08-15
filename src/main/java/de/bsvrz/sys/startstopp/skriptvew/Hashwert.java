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

import java.io.FileInputStream;
import java.security.MessageDigest;

import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Klasse um Berechnen eine Hashwertes mit Hilfe der Algorithmen MD5 oder SHA 
 * über eine Datei
 * @author Dambach Werke GmbH
 *
 */
public class Hashwert
{
  /**
   * Debug
   */
  private static final Debug logger = Debug.getLogger();

  /**
   * Methode wandelt ein Byte in eine Hexadezimale Darstellung um
   * @param b Byte
   * @return hexadezimale Darastellung
   */
  public static String toHexString (byte b)
  {
    int value = (b & 0x7F) + (b < 0 ? 128 : 0);
    String ret = (value < 16 ? "0" : "");
    ret += Integer.toHexString(value).toUpperCase();
    return ret;
  }

  /**
   * Methode zum Berechnen eine Hashwertes mit Hilfe der Algorithmen MD5 oder SHA 
   * über eine Datei
   * @param verfahren "MD5" oder "SHA"
   * @param datei Datei über die ein Hashwert gebildet werden soll
   * @return Hashwert hexadezimal als String
   */
  public static String berechneHashwert (String verfahren, String datei)
  {
    String hashwert = null;
    
    try 
    {
      // MessageDigest erstellen
      
      MessageDigest md = MessageDigest.getInstance(verfahren);
      
      FileInputStream in = new FileInputStream(datei);
      
      int len;
      
      byte[] data = new byte[1024];
      
      while ((len = in.read(data)) > 0) 
      {
        //MessageDigest updaten
        
        md.update(data, 0, len);
      }
      in.close();
      
      //MessageDigest berechnen und ausgeben
      
      byte[] result = md.digest();
      
      hashwert = "";
      
      for (int i = 0; i < result.length; ++i) 
      {
//        System.out.print(toHexString(result[i]) + " ");
        
        hashwert += toHexString(result[i]);
      }
      
//      System.out.println();
      
    } 
    catch (Exception e) 
    {
      logger.error(e.toString());
    }
    
//    System.out.println(hashwert);
    
    return hashwert;
  }
}
