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

import java.util.ArrayList;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;

import de.bsvrz.sys.startstopp.prozessvew.ProzessDaten;
import de.bsvrz.sys.startstopp.prozessvew.ProzessVerwaltung;
import de.bsvrz.sys.startstopp.prozessvew.StartBedingung;
import de.bsvrz.sys.startstopp.prozessvew.StoppBedingung;


/**
 * Klasse zum Erzeugen einer XML-Struktur entsprechend der StartStopp.dtd
 * @author Dambach Werke GmbH
 *
 */

public class StartStoppXML 
{
  private GlobaleDaten m_globaleDaten = null;

  /**
   * Wurzelelement (JDOM) mit der StartStopp XML Struktur 
   */
  private Element m_root = null;
  
  /**
   * Konstruktor der Klasse
   */
  public StartStoppXML ()
  {
    m_root = erzeugeKonfiguration();  
  }

  /**
   * Methode zum Erzeugen des XML-Elements <konfiguration> der StartStopp Datei
   * @return erzeugtes Element
   */
  public Element erzeugeKonfiguration ()
  {
    m_globaleDaten = GlobaleDaten.getInstanz();
    
    Element em = new Element ("konfiguration");
    
    Element startStopp = erzeugeStartStopp ();
    
    em.addContent( startStopp );
    
    
    return em;
  }

  /**
   * Methode zum Erzeugen des XML-Elements <startStopp> der StartStopp Datei
   * @return erzeugtes Element
   */

  private Element erzeugeStartStopp ()
  {
    Element em = new Element ("startStopp");
    
    Attribute a1 = new Attribute ("Versionsnummer",     "");
    Attribute a2 = new Attribute ("ErstelltAm",         "");
    Attribute a3 = new Attribute ("ErstelltDurch",      "");
    Attribute a4 = new Attribute ("Aenderungsgrund",    "");
    
    em.setAttribute( a1 );
    em.setAttribute( a2 );
    em.setAttribute( a3 );
    em.setAttribute( a4 );
    
    Element em1 = erzeugeElementGlobal ();
    Element em2 = erzeugeElementApplikationen ();
    
    em.addContent( em1 );
    em.addContent( em2 );
    
    return em;
  }

  /**
   * Methode erzeugt das Element <global> des StartStopp Blocks
   * @return Element <global>
   */
  private Element erzeugeElementGlobal()
  {
    Element root = new Element ("global");

    addMakros (root);

    addKernsystem ( root );
    
    addZugangDaV ( root );

    addUsv ( root );

    addRechner ( root );

    addProtokollDatei( root );

    return root;
  }

  /**
   * Methode um Eintragen der versorgten Makros in ein Element
   * @param root Element unter dem die Daten eingetragen werden
   */
  private void addMakros (Element root)
  {
    for (int i=0; i<m_globaleDaten.getMakroGlobal().size(); ++i)
    {
      Element em = new Element ("makrodefinition");
      
      Attribute a1 = new Attribute ("name", m_globaleDaten.getMakroGlobal().get( i ).getName());
      em.setAttribute( a1 );
      
      Attribute a2 = new Attribute ("wert", m_globaleDaten.getMakroGlobal().get( i ).getWert());
      em.setAttribute( a2 );
      
      root.addContent( em );
    }
  }
  
  /**
   * Methode um Eintragen der versorgten Kernsystem in ein Element
   * @param root Element unter dem die Daten eingetragen werden
   */
  private void addKernsystem (Element root)
  {
    for (int i=0; i<m_globaleDaten.getKernSystem().size(); ++i)
    {
      Element em = new Element ("kernsystem");
      
      Attribute a1 = new Attribute ("inkarnationsname", ersetzeDurchMakro( m_globaleDaten.getKernSystem().get( i ).getApplikation()));

      em.setAttribute( a1 );
      
      root.addContent( em );
    }
  }
  
  /**
   * Methode um Eintragen der Zugangsdaten DaV in ein Element
   * @param root Element unter dem die Daten eingetragen werden
   */
  private void addZugangDaV (Element root)
  {
    Element em = new Element ("zugangdav");
    
    Attribute a1 = new Attribute ("adresse",  ersetzeDurchMakro( m_globaleDaten.getDavAdresse()) );
    Attribute a2 = new Attribute ("port",     ersetzeDurchMakro( "" +  m_globaleDaten.getDavPort()));
    Attribute a3 = new Attribute ("username", ersetzeDurchMakro( m_globaleDaten.getDavBenutzer()) );
    Attribute a4 = new Attribute ("passwort", ersetzeDurchMakro( m_globaleDaten.getDavPasswort()) );
    
    em.setAttribute( a1 );
    em.setAttribute( a2 );
    em.setAttribute( a3 );
    em.setAttribute( a4 );
    
    root.addContent( em );
  }
  
