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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import org.jdom.Element;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.prozessvew.ProzessVerwaltung;
import de.bsvrz.sys.startstopp.prozessvew.Tools;

/**
 * Klasse die die globalen Daten der StartStopp Datei beinhaltet. Klasse wird als Singelton
 * ausgeführt.
 * @author Dambach Werke GmbH
 */
public class GlobaleDaten
{
  /**
   * Debug
   */
  private static final Debug logger = Debug.getLogger();

  private final boolean _debug = false;

  /**
   * Liste mit den Prozessen, die zum Kernsystem gehören
   */
  private List<Kernsystem> m_kernSystem; 
  
  /**
   * Adresse des DaV mit dem sich die StartStopp Applikation verbinden soll
   */
  private String m_davAdresse;
  
  /**
   * Port des DaV mit dem sich die StartStopp Applikation verbinden soll
   */
  private int m_davPort;
  
  /**
   * Benutzername mit dem sich die StartStopp Applikation mit dem DaV verbinden soll
   */
  private String m_davBenutzer;
  
  /**
   * Passwort mit dem sich die StartStopp Applikation mit dem DaV verbinden soll
   */
  private String m_davPasswort;
  
  /**
   * PID der USV deren Statusmeldungen die StartStopp Applikation auswerten soll
   */
  private String m_usvPid;
  
  /**
   * Liste der Makros
   */
  private List<MakroGlobal> m_makroGlobal;
  
  /**
   * Verwaltung der globalen Rechner. Die Daten der Rechner werden in einer
   * Hashmap abgelegt. Als Key dient dabei die TCP/IP Adresse des Rechners. 
   * Als Wert der Hashmap werden die Rechnereigenschaften eingetragen. 
   */
  private HashMap<String, RechnerGlobal> m_rechner = new HashMap<String, RechnerGlobal>();

  /**
   * Zuordnung der Alias Bezeichnung der globalen Rechner zu den richtigen Daten. Da es
   * möglich ist, dass unterschiedliche StartStopp Blöcke mit den identischen Aliasbezeichnungen
   * unterschiedliche Rechner referenzieren wollen, bzw. mit unterschiedlichen Aliasbezeichnungen
   * die selben Rechner referenzieren wollen, wird die Zuordnung Alias - realer Rechner in
   * einer Hashmap abgelegt. Als Key wird in dieser Hashmap die Kombination StartStoppBlock Id 
   * ergänzt mit der Aliasbezeichnung des Rechners verwendet. Als Wert in der Hashmap wird die 
   * TCP/IP Adresse verwendet. Die Eigenschaften des Rechners werden in der Klassenvariable 
   * m_rechner festgelegt.    
   */
  private HashMap<String, String> m_rechnerAlias = new HashMap<String, String>();

  /**
   * Verwaltung der Protokolldateien. Die Daten der Protokolldateien werden in einer
   * Hashmap abgelegt. Als Key dient dabei der richtige Dateiname. Als Wert der Hashmap
   * werden die Dateieigenschaften eingetragen. 
   */
  private HashMap<String, ProtokollDatei> m_dateien = new HashMap<String, ProtokollDatei>();
  
  /**
   * Zuordnung der Alias Bezeichnung der Protokolldateien zu den richtigen Daten. Da es
   * möglich ist, dass unterschiedliche StartStopp Blöcke mit den identischen Aliasbezeichnungen
   * unterschiedliche reale Dateien referenzieren wollen, bzw. mit unterschiedlichen Aliasbezeichnungen
   * die selben realen Dateien referenzieren wollen, wird die Zuordnung Alias - reale Datei in
   * einer Hashmap abgelegt. Als Key wird in dieser Hashmap die Kombination StartStoppBlock Id 
   * ergänzt mit der Aliasbezeichnung der Datei verwendet. Als Wert in der Hashmap wird der 
   * reale Dateiname verwendet. Die Eigenschaften der Datei (z.B. max Grösse). werden in der
   * Klassenvariable m_dateien festgelegt.    
   */
  private HashMap<String, String> m_dateiAlias = new HashMap<String, String>();
  
