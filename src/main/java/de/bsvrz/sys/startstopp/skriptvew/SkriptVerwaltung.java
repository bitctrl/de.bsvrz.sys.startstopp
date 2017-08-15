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

/**
 * 
 */
package de.bsvrz.sys.startstopp.skriptvew;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.bsvrz.sys.startstopp.prozessvew.ProzessVerwaltung;

/**
 * Klasse zum Auswerten einer StartStopp Datei. Bevor die Datei ausgewertet wird, werden die Makros in der Datei
 * ersetzt. Dabei fungiert die Klasse wie ein Precompiler.<br> 
 * Beispiel:
 * <makrodefinition name="java" wert="C:/Programme/Java/jdk1.6.0_03/bin/java" />
 * bewirkt, das an allen nachfolgenden Stellen in der Datei an denen der Makroverweis %java%
 * verwendet wird, der Text java" wert="C:/Programme/Java/jdk1.6.0_03/bin/java verwendet wird.
 * @author Dambach Werke GmbH
 */

public class SkriptVerwaltung extends SkriptVewEnums
{
  /**
   * Verweis auf Singleton Instanz der Klasse GlobaleDaten
   */
  private static GlobaleDaten m_globaleDaten = null;
  
  /**
   * Name der XML-Datei die interpretiert werden soll
   */
	private String m_xmlDatei = "";
  
  /**
   * ID des StartStopp Blocks der zu dieser Datei gehört
   */
  private String m_startStoppBlockId = "";
  
  /**
   * Hashmap mit den Makros die in diesem StartStopp Block definiert sind
   * 
   */
  private HashMap<String, String> m_makros = new HashMap<String, String> (); 
  
  /**
   * Merker ob in der Datei startStopp.xml ein semantischer Fehler vorliegt 
   */
  
  private boolean m_fehlerStartStopp = false;

  /**
   * Beschreibung des Fehlers in der XML Datei
   */
  private String m_fehlerText;
  
  /**
   * Klasse zum Auswerten eines StartStopp Blocks
   * @param datei Datei mit dem StartStopp Block
   * @param absender Auslöser des Einlesen: 0 - StartStopp selbst,
   * -1 - Handeingriff (Telnet), > 0 - ApplikationsID der Applikation
   * @param simulationsVariante Simulationsvariante
   */
  public SkriptVerwaltung (String datei, long absender, long simulationsVariante)
  {
    m_globaleDaten = GlobaleDaten.getInstanz();
    
    interpretation (datei, absender, simulationsVariante);
  }

  /**
   * Methode zum Auswerten einer StartStopp Datei
   * @param datei Datei mit dem StartStopp Block
   * @param absender Auslöser des Einlesen: 0 - StartStopp selbst,
   * -1 - Handeingriff (Telnet), > 0 - ApplikationsID der Applikation
   * @param simulationsVariante Simulationsvariante
   */
  private void interpretation (String datei, long absender, long simulationsVariante)
  {
    m_fehlerStartStopp = false;
    
    ProzessVerwaltung pv= ProzessVerwaltung.getInstanz();
    
    StartStoppVerwaltung sbv = StartStoppVerwaltung.getInstanz();
    
    //Merken der XML datei
    
    m_xmlDatei = datei;
    
    Element root = XMLTools.leseRootElement( datei );
    if (root == null)
    {
      m_fehlerStartStopp = true;
      m_fehlerText = XMLTools.getFehlerText();
      
      return;
    }

    //Erzeugen der Globalen Daten für die Speicherung des Kernsystems 
    //und Info die für alle Prozesse gelten
    
    GlobaleDaten globaleDaten = GlobaleDaten.getInstanz();
    
    //Auslesen der xml Datei und füllen der Prozessdaten
    //erfolgreiches auslesen der Datei wird mit der Rückmeldung True beantwortet
    
    // Makrosersetzen

    m_makros = bestimmeMakros (datei);

    String tmpDatei = ersetzeMakros (m_xmlDatei);
    
    // modifizierte Datei wieder in eine XML-Struktur einlesen
    
    Element rootModifiziert = XMLTools.leseRootElement( tmpDatei );
    
    // StartStoppBlock anlegen
    
    StartStoppBlock ssb = sbv.addStartStoppBlock (root, rootModifiziert);
    
    // ID merken
    
    m_startStoppBlockId = ssb.getStartStoppBlockId();

    // Globale Daten aus der XML-Datei auslesen
    
    globaleDaten.initialisiereGlobaleDaten( m_startStoppBlockId, rootModifiziert, absender, simulationsVariante );
   
    Iterator it1 = m_makros.entrySet().iterator();
    
    while (it1.hasNext())
    {
      Map.Entry me1 = (Map.Entry)it1.next();
      
      String key = (String)me1.getKey();
      String value = (String)me1.getValue();
      
      globaleDaten.addMakroGlobal(m_startStoppBlockId, key, StartStoppApp.urlUmsetzen(value));
    }
    
    // Prozessdaten aus der XML-Datei auslesen
    
    pv.initialisiereInkarnationen( rootModifiziert, absender, m_startStoppBlockId, simulationsVariante );
    
    if (!pv.isInkarntionsTeilPlausibel())
      m_fehlerStartStopp = true;
  }