  /**
   * Methode um Eintragen der Zugangsdaten usv in ein Element
   * @param root Element unter dem die Daten eingetragen werden
   */
  private void addUsv (Element root)
  {
    Element em = new Element ("usv");
    
    Attribute a1 = new Attribute ("pid", ersetzeDurchMakro( m_globaleDaten.getUsvPid()) );
    
    em.setAttribute( a1 );
    
    root.addContent( em );
  }
  
  /**
   * Methode um Eintragen der versorgten Rechner in ein Element
   * @param root Element unter dem die Daten eingetragen werden
   */
  private void addRechner (Element root)
  {
    String id = StartStoppVerwaltung.getInstanz().getOrignalStartStoppBlockId();
    
    List<RechnerGlobal> r = m_globaleDaten.getAlleRechner( id );
    
    if (r == null)
      return;
    
    for (int i=0; i<r.size(); ++i)
    {
      Element em = new Element ("rechner");
      
      Attribute a1 = new Attribute ("name",       ersetzeDurchMakro( r.get( i ).getAlias()) );
      Attribute a2 = new Attribute ("tcpAdresse", ersetzeDurchMakro( r.get( i ).getTcpAdresse()) );

      em.setAttribute( a1 );
      em.setAttribute( a2 );
      
      root.addContent( em );
    }
  }
  
  /**
   * Methode um Eintragen der versorgten Protokolldateien in ein Element
   * @param root Element unter dem die Daten eingetragen werden
   */
  private void addProtokollDatei (Element root)
  {
    String id = StartStoppVerwaltung.getInstanz().getOrignalStartStoppBlockId();
    
    List<ProtokollDatei> p = m_globaleDaten.getAlleProtokollDateien( id );
    
    if (p == null)
      return;
    
    for (int i=0; i<p.size(); ++i)
    {
      Element em = new Element ("protokolldatei");
      
      Attribute a1 = new Attribute ("name",      ersetzeDurchMakro( p.get(i).getAlias()) );
      Attribute a2 = new Attribute ("nameDatei", ersetzeDurchMakro( p.get( i ).getNameDatei()) );
      Attribute a3 = new Attribute ("groesse",   ersetzeDurchMakro( "" + p.get( i ).getGroesse()) );
      
      em.setAttribute( a1 );
      em.setAttribute( a2 );
      em.setAttribute( a3 );
      
      root.addContent( em );
    }
  }

  
  /**
   * Methode erzeugt das Element <applikationen> des StartStopp Blocks
   * @return Element <applikationen>
   */
  private Element erzeugeElementApplikationen()
  {
    Element root = new Element ("applikationen");
    
    ProzessVerwaltung pv = ProzessVerwaltung.getInstanz();
    
    // Bestimmen der IDs des ursrünglichen StartStopp Blocks
    
    StartStoppVerwaltung ssv = StartStoppVerwaltung.getInstanz();
    
    String originalId = ssv.getOrignalStartStoppBlockId();

    // Prozessdaten laden

    List<ProzessDaten> pd = new ArrayList<ProzessDaten> ();

    if (originalId == null)
    {
//      System.out.println("Versionierung aller Prozesse");
      
      pd = pv.getAlleProzessDaten();
    }
    else
    {
//      System.out.println("Versionierung der Prozesse des StartStoppBlocks " + originalId);
      
      List<String> ids = ssv.getStartStoppBlock( originalId ).getAllProcessIds();
    
      for (int i=0; i<ids.size(); ++i)
        pd.add( pv.getProzessDaten( ids.get( i ) ));
    }
    
    for (int i = 0; i<pd.size(); ++i)
      addInkarnation (root, pd.get( i ));
    
    return root;
  }

