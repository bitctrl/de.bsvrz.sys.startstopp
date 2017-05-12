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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jdom.Element;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.prozessvew.DaVKommunikation;
import de.bsvrz.sys.startstopp.prozessvew.Logbuch;
import de.bsvrz.sys.startstopp.prozessvew.ProzessDaten;
import de.bsvrz.sys.startstopp.prozessvew.ProzessVerwaltung;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_STARTART;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_STATUS_STARTSTOPPBLOCK;

/**
 * Klasse realisiert einen StartStopp-Block
 * @author Dambach Werke GmbH
 */
public class StartStoppBlock
{
  /**
   * Debug
   */
  private static final Debug logger = Debug.getLogger();
  
  /**
   * StartStoppBlock ID
   */
  private String m_startStoppBlockId = null;
  
  /**
   * Verweis auf original Wurzelement (JDOM) des StartStopp Blocks
   */
  private Element m_root = null;
  
  /**
   * Verweis auf modifiziertes Wurzelement (JDOM) des StartStopp Blocks
   * (modifiziert heist, dass die Makros (%makro%) aufgelöst wurden.
   */
  private Element m_rootModifiziert = null;
  
  /**
   * Liste mit den IDs der zu diesem StartStopp Block gehörenden Prozessen
   */
  private List<String> m_processId = new ArrayList<String> ();
  
  /**
   * Status des StartStopp Blocks
   */
  
  private E_STATUS_STARTSTOPPBLOCK m_status;
  
  /**
   * Zeitpunkt der letzten Statusänderung
   */
  
  private Date m_statusZeitpunkt;
  
  /**
   * Simulationsvariante des StartStoppBlocks
   */
  private long m_simulationsVariante = 0l;
  
  /** 
   * Konstruktor der Klasse
   * @param startStoppBlockId Id des StartStopp Blocks
   */
  public StartStoppBlock (String startStoppBlockId)
  {
    m_startStoppBlockId = startStoppBlockId;
    
    m_status = E_STATUS_STARTSTOPPBLOCK.ANGELEGT;
    
    m_statusZeitpunkt = new Date ();
  }

  /**
   * @return liefert die Klassenvariable m_startStoppBlockId zurück
   */
  public String getStartStoppBlockId()
  {
    return m_startStoppBlockId;
  }
  
  /**
   * Methode zum Hinzufügen eines Prozesses zu einem StartStoppBlock
   * @param processId
   */
  public void addProzess (String processId)
  {
    m_processId.add( processId );  
  }
  
  /**
   * Methode zum Auslesen aller zu einem StartStoppBlock gehörenden Prozess IDs
   */
  public List<String> getAllProcessIds ()
  {
    return m_processId;
  }

  /**
   * @return liefert die Klassenvariable m_root zurück
   */
  public Element getRoot()
  {
    return m_root;
  }

  /**
   * @param root setzt die Klassenvariable m_root
   */
  public void setRoot( Element root )
  {
    this.m_root = root;
  }

  /**
   * @return liefert die Klassenvariable m_rootModifiziert zurück
   */
  public Element getRootModifiziert()
  {
    return m_rootModifiziert;
  }

  /**
   * @param rootModifiziert setzt die Klassenvariable m_rootModifiziert
   */
  public void setRootModifiziert( Element rootModifiziert )
  {
    this.m_rootModifiziert = rootModifiziert;
  }

  /**
   * @return liefert die Klassenvariable m_status zurück
   */
  public E_STATUS_STARTSTOPPBLOCK getStatus()
  {
    return m_status;
  }

  /**
   * Methode setzt die Klassenvariable m_status. In der Klassenvariablen m_statusZeitpunkt
   * wird die aktuelle Uhrzeit eingetragen. Zusätzliche wird die Änderung über die 
   * Attributgruppe atg.startStoppInfo publiziert. Eine Aktualisierung 
   * @param m_status setzt die Klassenvariable m_status bzw. m_statusZeitpunkt
   * un 
   */
  public void setStatus( E_STATUS_STARTSTOPPBLOCK m_status )
  {
    this.m_status = m_status;
    
    this.m_statusZeitpunkt = new Date ();
    
    // Änderung publizieren
    
    DaVKommunikation.getInstanz().sendeStartStoppInfo();
    
    // ... und der Nutzerschnittstelle mitteilen
    
    if (StartStoppApp.getNutzerSchnittstelle() != null)
      StartStoppApp.getNutzerSchnittstelle().erzeugeStartStoppEintraege();
  }

  /**
   * @return liefert die Klassenvariable m_date zurück
   */
  public Date getStatusZeitpunkt()
  {
    return m_statusZeitpunkt;
  }

  /**
   * Methode liefert den Zeitpunkt der letzten Statusänderung als String
   * im Format "dd.MM.yyyy HH:mm:ss"
   * @return String mit Zeitpunkt
   */
  public String getStatusZeitpunktAlsString ()
  {
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    
    if (m_statusZeitpunkt == null)
      return ("00.00.0000 00:00:00");
    else
      return sdf.format(m_statusZeitpunkt);
  }