  /**
   * Methode zum Einlesen einer kompletten Datei in einen String 
   * @param datei Die einzulesenden Datei.
   * @throws java.io.FileNotFoundException Die Datei wurde nicht gefunden.
   * @throws java.io.IOException Es gab eine Input-Output Fehler.
   * @return Der Inhalt der Datei.
   */
  public static String leseDateiInString (File datei) throws FileNotFoundException,IOException 
  {
    StringBuffer buffer = new StringBuffer("");
    FileReader fr = new FileReader(datei);
    BufferedReader br= new BufferedReader(fr);
    
    String zeile = br.readLine();        
    while(zeile != null) 
    {
      buffer.append(zeile);
      buffer.append("\n");
      zeile = br.readLine();
    }
    
    br.close();
    
    return buffer.toString();
  }
 
  /**
   * Schreibt einen String in eine Datei.
   * @param datei Die Datei, in der hinein geschreiben werden soll.
   * @param inhalt Der zu schreibende Inhalt.
   * @throws java.io.IOException Input-Output-Fehler.
   */
  public static void schreibeStringInDatei(File datei, String inhalt) throws IOException 
  {        
    FileWriter fw = new FileWriter(datei);
    BufferedWriter bw = new BufferedWriter(fw);
    bw.write(inhalt);
    bw.close();
  }
     
  /**
   * Methode bestimmt die eingestellten Makros im globalen Teil der StartStopp Datei
   * @param datei Name der StartStopp Datei 
   * @return Hahmap mit den Makros, als Key wird der Makroname, als Wert die Makrodefinition
   * eingetragen
   */
  public static HashMap<String, String> bestimmeMakros (String datei) 
  {
    boolean validate = true;
    
    HashMap<String, String> makros = new HashMap<String, String>();
    
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
      e.printStackTrace();
    }
    catch ( IOException e )
    {
      e.printStackTrace();
    }
    
    //Ausgehend vom Root-Element wird zu den gewünschten Elementen im Baum durchgegangen.
    
    Element rootElement = jdomDokument.getRootElement();  
    
    //Holen von startStopp Element
    
    Element startStoppElement = null;
    
    startStoppElement = rootElement.getChild ( "startStopp" );
    
    //Holen von global
    
    Element globalElement = startStoppElement.getChild( "global" );
    
    ListIterator<Element> listeKinderGlobal = globalElement.getChildren().listIterator();
    
    //----------------------------------------------------------------------
    // Diese While schleife durchläuft die einzelnen Subelemente
    //----------------------------------------------------------------------
    
    while (listeKinderGlobal.hasNext())
    {
      Element child = listeKinderGlobal.next();
      
      if (child.getName() == "makrodefinition")
      {
        String key   = child.getAttribute( "name" ).getValue();
        String value = child.getAttribute( "wert" ).getValue();
        
        makros.put( key, StartStoppApp.urlUmsetzen(value) );
      }
    }
    