  /**
   * Methode um Eintragen einer Inkarnation in ein Element
   * @param root Element unter dem die Daten eingetragen werden
   * @param pd Prozessdaten der Inkarnation
   */
  private void addInkarnation (Element root, ProzessDaten pd)
  {
    Element em = new Element ("inkarnation");
    
    Attribute a1 = new Attribute ("name", pd.getName());
    em.setAttribute( a1 );
    
    addAusfuehrbareDatei( em, pd );
    
    addAufrufParameter( em, pd );
    
    addStartArt( em, pd );

    addStartBedingung( em, pd );

    addStoppBedingung( em, pd );

    addStandardAusgabe( em, pd );

    addStandardFehlerAusgabe( em, pd );

    addStartFehlerVerhalten( em, pd );
    
    addStoppFehlerVerhalten( em, pd );

    root.addContent( em );
  }

  /**
   * Methode um Eintragen der ausführbaren Datei einer Inkarnation in ein Element
   * @param root Element unter dem die Daten eingetragen werden
   * @param pd Prozessdaten der Inkarnation
   */
  private void addAusfuehrbareDatei (Element root, ProzessDaten pd)
  {
    Element em = new Element ("applikation");
    
    Attribute a1 = new Attribute ("name", ersetzeDurchMakro( pd.getAusfuehrbareDatei()) );
    
    em.setAttribute( a1 );
    
    root.addContent( em );
  }

  /**
   * Methode um Eintragen der versorgten Aufrufparameter in ein Element
   * @param root Element unter dem die Daten eingetragen werden
   * @param pd Prozessdaten der Inkarnation
   */
  private void addAufrufParameter (Element root, ProzessDaten pd)
  {
    List<String> aufrufparameter = pd.getAufrufParameter();
    for (int i=0; i<aufrufparameter.size(); ++i)
    {      
      Element em = new Element ("aufrufparameter");
      Attribute a1 = new Attribute ("wert", ersetzeDurchMakro( aufrufparameter.get( i )) );
      
      em.setAttribute( a1 );
      root.addContent( em );
    }
  }

  /**
   * Methode um Eintragen der versorgten Startart in ein Element
   * @param root Element unter dem die Daten eingetragen werden
   * @param pd Prozessdaten der Inkarnation
   */
  private void addStartArt (Element root, ProzessDaten pd)
  {
    Element em = new Element ("startart");
    
    Attribute a1 = new Attribute ("option",    ersetzeDurchMakro( pd.getStartArt().getOption().getText()) );
    Attribute a2 = new Attribute ("neustart",  ersetzeDurchMakro( pd.getStartArt().getNeuStart().getText()) );
    Attribute a3 = new Attribute ("intervall", ersetzeDurchMakro( "" + pd.getStartArt().getIntervallZeit()) );
    
    em.setAttribute( a1 );
    em.setAttribute( a2 );
    em.setAttribute( a3 );
    
    root.addContent( em );
  }

  /**
   * Methode um Eintragen der versorgten Startbedingungen in ein Element
   * @param root Element unter dem die Daten eingetragen werden
   * @param pd Prozessdaten der Inkarnation
   */
  private void addStartBedingung (Element root, ProzessDaten pd)
  {
    List<StartBedingung> sb = pd.getStartBedingung();
    
    for (int i=0; i<sb.size(); ++i)
    {
      Element em = new Element ("startbedingung");
      
      Attribute a1 = new Attribute ("vorgaenger", ersetzeDurchMakro( sb.get( i ).getProzessName()) );
      Attribute a2 = new Attribute ("warteart",   ersetzeDurchMakro( sb.get( i ).getWarteArt().getText()) );
      Attribute a3 = new Attribute ("rechner",    ersetzeDurchMakro( sb.get( i ).getRechnerAlias()) );
      Attribute a4 = new Attribute ("wartezeit",  ersetzeDurchMakro( "" + sb.get( i ).getWarteZeit()) );
      
      em.setAttribute( a1 );
      em.setAttribute( a2 );
      em.setAttribute( a3 );
      em.setAttribute( a4 );
    
      root.addContent( em );
    }
  }

  /**
   * Methode um Eintragen der versorgten Stoppbedingungen in ein Element
   * @param root Element unter dem die Daten eingetragen werden
   * @param pd Prozessdaten der Inkarnation
   */
  private void addStoppBedingung (Element root, ProzessDaten pd)
  {
    List<StoppBedingung> sb = pd.getStoppBedingung();
    
    for (int i=0; i<sb.size(); ++i)
    {
      Element em = new Element ("stoppbedingung");
      
      Attribute a1 = new Attribute ("nachfolger", ersetzeDurchMakro( sb.get( i ).getProzessName()) );
      Attribute a2 = new Attribute ("rechner",    ersetzeDurchMakro( sb.get( i ).getRechnerAlias()) ) ;
      Attribute a3 = new Attribute ("wartezeit",  ersetzeDurchMakro( "" + sb.get( i ).getWarteZeit()) );
      
      em.setAttribute( a1 );
      em.setAttribute( a2 );
      em.setAttribute( a3 );
    
      root.addContent( em );
    }
  }

