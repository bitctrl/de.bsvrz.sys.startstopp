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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Klasse die Methoden zur Verfügung stellt die es ermöglichen den freien Hauptspeicher 
 * zu ermitteln. Da Java von Natur aus keine Möglichkeit hat diese Informationen auszulesen
 * muss der "Umweg" über entsprechende Betriebssystem Aufrufe genommen werden. 
 * Das Bestimmen des freien Speichers wird je nach Betriebssystem unterschiedlich 
 * gehandhabt:<br>
 * - UNIX/Linux: Aufruf des Befehls "vmstat" und auswerten der Spalte "free"<br>
 * - Windows: derzeit nicht implementiert
 * - Mac: derzeit nicht implementiert
 * @author Dambach Werke GmbH
 */
public class Memory
{
  /**
   * Debug
   */
  private static final Debug logger = Debug.getLogger();

  /**
   * Parameterloser Konstruktor der Klasse
   */
  public Memory()
  {
  }

  /**
   * Methode liefert den freien Speicher zurück
   * @return freier Speicher in Bytes, im Fehlerfall -1
   */
  public long getFreierSpeicher ()
  {
    long frei = -1;
    
    if (Tools.isUnix())
    {
      String befehl = "vmstat ";
    
      List<String> ausgabe = Tools.ausführen (befehl);
    
      if (ausgabe.size() > 0)
        frei = auswerteVmstatUnix( ausgabe );
    }

    if (Tools.isLinux())
    {
      String befehl = "vmstat -S k";
    
      List<String> ausgabe = Tools.ausführen (befehl);
    
      if (ausgabe.size() > 0)
        frei = auswerteVmstatLinux( ausgabe );
    }

    if (Tools.isWindows())
    {
  		try
  		{
  			Process process = Runtime.getRuntime().exec("typeperf \"\\Speicher\\Verfügbare KB\" -sc 1");
  			InputStream is = process.getInputStream();
  			
  			BufferedInputStream bis = new BufferedInputStream(is);

  			byte[] gesamt = new byte[500];
  			
  			int j = 0;
  			
  			while (true)
  			{
  				int warten = 0;
  				
  				while (bis.available() == 0)
  				{
  					Thread.sleep(100);
  					
  					warten++;
  					
  					if (warten >= 20) // maximal 2 Sekunden warten
  					{
  						return frei;
  					}
  				}
  				
  				byte[] wert = new byte[1000];
  				
  				int anz = is.read(wert, 0, 1000);
  				
  				for (int i = 0; i < anz; i++)
  				{
  					if (wert[i] == '"')
  					{
  						continue;
  					}
  						
  					if (wert[i] > 14)
  					{
  						gesamt[j] = wert[i];
  						
  						j++;
  					}

  					if (wert[i] == 10)
  					{
  						gesamt[j] = 0;
  						
  						String zeile = new String(gesamt, 0, j);

  						String[] zeileTeil = zeile.split(",");
  						
  						if (zeileTeil.length > 1)
  						{
  							float f1;

  							try
  							{
  								f1 = Float.parseFloat(zeileTeil[1]);

  								float f2 = f1 * 1024;
  								
  								frei = (long)f2;
  								
  								return frei;
  							}
  							catch (NumberFormatException e)
  							{
  							}
  						}
  						
  						j = 0;
  					}
  				}
  			}
  		}
  		catch (IOException e)
  		{
//  			e.printStackTrace();
  		}
  		catch (InterruptedException e)
  		{
//  			e.printStackTrace();
  		}
    }

    return frei;  
  }
  
