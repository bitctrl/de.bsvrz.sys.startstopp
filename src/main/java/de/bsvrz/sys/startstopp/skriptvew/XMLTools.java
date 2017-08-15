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
import java.io.IOException;
import java.net.URL;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.bsvrz.sys.startstopp.prozessvew.Logbuch;

/**
 * Klasse stellt allgemeine Methoden zur Verfügung für die Bearbeitung der XML-Dateien
 * @author Dambach Werke GmbH
 */

public class XMLTools
{
  /**
   * Text für aktuelle Fehlermeldung
   */
  
  private static String m_fehlerText = null;
  
  /**
   * Konstruktor der Klasse
   */
  
  public XMLTools ()
  {
    m_fehlerText = null;
  }
  
  /**
   * Methode liest das Rootelement aus einer XML-Datei aus. Vorher wird
   * geprüft, ob die Datei überhaupt existiert und ob sie der DTD Beschreibung
   * entspricht.
   * @param datei Name der Datei die in eine XML-Struktur eingelsen werden soll
   * @return Element RootElement der XML-Struktur oder null im Fehlerfall
   */
  public static Element leseRootElement (String datei)
  {
    // Prüfen ob Datei überhaupt existiert
    
    if (!(new File (datei)).exists())
    {
      merkeFehler ("Datei \"" + datei + "\" nicht gefunden !");
      
      return null;
    }

    // Einlesen der Datei in eine XML-Struktur
    
    boolean validate = true;  //Validierung gegen die jeweilige DTD
    
    String driverClass = "org.apache.xerces.parsers.SAXParser";
    
    SAXBuilder builder = new SAXBuilder(driverClass, validate);
    builder.setEntityResolver(new EntityResolver() 
    {
      public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
      {
        if ((systemId != null) && (systemId.endsWith("/startStopp.dtd"))) 
        {
          URL url = this.getClass().getResource("startStopp.dtd");
          assert url != null : this.getClass();
          
          return new InputSource(url.toExternalForm());
        }

        if ((systemId != null) && (systemId.endsWith("/startStoppHistory.dtd"))) 
        {
          URL url = this.getClass().getResource("startStoppHistory.dtd");
          assert url != null : this.getClass();
          
          return new InputSource(url.toExternalForm());
        }

        return null;
      }
    });
  
    Document jdomDokument = null;
    
    try
    {
      jdomDokument = builder.build (datei);
    }
    catch ( JDOMException e )
    {
//      e.printStackTrace();
//      System.out.println(e.getMessage());
      merkeFehler (e.getMessage());
      return null;
    }
    catch ( IOException e )
    {
//      e.printStackTrace();
//      System.out.println(e.getMessage());
      merkeFehler (e.getMessage());
      return null;
    }
    
    // Rootelement aus eingelesener Datei bestimmen
    
    Element rootElement = jdomDokument.getRootElement(); 
    
    rootElement.detach();  // Wichtig: nach dem Einlesen aus der Datei muss am Knoten "konfiguration" der
                           // Verweis auf den Parent gelöscht werden. Sonst Fehlermeldung beim Versuch 
                           // den Schreibens des Parents in der Methode "schreibeXML".

    return rootElement;
  }

  /**
   * Methode zum Publizieren des Fehlers
   * @param fehler Fehlermeldung
   */
  private static void merkeFehler (String fehler)
  {
    m_fehlerText = fehler;
    
//    System.err.println(fehler);
    
    Logbuch.getInstanz().schreibe( fehler );
  }

  /**
   * @return liefert die Klassenvariable m_fehlerText zurück
   */
  public static String getFehlerText()
  {
    return m_fehlerText;
  }
  
  /**
   * Methode prüft ob ein Fehler in der XML-Datei vorleigt
   */
  public boolean isFehler ()
  {
   return m_fehlerText != null; 
  }
}