  /**
   * Eigenschaften der StartStopp Blöcke. Als Key wird die ID der StartStopp Blöcke verwendet
   */
  private TreeMap<String, StartStoppEigenschaften> m_eigenschaften = new TreeMap<String, StartStoppEigenschaften> (); 
  
  /**
   * Liste mit den Klassen dich sich als Listener auf die Klasse angemeldet haben. Diese Klassen
   * werden informiert wenn sich Änderungen in den globalen Daten ereignet haben. Insbesondere ist
   * dies wichtig wenn sich die Liste der globalen Rechner geändert hat, da sich die Klasse 
   * DaV Kommunikation bei den neuen Rechnern zum Empfang der Prozessinformationen anmelden muss.
   */
  
  private List<IGlobaleDatenListener> m_angemeldeteListener = new LinkedList<IGlobaleDatenListener>();

  /**
   * Konstruktor der Klasse
   */
  private GlobaleDaten()
  {
    m_kernSystem= new ArrayList <Kernsystem>();
    m_makroGlobal= new ArrayList <MakroGlobal>();
  }
  
  /**
   * Liefert die einzige Instanz der Klasse
   * @return einzige Instanz der Klasse
   */
  public static GlobaleDaten getInstanz()
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
    private static GlobaleDaten INSTANCE = new GlobaleDaten();
  }

  /**
   * @return liefert die Klassenvariable m_davAdresse zurück
   */
  public String getDavAdresse()
  {
    return m_davAdresse;
  }

  /**
   * @param davAdresse setzt die Klassenvariable m_davAdresse
   */
  public void setDavAdresse( String davAdresse )
  {
    m_davAdresse = davAdresse;
  }

  /**
   * @return liefert die Klassenvariable m_davBenutzer zurück
   */
  public String getDavBenutzer()
  {
    return m_davBenutzer;
  }

  /**
   * @param davBenutzer setzt die Klassenvariable m_davBenutzer
   */
  public void setDavBenutzer( String davBenutzer )
  {
    m_davBenutzer = davBenutzer;
  }

  /**
   * @return liefert die Klassenvariable m_davPasswort zurück
   */
  public String getDavPasswort()
  {
    return m_davPasswort;
  }

  /**
   * @param davPasswort setzt die Klassenvariable m_davPasswort
   */
  public void setDavPasswort( String davPasswort )
  {
    m_davPasswort = davPasswort;
  }

  /**
   * @return liefert die Klassenvariable m_davPort zurück
   */
  public int getDavPort()
  {
    return m_davPort;
  }

  /**
   * @param davPort setzt die Klassenvariable m_davPort
   */
  public void setDavPort( int davPort )
  {
    m_davPort = davPort;
  }

  /**
   * @return liefert die Klassenvariable m_eigenschaften zurück
   */
  public TreeMap<String, StartStoppEigenschaften> getEigenschaften()
  {
    return m_eigenschaften;
  }

  /**
   * @param eigenschaften setzt die Klassenvariable m_eigenschaften
   */
  public void setEigenschaften( TreeMap<String, StartStoppEigenschaften> eigenschaften )
  {
    m_eigenschaften = eigenschaften;
  }

  /**
   * @return liefert die Klassenvariable m_kernSystem zurück
   */
  public List<Kernsystem> getKernSystem()
  {
    return m_kernSystem;
  }

  /**
   * @param kernSystem setzt die Klassenvariable m_kernSystem
   */
  public void setKernSystem( List<Kernsystem> kernSystem )
  {
    m_kernSystem = kernSystem;
  }

  /**
   * @param makroGlobal setzt die Klassenvariable m_makroGlobal
   */
  public void setMakroGlobal( List<MakroGlobal> makroGlobal )
  {
    m_makroGlobal = makroGlobal;
  }

  /**
   * Methode stellt fest, ob eine Inkarnation zum Kensystem gehört oder nicht
   * @param inkarnation Name der Inkarnation
   * @return true: Inkarnation gehört zum Kernsystem, false: Inkarnation gehört
   * nicht zum Kernsystem
   */
  public boolean isKernsystem (String inkarnation )
  {
    return inkarnationGehoertZumKernsystem( inkarnation );
  }

  /**
   * Methode liefert die Kernsystemeinstellungen der Inkarnatione zurück, deren Name übergeben wird 
   * @param inkarnation Name der Inkarnation
   * @return wenn Inkarnation zum kernsystem gehört werden die Kernsystemeinstellungen zurückgegeben, sonst null
   */
  public Kernsystem getKernsystemInkarnation (String inkarnation )
  {
    if (m_kernSystem != null)
    {
      Iterator<Kernsystem> it = m_kernSystem.iterator();
      while (it.hasNext())
      {
        Kernsystem ks = it.next();
        
        if (ks.getApplikation().equals( inkarnation ))
          return ks;
      }
    }
    
    return null;
  }

  /**
   * Methode fügt einen Prozess zu der Liste der Kernsysteme zu 
   * @param kernSystem Name des Prozesses
   */
  public void addKernSystem( Kernsystem kernSystem )
  {
    // Nur wenn ein Inkarnationsname angegeben ist
    
     if (!kernSystem.getApplikation().equals( "" ))
       m_kernSystem.add( kernSystem);
  }

  /**
   * @return liefert die Klassenvariable m_makroGlobal zurück
   */
  public List<MakroGlobal> getMakroGlobal()
  {
    return m_makroGlobal;
  }

  /**
   * Hinzufügen eines Makros zur Liste der globalen Makros
   * @param id Id des StartStopp Blocks zu dem die Makros gehören
   * @param name Name des Makros
   * @param wert Wert des Makros
   */
  public void addMakroGlobal( String id, String name, String wert )
  {
    MakroGlobal mg = new MakroGlobal(); 
    
    mg.setStartStoppBlockId( id );
    mg.setName( name );
    mg.setWert( wert );
    
    m_makroGlobal.add( mg );
  }
  
  /**
   * Methode liefert die Eigenschaften einer Protokolldatei durch Übergabe des Alias 
   * einer Datei. Da die Alias in den unterschiedliche StartStopp Blöcken unterschiedlich 
   * definiert sind, muss hier die ID des StartStopp Blocks angegeben werden.
   * @param startStoppBlockId Id des StartStoppBlocks
   * @param alias Alias Bezeichnung
   * @return Eigenschaften der Protokolldatei wenn vorhanden, sonst null
   */
  public ProtokollDatei getProtokollDateiByAlias (String startStoppBlockId, String alias)
  {
    String key = startStoppBlockId + "_" + alias;
    
    String dateiName = m_dateiAlias.get( key );
    
    return getProtokollDateiByName( dateiName );
  }

  /**
   * Methode liefert die Eigenschaften einer Protokolldatei durch Übergabe des Namens 
   * einer Datei. 
   * @param dateiName Name der Datei
   * @return Eigenschaften der Protokolldatei wenn vorhanden, sonst null
   */
  public ProtokollDatei getProtokollDateiByName (String dateiName)
  {
    if (m_dateien.containsKey( dateiName ))
      return m_dateien.get( dateiName );
    
    return null;
  }

  /**
   * Methode zum Definieren einer Protokolldatei. Wird eine Datei (nicht der Alias !!!) mehrfach im globalen Teil
   * definiert, so wird nur die 1. Definition verwendet. Weitere Definitionen werden ignoriert.
   * @param startStoppBlockId Id des StartStoppBlocks zu dem diese Definition gehört
   * @param alias Alias der Datei
   * @param nameDatei richtiger Dateiname der Datei
   * @param groesse max. Grösse der Datei in MB
   */
  public void addProtokollDatei( String startStoppBlockId, String alias, String nameDatei, long groesse )
  {
    ProtokollDatei pd = new ProtokollDatei(startStoppBlockId, alias, nameDatei, groesse);
    
    // Nur wenn Datei noch nicht verwendet wird. 
    
    if (!m_dateien.containsKey( nameDatei ))
      m_dateien.put( nameDatei, pd );
    
    String key = startStoppBlockId + "_" + alias;
    
    m_dateiAlias.put( key, nameDatei );
  }
  
  /**
   * Methode zum Definieren eines Rechners. Wird ein Rechner (nicht der Alias !!!) mehrfach im 
   * globalen Teil definiert, so wird nur die 1. Definition verwendet. Weitere Definitionen 
   * werden ignoriert.
   * @param startStoppBlockId Id des StartStoppBlocks zu dem diese Definition gehört
   * @param alias Alias des Rechners
   * @param tcp TCP/IP Adresse des Rechners
   */
  public void addRechner( String startStoppBlockId, String alias, String tcp )
  {
    RechnerGlobal rg = new RechnerGlobal(startStoppBlockId, alias, tcp); 

    // Nur wenn Rechner noch nicht verwendet wird. 
    
    if (!m_rechner.containsKey( tcp ))
      m_rechner.put( tcp, rg );
    
    String key = startStoppBlockId + "_" + alias;
    
    m_rechnerAlias.put( key, tcp );
  }
  
  /**
   * @return liefert die Klassenvariable m_usvPid zurück
   */
  public String getUsvPid()
  {
    return m_usvPid;
  }

  /**
   * @param usvPid setzt die Klassenvariable m_usvPid
   */
  public void setUsvPid( String usvPid )
  {
    m_usvPid = usvPid;
  }

  /**
   * Methode zum Einlesen der globalen Daten aus einer XML-Struktur. Nicht behandelt werden
   * in dieser Methode die Makros. Diese wurden bereits in der Klasse SkriptVerwaltung bearbeitet.
   * Bei XML-Strukturen die über den DaV empfangen wurden (atg.startStoppAnfrage) werden nur
   * die Items "rechner" und "protokolldatei" ausgewertet, die anderen Items werden ignoriert.
   * @param startStoppBlockId Id des StartStopp Blcoks aus dem die Daten sind
   * @param root Wurzelelement der XML-Struktur 
   * @param absender Auslöser der Aktivität
   * @param simulationsVariante Simulationsvariante des StartStopp Blocks
   */
  public void initialisiereGlobaleDaten (String startStoppBlockId, Element root, long absender, long simulationsVariante) 
  {
    Element startStoppElement = root.getChild("startStopp");
    
    String version         = startStoppElement.getAttributeValue("Versionsnummer" );
    String erstelltAm      = startStoppElement.getAttributeValue("ErstelltAm" );
    String erstelltDurch   = startStoppElement.getAttributeValue("ErstelltDurch" );
    String aenderungsGrund = startStoppElement.getAttributeValue("Aenderungsgrund" );
    
    m_eigenschaften.put( startStoppBlockId, 
                         new StartStoppEigenschaften (absender, version, erstelltAm, erstelltDurch, aenderungsGrund) );
    
    //Holen von global
    
    Element globalElement = startStoppElement.getChild( "global" );
    
    ListIterator<Element> listeKinderGlobal = globalElement.getChildren().listIterator();
    
    //----------------------------------------------------------------------
    // Diese While schleife durchläuft die einzelnen Subelemente
    //----------------------------------------------------------------------
    
    while (listeKinderGlobal.hasNext())
    {
      Element child = listeKinderGlobal.next();

      // Ignorieren von
      // - kernsystem
      // - zugangdav
      // - usv
      
      if (absender == 0)
      {
        if (child.getName() == "kernsystem")
        {
          String value      = child.getAttribute( "inkarnationsname" ).getValue();
          
          Kernsystem ks              = null;
          String wartezeit           = null;
          String mitInkarnationsName = null;
          
          if (child.getAttribute( "wartezeit" ) != null)
            wartezeit  = child.getAttribute( "wartezeit" ).getValue();

          if (child.getAttribute( "mitInkarnationsname" ) != null)
            mitInkarnationsName  = child.getAttribute( "mitInkarnationsname" ).getValue();

          value = Tools.bestimmeInkarnationsName( value, simulationsVariante );
          
          if (wartezeit != null)
          {
            ks = new Kernsystem (value, ProzessVerwaltung.myLong (wartezeit));
          }
          else
          {
            ks = new Kernsystem (value);
          }
          
          if (mitInkarnationsName != null)
            ks.setMitInkarnationsname( mitInkarnationsName.equals( "ja" ) );
          
          addKernSystem ( ks );
        }
        
        if (child.getName() == "makrodefinition")
        {
          // Makros wurden bereits in der Klasse Skriptverwaltung verarbeitet        
        }
  
        if (child.getName() == "zugangdav")
        {
          String adresse  = child.getAttribute( "adresse" ).getValue();
          String port     = child.getAttribute( "port" ).getValue();
          String username = child.getAttribute( "username" ).getValue();
          String passwort = child.getAttribute( "passwort" ).getValue();
  
          Integer p = 0;
          
          try
          {
            p = Integer.parseInt ( port );
          }
          catch ( Exception e )
          {
          }                
  
          setDavAdresse( adresse );
          setDavPort( p );
          setDavBenutzer( username );
          setDavPasswort( passwort );
        }
  
        if (child.getName() == "usv")
        {
          String pid = child.getAttribute( "pid" ).getValue();
          
          setUsvPid( pid );
        }
        
      } // if (absender == 0)

      if (child.getName() == "rechner")
      {
        String name    = child.getAttribute( "name" ).getValue();
        String adresse = child.getAttribute( "tcpAdresse" ).getValue();
        
        addRechner( startStoppBlockId, name, adresse );
      }

      if (child.getName() == "protokolldatei")
      {
        String name  = child.getAttribute( "name" ).getValue();
        String datei = child.getAttribute( "nameDatei" ).getValue();
        String groesse = child.getAttribute( "groesse" ).getValue();

        Long l = 0l;
        
        try
        {
          l = Long.parseLong( groesse );
        }
        catch ( Exception e )
        {
        }                

        addProtokollDatei( startStoppBlockId, name, datei, l );
      }

    } // while

    // Listener über die Änderung benachrichtigen
    
    benachrichtigeListener ();

    if (_debug)
    {
      if (absender == 0)
      {
        System.out.println("ThreadStarteKernsystem:");
        for (int i=0; i<getKernSystem().size(); ++i)
          System.out.println("- " +  getKernSystem().get( i ));
    
        System.out.println("Zugang DaV:");
        System.out.println("- Adresse: " + getDavAdresse());
        System.out.println("- Port: " + getDavPort());
        System.out.println("- User: " + getDavBenutzer());
        System.out.println("- Passwort: " + getDavPasswort());
        
        System.out.println("USV:");
        System.out.println("- Pid: " + getUsvPid());
      }
      
      System.out.println("Rechner:");
      Iterator it = m_rechnerAlias.entrySet().iterator();
      while (it.hasNext())
      {
        Map.Entry me = (Map.Entry) it.next();
        
        String ip = (String) me.getValue();
        
        RechnerGlobal rg = m_rechner.get( ip );
        
        if (rg != null)
          System.out.println("- " + rg.getAlias() + " " + rg.getTcpAdresse());
      }
      
      System.out.println("Protokolldatei:");
      
      it = m_dateiAlias.entrySet().iterator();
      while (it.hasNext())
      {
        Map.Entry me = (Map.Entry) it.next();
        
        String dateiName = (String) me.getValue();
        
        ProtokollDatei pd = m_dateien.get( dateiName );
        
        if (pd != null)
          System.out.println("- " + pd.getAlias() + " " + pd.getNameDatei() + " " + pd.getGroesse());
      }
    }
  }
  
  /**
   * Methode prüft, on eine Inkarantion zum Kernsystem gehört.
   * @param name Name der Inkarnation
   * @return true: Inkarnation gehört zum Kernsystem, false: 
   * Inkarnation gehört nicht zum Kernsystem
   */
  public boolean inkarnationGehoertZumKernsystem( String name )
  {
    if (getKernSystem().size() > 0)
    {
      for (int i=0; i<getKernSystem().size(); ++i)
      {
        Kernsystem ks = getKernSystem().get( i );
        String applikation = ks.getApplikation();
        
        if (applikation.equals( name ))
          return true;
      }
    }
    return false;
  }
  
  /**
   * Methode zum Lesen der Eigenschaften eines StartStopp Blocks
   * @param id StartStopp Block Id des StartStopp Blocks der eingelsen werden soll 
   * @return Eigenschaften des StartStopp Blocks
   */
  public StartStoppEigenschaften getStartStoppEigenschaften (String id)
  {
    if (m_eigenschaften.containsKey( id ))
      return m_eigenschaften.get( id );
    
    return null;
  }
  
  /**
   * Methode liefert zu einer übergebenen Aliasbezeichnung eines Rechners die dazugehörende
   * TCP/IP Adresse die in dem globalen Teil der StartStopp.xml definiert wurde. Wird ein Alias
   * übergeben der nicht Teil der Definition der globalen Daten ist, wird null zuückgeliefert.
   * Wird als Alias "" übergeben, so wird die lokalte IP Adresse zurückgegeben.
   * @param alias Aliasbezeichnung des Rechners
   * @return IP-Adresse als String oder null
   */
  public String getIpAdresse (String startStoppId, String alias)
  {
    // Eigener Rechner
    
    if (alias.equals( "" ))
      return StartStoppApp.getRechnerAdresse();

    String key = startStoppId + "_" + alias;
    
    if (startStoppId == null)
    {
      logger.error("GlobaleDaten.getIpAdresse: startStoppId = null");
    }
    
    if (m_rechnerAlias.containsKey( key ))
      return m_rechnerAlias.get(key);

    // Fehlerfall: Kein Rechner gefunden
    
    return null;
  }

  /**
   * Methode liefert alle ProtokollDateien die in einem StartStopp Block definiert waren
   * @param startStoppId Id des StartStoppBlocks
   * @return ProtokollDateien die in einem StartStopp Block definiert waren
   */
  public List<ProtokollDatei> getAlleProtokollDateien (String startStoppId)
  {
    List<ProtokollDatei> list = new ArrayList<ProtokollDatei> ();

    Iterator it = m_dateiAlias.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry me = (Map.Entry) it.next();
      
      String key = (String) me.getKey();
    
      if (key.startsWith( startStoppId ))
        list.add( m_dateien.get( me.getValue()));
    }

    return list;
  }

  /**
   * Methode liefert alle Rechner die in einem StartStopp Block definiert waren
   * @param startStoppId Id des StartStoppBlocks
   * @return Rechner die in einem StartStopp Block definiert waren
   */
  public List<RechnerGlobal> getAlleRechner (String startStoppId)
  {
    List<RechnerGlobal> list = new ArrayList<RechnerGlobal> ();

    Iterator it = m_rechnerAlias.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry me = (Map.Entry) it.next();
      
      String key = (String) me.getKey();
    
      if (key.startsWith( startStoppId ))
        list.add( m_rechner.get( me.getValue()));
    }

    return list;
  }

  /**
   * Methode liefert alle Rechner die der StartStopp Applikation bekannt sind
   * @return alle bekannten Rechner
   */
  public List<RechnerGlobal> getAlleRechner ()
  {
    List<RechnerGlobal> list = new ArrayList<RechnerGlobal> ();

    Iterator it = m_rechner.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry me = (Map.Entry) it.next();
      
      list.add( (RechnerGlobal) me.getValue() );
    }

    return list;
  }

  //----------------------------------------------------------------------------------------------
  // Listener Funktionalitäten
  //----------------------------------------------------------------------------------------------

  /**
   * Methode zum Hinzufügen eines Listeners
   * @param listener Listener der hinzugefügt werden soll
   */
  public void addListener (IGlobaleDatenListener listener)
  {
    m_angemeldeteListener.add( listener );
  }
 
  /**
   * Methode zum Entfernen eines Listeners
   * @param listener Listener der entfernt werden soll
   */
  public void removeListener (IGlobaleDatenListener listener)
  {
    m_angemeldeteListener.remove( listener );
  }
 
  /**
   * Methode mit der die Klasse alle bei ihr angemeldeten Listener
   * über die Änderung der globalen Daten informiert.
   */
  private void benachrichtigeListener ()
  {
    // neues Ereignis erzeugen
  
    GlobalEreignis e = new GlobalEreignis (this);
   
    // zu übergebende Daten eintragen
  
    Iterator<IGlobaleDatenListener> it = m_angemeldeteListener.iterator();
    while (it.hasNext())
    {
      IGlobaleDatenListener l = it.next();
      l.exec(e);
    }
  }
  
  /**
   * Methode zum Löschen der Listener
   */
  public void removeAllListener ()
  {
    m_angemeldeteListener.clear();
  }
}
