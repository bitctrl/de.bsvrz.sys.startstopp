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

import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;


/**
 * Klasse zum Erzeugen einer XML-Struktur entsprechend der StartStoppHistory.dtd
 * @author Dambach Werke GmbH
 *
 */
public class StartStoppHistoryXML 
{
  /**
   * Wurzelelement (JDOM) mit der StartStopp XML Struktur 
   */
  private Element m_root = null;
  
  /**
   * Konstruktor der Klasse
   */
  public StartStoppHistoryXML ()
  {
    m_root = erzeugeHistory();  
  }

  /**
   * Methode zum Erzeugen des XML-Elements <history> der StartStoppHistory Datei
   * @return erzeugtes Element
   */
  public Element erzeugeHistory ()
  {
    Element em = new Element ("history");
    
    erzeugeStartStoppVersioniert (em);
    
    return em;
  }

  /**
   * Methode zum Erzeugen des XML-Elements <startStoppVersioniert> der StartStoppHistory Datei
   * @param root Rootelement der XML-Struktur
   */

  private void erzeugeStartStoppVersioniert (Element root)
  {
    List <HistoryData> h = StartStoppHistorie.getInstanz().getHistory();
    
    for (int i = 0; i < h.size(); ++i)
    {
      Element em = new Element ("startStoppVersioniert");
      
      Attribute a1 = new Attribute ("Versionsnummer",     h.get( i ).getVersion());
      Attribute a2 = new Attribute ("ErstelltAm",         h.get( i ).getErstelltAm());
      Attribute a3 = new Attribute ("ErstelltDurch",      h.get( i ).getErstelltDurch());
      Attribute a4 = new Attribute ("Aenderungsgrund",    h.get( i ).getAenderungsGrund());
      Attribute a5 = new Attribute ("Checksumme",         h.get( i ).getHashwert());
      
      em.setAttribute( a1 );
      em.setAttribute( a2 );
      em.setAttribute( a3 );
      em.setAttribute( a4 );
      em.setAttribute( a5 );
      
      root.addContent( em );
    }
  }

  /**
   * @return liefert die Klassenvariable m_root zurück
   */
  public Element getRoot()
  {
    return m_root;
  }
}
