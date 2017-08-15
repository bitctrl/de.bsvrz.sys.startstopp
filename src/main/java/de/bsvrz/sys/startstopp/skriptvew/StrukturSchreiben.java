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
import java.io.PrintStream;
import java.net.URL;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.EscapeStrategy;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Zuständig für das Schreiben der XML-Struktur
 * @author HofmannJ
 *
 */
public class StrukturSchreiben 
{
  /**
   * Konstruktor der Klasse
   * @param root Wurzelelement der XML-Struktur das in eine Datei geschrieben werden soll
   * @param datei
   */
	public StrukturSchreiben (int art, Element root, String datei)
  {		
    schreibeXML (art, root, datei);
    
    verschoenereXML( datei, datei);
	}
	
	/**
	 * Schreibt die Daten aus einem XML-Element (JDOM) in eine Datei.
	 * Sind in dieser Datei bereits startStop-Elemente vorhanden, dann
	 * wird eine Versionierung vorgenommen
	 * @param em Element nach JDOM, ElementstartStop
	 * @param sDateiName Dateiname bzw. kompletter Pfad zur Datei
	 */
	public void schreibeXML (int art, Element em, String sDateiName)
	{
		Document doc = new Document();
		doc.setRootElement(em);
		
		DocType doctype = null; 
      
    if (art == 0)
      doctype = new DocType("konfiguration", "-//startstopp//DTD Dokument//DE", "startStopp.dtd");
    
    if (art == 1)
      doctype = new DocType("history", "-//startstopp//DTD Dokument//DE", "startStoppHistory.dtd");
    
		doc.setDocType(doctype);

		try
		{
			XMLOutputter out = new XMLOutputter();
			Format f = out.getFormat();
			f.setEncoding("ISO-8859-1");
      f.setEscapeStrategy(new Umlaute());
			out.setFormat(f);
			PrintStream ps = new PrintStream(sDateiName);
			out.output(doc, ps);
			ps.close();

		} catch (java.io.IOException exc)
		{
			exc.printStackTrace();
		}
	}

	/**
	 * Verschönert die Ausgabe einer Datei von JDOM
	 * @param sAlteDatei Kompletter Pfad zur alten Datei
	 * @param sNeueDatei Kompletter Pfad zur neuen Datei
	 */
	public void verschoenereXML (String sAlteDatei, String sNeueDatei)
	{
		boolean validate = false; 
    
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

		Document doc;

		try
		{
			doc = builder.build(new File(sAlteDatei));
			Format prettyFormat = Format.getPrettyFormat();
			XMLOutputter out = new XMLOutputter(prettyFormat);
			Format f = out.getFormat();
			f.setEncoding("ISO-8859-1");
			f.setEscapeStrategy(new Umlaute());
			out.setFormat(f);
			PrintStream ps = new PrintStream(sNeueDatei);
			out.output(doc, ps);
			ps.close();
		} 
    catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}
	
	/**
	 * Klasse für die Umsetzung der hier notwendigen Escape-Strategie. 
	 */
	class Umlaute implements EscapeStrategy
	{
    /* (non-Javadoc)
     * @see org.jdom.output.EscapeStrategy#shouldEscape(char)
     */
    public boolean shouldEscape(char zeichen)
    {
      if ((zeichen == 'Ä') ||
          (zeichen == 'Ö') ||
          (zeichen == 'Ü') ||
          (zeichen == 'ä') ||
          (zeichen == 'ö') ||
          (zeichen == 'ü') ||
          (zeichen == 'ß'))
      {
        return true;
      }
      
      return false;
    }
	}
}
