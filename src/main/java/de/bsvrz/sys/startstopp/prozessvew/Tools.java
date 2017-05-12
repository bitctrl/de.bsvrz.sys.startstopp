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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasse die allgemein verwendbare Tools enthält
 * @author Dambach Werke GmbH
 *
 */
public class Tools
{
  /**
   * Methode sucht die erste Zahl in einem String. Als Zahl wird die erste Zahlenkombination verwendet, 
   * die in dem String vorkommt (z.B. "abc 123.456" wird in "123" umgewandelt).
   * @param s umzuwandelnder String
   * @return String mit der Zahl wenn eine Zahl im String vorkommt, sonst null
   */
  public static String bestimmeErsteZahl (String s)
  {
    if (s == null)
      return null;
    
    // Alle Zeichen die keine Ziffer sind durch ein Leerzeichen ersetzen
    
    String z1 = s.replaceAll ("[^0-9]", " ");
    
    // Mehrere Leerzeichen durch ein Leerzeichen ersetzen und dann die
    // Leerzeichen am Anfang und Ende entfernen
    
    String z2 = z1.replaceAll ( " +", " " ).trim();
    
    // Diesen String in seine einzelnen Komponenten aufsplitten
    
    String z3[] = z2.split( " " );
    
    // Sonderfall s = ""
    
    if (z3.length == 0)
      return null;
    
    // Sonderfall keine Ziffer gefunden
    
    if (z3[0].equals( "" ))
      return null;
    
    return z3[0];
  }
  
  /**
   * Methode zum Feststellen, ob es sich um ein Windows System handelt 
   * @return bei einem Windows System true, sonst false
   */
  public static boolean isWindows ()
  {
    return  System.getProperty("os.name").toLowerCase().contains( "windows" );
  }

  /**
   * Methode zum Feststellen, ob es sich um ein Unix System handelt 
   * @return bei einem Unix System true, sonst false
   */
  public static boolean isUnix ()
  {
    return  System.getProperty("os.name").toLowerCase().contains( "unix" );
  }

  /**
   * Methode zum Feststellen, ob es sich um ein Linux System handelt 
   * @return bei einem Linux System true, sonst false
   */
  public static boolean isLinux ()
  {
    return  System.getProperty("os.name").toLowerCase().contains( "linux" );
  }
  
  /**
   * Methode zum Feststellen, ob es sich um ein Mac System handelt 
   * @return bei einem Mac System true, sonst false
   */
  public static boolean isMac ()
  {
    return  System.getProperty("os.name").toLowerCase().contains( "mac" );
  }
  
  /**
   * Methode zum Einlesen einer kompletten Datei in eine Liste von Strings 
   * @param dateiName Name der einzulesenden Datei
   * @return Der Inhalt der Datei als Liste von Strings, im Fehlerfall leere Liste
   */
  public static List<String> leseDateiInString (String dateiName) 
  {
    File datei = new File (dateiName);
    
    List<String> l = new ArrayList<String>();
    
    FileReader fr = null;
    String zeile = null;
    
    try
    {
      fr = new FileReader(datei);

      BufferedReader br= new BufferedReader(fr);

      do
      {
        zeile = br.readLine();        
        if (zeile != null) 
          l.add( zeile );
      }
      while (zeile != null);
      
      br.close();
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    
    return l;
  }
  
  /**
   * Methode zum Ausführen eines Systembefehls. Die Methode nimmt dabei die Ausgabe
   * des Prozesses entgegen und gibt die Ausgaben des Befehls als Liste von Strings zurück. 
   * Die Methode wartet bis der Systembefehl ausgeführt wurde.
   * @param befehl Systembefehl
   * @return Standardausgaben des Befehls als Liste, im Fehlerfall eine leere Liste.
   */
  public static synchronized List<String> ausführen (String befehl)
  {
    List<String> ausgabe = new ArrayList<String>();
    
    // Systembefehl ausführen
    
    try
    {
      // Prozess starten
      
//      System.out.println("---------------------------------------------");
//      System.out.println(befehl);
//      System.out.println("---------------------------------------------");
      
      Process p = Runtime.getRuntime().exec( befehl );
      
      BufferedReader in = new BufferedReader (new InputStreamReader(p.getInputStream()) );

      String text = "";
      while ((text = in.readLine()) != null) 
      {
//        System.out.println(text);
        ausgabe.add( text );
      }
      
      // warten auf Ende des Prozess
      
      p.waitFor();
      
      in.close();   // MO 27.08.12 Bug 31
      p.destroy();  // MO 27.08.12
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  
    return ausgabe;
  }

  /**
   * Methode zum Bestimmen eines einheitlichen Namens für eine Inkarnation bestehend aus Name der
   * Inkarnation wie er in der startStopp Datei definiert wurde und der Simulationsvariante.
   * Der Name setzt sich wie folgt zusammen:<br>
   * Simulationsvariante = 0: Name der übergeben wurde wurde<br>
   * Simulationsvariante > 0: Name der übergeben wurde wurde plus Kennung "_SV_" plus Simulationsvariante<br>
   * Sonderfälle:<br>
   * Name = "": Returnwert = ""<br>
   * Name = null: Returnwert = null<br>
   * Zusätzlich werden Leerzeichen aus dem Original Inkarnationsnamen entfernt (der hier gebildete Name ist
   * Teil des Inkarnationsnamens, der als Parameter an die Applikation übergeben wird. Leerzeichen in diesem
   * Inkarnationsnamen würde die Applikation als neuen Parameter interpretieren).
   * @param name Originalname der Inkarnation aus der startStopp Datei
   * @param simulationsVariante aktuell eingestellte Simulationsvariante
   * @return Inkarnationsname
   */
  public static String bestimmeInkarnationsName (String name, long simulationsVariante)
  {
    if (name == null)
      return null;
    
    if (name.equals( "" ))
      return "";
    
    String nameOhneLeerzeichen = name.replaceAll ( " +", "" );
    
    if (simulationsVariante != 0)
      return nameOhneLeerzeichen + "_SV_" + simulationsVariante;
    else
      return nameOhneLeerzeichen;
  }
}