  /**
   * Methode um Eintragen der versorgten Standardausgabe in ein Element
   * @param root Element unter dem die Daten eingetragen werden
   * @param pd Prozessdaten der Inkarnation
   */
  private void addStandardAusgabe (Element root, ProzessDaten pd)
  {
    Element em = new Element ("standardAusgabe");
    
    Attribute a1 = new Attribute ("option",    ersetzeDurchMakro( pd.getStandardAusgabe().getOption().getText()) );
    Attribute a2 = new Attribute ("dateiname", ersetzeDurchMakro( pd.getStandardAusgabe().getDateiAlias()) );
    
    em.setAttribute( a1 );
    em.setAttribute( a2 );
    
    root.addContent( em );
  }

  /**
   * Methode um Eintragen der versorgten Standardfehlerausgabe in ein Element
   * @param root Element unter dem die Daten eingetragen werden
   * @param pd Prozessdaten der Inkarnation
   */
  private void addStandardFehlerAusgabe (Element root, ProzessDaten pd)
  {
    Element em = new Element ("standardFehlerAusgabe");
    
    Attribute a1 = new Attribute ("option",    ersetzeDurchMakro( pd.getStandardFehlerAusgabe().getOption().getText()) );
    Attribute a2 = new Attribute ("dateiname", ersetzeDurchMakro( pd.getStandardFehlerAusgabe().getDateiAlias()) );
    
    em.setAttribute( a1 );
    em.setAttribute( a2 );
    
    root.addContent( em );
  }

  /**
   * Methode um Eintragen des versorgten Verhaltens beim Start in ein Element
   * @param root Element unter dem die Daten eingetragen werden
   * @param pd Prozessdaten der Inkarnation
   */
  private void addStartFehlerVerhalten (Element root, ProzessDaten pd)
  {
    Element em = new Element ("startFehlerverhalten");
    
    Attribute a1 = new Attribute ("option",         ersetzeDurchMakro( pd.getStartVerhaltenFehler().getOption().getText()) );
    Attribute a2 = new Attribute ("wiederholungen", ersetzeDurchMakro( "" + pd.getStartVerhaltenFehler().getWiederholungen()) );
    
    em.setAttribute( a1 );
    em.setAttribute( a2 );
    
    root.addContent( em );
  }

  /**
   * Methode um Eintragen des versorgten Verhaltens beim Stoppen in ein Element
   * @param root Element unter dem die Daten eingetragen werden
   * @param pd Prozessdaten der Inkarnation
   */
  private void addStoppFehlerVerhalten (Element root, ProzessDaten pd)
  {
    Element em = new Element ("stoppFehlerverhalten");
    
    Attribute a1 = new Attribute ("option",         ersetzeDurchMakro( pd.getStoppVerhaltenFehler().getOption().getText()) );
    Attribute a2 = new Attribute ("wiederholungen", ersetzeDurchMakro( "" + pd.getStoppVerhaltenFehler().getWiederholungen()) );
    
    em.setAttribute( a1 );
    em.setAttribute( a2 );
    
    root.addContent( em );
  }

  /**
   * Methode wandelt in einem String Textpassagen in Makroaufrufe um die
   * im globalen Teil der StartStopp Datei definiert sind
   * @param text Umzuwandelnder Text
   * @return umgewandelter Text
   */
  private String ersetzeDurchMakro (String text)
  {
    String s1 = text;
    
    for (int i=0; i<m_globaleDaten.getMakroGlobal().size(); ++i)
    {
      String makro = m_globaleDaten.getMakroGlobal().get( i ).getName();
      String wert  = m_globaleDaten.getMakroGlobal().get( i ).getWert();

      makro = "%" + makro + "%";
      
      s1 = s1.replace( wert , makro );
    }
    
    return s1;
  }

  /**
   * @return liefert die Klassenvariable m_root zurück
   */
  public Element getRoot()
  {
    return m_root;
  }
}