  /**
   * Methode zum Starten der Prozesse eines StartStopp Blocks
   * @param absender Absender
   */
  public void starteProzesse ( long absender, long simulationsVariante )
  {
    logger.info("Starte Prozesse");

    if (!isPlausibel())
    {
      logger.error ("StartStopp Block \"" + m_startStoppBlockId + "\" nicht plausibel !");
      return;
    }
    
    m_simulationsVariante = simulationsVariante;
    
    ProzessVerwaltung pv = ProzessVerwaltung.getInstanz();
    
    if (absender == 0l)
    {
      // ThreadStarteKernsystem starten
    
      if (!pv.isKernSystemGestartet())
        pv.starteKernsystem ();
    }

    // Applikationen des StartStopp Blocks starten

    for (int i = 0; i<m_processId.size(); ++i)
    {
      String id = m_processId.get( i );

      ProzessDaten pd = pv.getProzessDaten( id );
      
      if (pd.getStartArt().getOption() == E_STARTART.AUTOMATISCH) 
      {
        if (!GlobaleDaten.getInstanz().inkarnationGehoertZumKernsystem (pd.getName()))
          pv.starteProzess( id, -1, simulationsVariante );
      }

      if (pd.getStartArt().getOption() == E_STARTART.INTERVALL) 
      {
        if (!GlobaleDaten.getInstanz().inkarnationGehoertZumKernsystem (pd.getName()))
          pv.starteProzessZyklisch( id, -1, simulationsVariante );
      }

      if (pd.getStartArt().getOption() == E_STARTART.MANUELL) 
      {
        if (!GlobaleDaten.getInstanz().inkarnationGehoertZumKernsystem (pd.getName()))
          pv.starteProzessManuell( id, -1, simulationsVariante );
      }

    }

    setStatus( E_STATUS_STARTSTOPPBLOCK.GESTARTET );
  }

  /**
   * Methode zum Stoppen der Prozesse eines StartStopp Blocks
   * @param absender Absender
   */
  public void stoppeProzesse ( long absender )
  {
    ProzessVerwaltung pv = ProzessVerwaltung.getInstanz();
    
    // Loop über die Inkarnationen des StartStopp Blocks
    
    for (int i = 0; i<m_processId.size(); ++i)
    {
      String id = m_processId.get( i );

      ProzessDaten pd = pv.getProzessDaten( id );
      
      if (!GlobaleDaten.getInstanz().inkarnationGehoertZumKernsystem (pd.getName()))
        pv.stoppeProzess( id, -1, false );
    }
    
    setStatus( E_STATUS_STARTSTOPPBLOCK.GESTOPPT );
  }

  /**
   * Methode zum Neustarten der Prozesse eines StartStopp Blocks
   * @param absender Absender
   * @param simulationsVariante Simulationsvariante
   */
  public void neuStarteProzesse( long absender, long simulationsVariante )
  {
    if (!isPlausibel())
    {
      logger.error ("StartStopp Block \"" + m_startStoppBlockId + "\" nicht plausibel !");
      return;
    }
    
    m_simulationsVariante = simulationsVariante;
    
    ProzessVerwaltung pv = ProzessVerwaltung.getInstanz();

    // Loop über die Inkarnationen des StartStopp Blocks
    
    for (int i = 0; i<m_processId.size(); ++i)
    {
      String id = m_processId.get( i );

      ProzessDaten pd = pv.getProzessDaten( id );
      
      if ((pd.getStartArt().getOption() == E_STARTART.AUTOMATISCH) ||
          (pd.getStartArt().getOption() == E_STARTART.INTERVALL  ))
      {
        if (!GlobaleDaten.getInstanz().inkarnationGehoertZumKernsystem (pd.getName()))
          pv.neuStartProzess( id, -1, simulationsVariante );
      }
    }
    
    setStatus( E_STATUS_STARTSTOPPBLOCK.GESTARTET );
  }

  /**
   * Methode zum Löschen der Prozesse eines StartStopp Blocks
   * @param absender Absender
   */
  public void loescheProzesse( long absender )
  {
    ProzessVerwaltung pv = ProzessVerwaltung.getInstanz();

    // Loop über die Inkarnationen des StartStopp Blocks
    // Wichtig: Daten in lokaler Liste halten, da die Methode ProzessVerwaltung.getInstanz().loescheProzess die
    // Zuordnung in der Liste m_processId löscht. 
    
    List<String> l = new ArrayList<String>();
    for (int i=0; i<m_processId.size(); ++i)
      l.add( m_processId.get( i ) );
    
    pv.loescheProzesse( l, absender );
  }

  /**
   * @return liefert die Klassenvariable m_simulationsVariante zurück
   */
  public long getSimulationsVariante()
  {
    return m_simulationsVariante;
  }
  
  /**
   * Method prüft ob die Prozessdaten eines StartStoppBlocks plausibel sind
   * @return true: Daten plausibel, false: Daten nicth plausibel
   */
  public boolean isPlausibel ()
  {
    String s = null;
    
    boolean plausibel = true;
    
    ProzessVerwaltung pv = ProzessVerwaltung.getInstanz();
    
    for (int i=0; i<m_processId.size(); ++i)
    { 
      String prozessId = m_processId.get( i );
      
      ProzessDaten pd = pv.getProzessDaten( prozessId );
      
      if (pd == null)
      {
        s = "Plausibilitätsfehler: keine Prozessdaten für Inkarantion \"" + prozessId + "\"";
        
        logger.error (s);
        
        plausibel = false;
      }
      else
      {
        if (!pd.isPlausibel())
          plausibel = false;
      }
    }
    
    return plausibel;
  }

  /**
   * Methode zum Löschen der Zuordnung eines Prozesses zu einem StartStoppBlock
   * @param prozessId Prozess Id des zu löschenden Prozesses 
   */
  public void loescheProzess( String prozessId )
  {
    if (m_processId.contains( prozessId ))
      m_processId.remove( prozessId );  
  }
}