    return makros;
  }

  /**
   * Methode zum Ersetzen der Makros in einer Datei. Methode fungiert dabei als Precompiler. 
   * Die Makros müssen vorher über die Methode bestimmeMakros bestimmt worden sein.
   * Beispiel:
   * <makrodefinition name="java" wert="C:/Programme/Java/jdk1.6.0_03/bin/java" />
   * bewirkt, das an allen nachfolgenden Stellen in der Datei an denen der Makroverweis %java%
   * verwendet wird, der Text java" wert="C:/Programme/Java/jdk1.6.0_03/bin/java
   * eingetragen wird. Die modifizierter Daten werden in einer temporären Datei abgespeichert.
   * @param datei Name der ursprünglichen Datei
   * @return Name der modifizierten Datei oder null im Fehlerfall
   */
  private String ersetzeMakros( String datei )
  {
    String neueDatei = null;
    
    // XML Datei als String einlesen
   
    File f = new File (datei);
    String s = null;
    
    try
    {
      s = leseDateiInString (f);
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
      s = null;
    }
    catch (IOException e)
    {
      e.printStackTrace();
      s = null;
    }
    
    if (s != null)
    {
      // Makros in den Makros ersetzen
      
      Iterator it1 = m_makros.entrySet().iterator();
      
      while (it1.hasNext())
      {
        Map.Entry me1 = (Map.Entry)it1.next();
        
        String key = (String)me1.getKey();
        String value = (String)me1.getValue();
        
        Iterator it2 = m_makros.entrySet().iterator();
        
        while (it2.hasNext())
        {
          Map.Entry me2 = (Map.Entry)it2.next();
          
          String key2 = (String)me2.getKey();
          String value2 = (String)me2.getValue();
          
          if (!key2.equals( key ))
          {
            String v2 = value2.replace( "%" + key + "%", value );
            m_makros.put( key2, v2 );
          }
        }
      }
      
      // Makros in Datei ersetzen
      
      Iterator it = m_makros.entrySet().iterator();
      
      String s1 = null;
      
      s1 = s;
      
      while (it.hasNext())
      {
        Map.Entry me = (Map.Entry)it.next();
        
        String key = (String)me.getKey();
        String value = (String)me.getValue();
        
        String k1 = "%" + key + "%";

//          System.out.println(k1 + " --> " + value);

        value = value.replace("\"", "&quot;"); // Anführungszeichen durch &quot; ersetzen, damit XML-Datei plausibel bleibt

        s1 = s1.replace( k1 , value );
      }

      // Umlaute durch Sequenzen ersetzen, damit XML-Datei plausibel bleibt
      
      s1 = s1.replace("Ä", "&#196;");
      s1 = s1.replace("Ö", "&#214;");
      s1 = s1.replace("Ü", "&#220;");
      s1 = s1.replace("ä", "&#228;");
      s1 = s1.replace("ö", "&#246;");
      s1 = s1.replace("ü", "&#252;");
      s1 = s1.replace("ß", "&#223;");
      
      neueDatei = datei + "_tmp";
      File f1 = new File (neueDatei);
      
      try
      {
        schreibeStringInDatei (f1, s1);
      }
      catch (IOException e)
      {
        neueDatei = null;
        e.printStackTrace();
      }
      
    } // if (s != null)
    
    return neueDatei;
  }

  /**
   * @return liefert die Klassenvariable m_startStoppBlockId zurück
   */
  public String getStartStoppBlockId()
  {
    return m_startStoppBlockId;
  }

  /**
   * @return liefert die Klassenvariable m_fehlerStartStopp zurück
   */
  public boolean isFehlerStartStopp()
  {
    return m_fehlerStartStopp;
  }

  /**
   * @return liefert die Variable fehlerText zurück
   */
  public String getFehlerText()
  {
    return m_fehlerText;
  }
}