  /**
   * Methode wertet die Ausgaben des Unix Aufrufs "vmstat" aus und bestimmt daraus den aktuellen 
   * freien Speicher. Hierzu wird die Spalte "free" der Ausgabe ausgewertet. Berücksichtigt
   * wird dabei ob es sich bei der Ausgabe um einen Wert in Pages (Unix) handelt.
   * @param ausgabeVmstat Ausgabe eines Unix Aufrufs "vmstat"
   * @return Anzahl freier Bytes, im Fehlerfall -1
   */
  private long auswerteVmstatUnix( List<String> ausgabeVmstat )
  {
    long frei = -1;
    
    long pagesize  =  1;
    int  indexFree = -1;
    
    for (int i=0; i<ausgabeVmstat.size(); ++i)
    {
      String zeile = ausgabeVmstat.get( i ).toLowerCase();
      
      // Prüfen ob Zeile mit dem Text "pagesize = " vorkommt
      
      if (zeile.contains( "pagesize = " ))
      {
        String ps = Tools.bestimmeErsteZahl (zeile);
        
        if (ps != null)
          pagesize = myLong( ps );
      }
      
      // Prüfen ob in der Zeile der Text "free" vorkommt, wenn ja dann wird
      // diese Zeile benutzt um die Spalte zu bestimmen in der der free-Wert
      // steht.
      
      if (zeile.contains( "free" ))
      {
        // Mehrere Leerzeichen durch ein Leerzeichen ersetzen und dann die
        // Leerzeichen am Anfang und Ende entfernen
        
        String z1 = zeile.replaceAll ( " +", " " ).trim();

        // Aufsplitten der einzelnen Spalten
        
        String s1[] = z1.split( " " );
        
        int counter = 0;

        // Spalten auswerten und den Text "free" suchen
        
        for (int ii=0; (ii<s1.length) && (indexFree == -1); ++ii)
        {
          counter++;
          if (s1[ii].equals( "free" ))
            indexFree = ii;
        }
      }
      else
      {
        // Zeile mit Zahlenwerten auswerten
        
        if (indexFree != -1)
        {
          // Mehrere Leerzeichen durch ein Leerzeichen entfernen und dann die
          // Leerzeichen am Anfang und Ende entfernen
          
          String z1 = zeile.replaceAll ( " +", " " ).trim();
          
          // Aufsplitten der einzelnen Spalten

          String s1[] = z1.split( " " );
          
          // Zahl bestimmen, hierzu werden alle Zeichen die keine Ziffern sind (Einheiten) aus dem
          // String entfernt.
          
          String z2 = s1[indexFree].replaceAll ("[^0-9]", "");
          
          long wert = myLong( z2 );
          
          // Einheit bestimmen, hierzu werden die Ziffern aus dem String entfernt
          
          String einheit = s1[indexFree].replaceAll ("[0-9]", "");

          // Umrechnen in freie Bytes
          
          frei = wert;
          
          if (einheit.toLowerCase().equals( "k" ))
            frei = wert * 1024;

          if (einheit.toLowerCase().equals( "m" ))
            frei = wert * 1024 * 1024;
          
          frei *= pagesize;
          
          logger.fine("Freier Speicher: " + wert + einheit + " (Pagesize " + pagesize + ") --> " + frei + " Bytes");
        }
      }
    }
    
    return frei;
  }

  /**
   * Methode wertet die Ausgaben des Linux Aufrufs "vmstat" aus und bestimmt daraus den aktuellen 
   * freien Speicher. Hierzu wird die Spalte "free" der Ausgabe ausgewertet. Berücksichtigt
   * wird dabei ob es sich bei der Ausgabe um einen Wert in kB (Linux) handelt.
   * @param ausgabeVmstat Ausgabe eines Linux Aufrufs "vmstat"
   * @return Anzahl freier Bytes, im Fehlerfall -1
   */
  private long auswerteVmstatLinux( List<String> ausgabeVmstat )
  {
    long frei = -1;
    
    long faktor    = 1024; // 1kB = 1024 Bytes
    int  indexFree = -1;
    
    for (int i=0; i<ausgabeVmstat.size(); ++i)
    {
      String zeile = ausgabeVmstat.get( i ).toLowerCase();
      
      // Prüfen ob in der Zeile der Text "free" vorkommt, wenn ja dann wird
      // diese Zeile benutzt um die Spalte zu bestimmen in der der free-Wert
      // steht.
      
      if (zeile.contains( "free" ))
      {
        // Mehrere Leerzeichen durch ein Leerzeichen ersetzen und dann die
        // Leerzeichen am Anfang und Ende entfernen
        
        String z1 = zeile.replaceAll ( " +", " " ).trim();

        // Aufsplitten der einzelnen Spalten
        
        String s1[] = z1.split( " " );
        
        int counter = 0;

        // Spalten auswerten und den Text "free" suchen
        
        for (int ii=0; (ii<s1.length) && (indexFree == -1); ++ii)
        {
          counter++;
          if (s1[ii].equals( "free" ))
            indexFree = ii;
        }
      }
      else
      {
        // Zeile mit Zahlenwerten auswerten
        
        if (indexFree != -1)
        {
          // Mehrere Leerzeichen durch ein Leerzeichen entfernen und dann die
          // Leerzeichen am Anfang und Ende entfernen
          
          String z1 = zeile.replaceAll ( " +", " " ).trim();
          
          // Aufsplitten der einzelnen Spalten

          String s1[] = z1.split( " " );
          
          // Zahl bestimmen, hierzu werden alle Zeichen die keine Ziffern sind (Einheiten) aus dem
          // String entfernt.
          
          String z2 = s1[indexFree].replaceAll ("[^0-9]", "");
          
          long wert = myLong( z2 );
          
          // Einheit bestimmen, hierzu werden die Ziffern aus dem String entfernt
          
          String einheit = s1[indexFree].replaceAll ("[0-9]", "");

          // Umrechnen in freie Bytes
          
          frei = wert;
          
          frei *= faktor;
          
          logger.fine("Freier Speicher: " + wert + " (Faktor " + faktor + ") --> " + frei + " Bytes");
        }
      }
    }
    
    return frei;
  }

  /**
   * Hilfsmethode zum Umwandeln eines Strings in einen Long-Wert
   * @param buffer
   * @return umgewandelter Long Wert
   */
  private long myLong (String buffer)
  {
    long wert = -1;

    try
    {
      wert = Long.parseLong( buffer );
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    
    return wert;
  }
}

