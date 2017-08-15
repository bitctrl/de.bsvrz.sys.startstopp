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
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import de.bsvrz.sys.startstopp.prozessvew.DaVKommunikation;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_STATUS_STARTSTOPPBLOCK;

/**
 * Die Klasse verwaltet die einzelnen StartStopp Blöcke die durch die StartStopp
 * Applikation verwaltet werden. Sie verfügt über Methoden zum Starten, Stoppen 
 * und Neustarten der StartStopp Blöcke. Die Klasse wird als Singleton ausgeführt. 
 * @author Dambach Werke GmbH
 */
public class StartStoppVerwaltung
{
  private static final long serialVersionUID = 1L;

  /**
   * Hashmap mit den einzelnen StartStopplöcken
   */
  private StartStoppBlockVerwaltung m_startStoppBlockDaten;
  
  /**
   * Variable zum Verwalten der StartStoppBlock Nummern. Initialisierungmässig
   * auf 0, wird bei jedem Zugriff inkrementiert. 
   */
  private int m_startStoppBlockNummer = 0;

  /**
   * Verweis auf den original StartStopp Block (1. StartStopp Block den die 
   * Applikation gestartet hat)
   */
  private String m_orignalStartStoppBlockId = null;

  /**
   * Methode zum Lesen der einzigen Instanz der Klasse
   * @return einzige Instanz der Klasse
   */
  public static StartStoppVerwaltung getInstanz()
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
    private static StartStoppVerwaltung INSTANCE = new StartStoppVerwaltung();
  }

  /**
   * Konstruktor der Klasse
   */
  private StartStoppVerwaltung()
  {
    // Instanz anlegen zur Verwaltung der StartStopp Blöcke
    
    m_startStoppBlockDaten = new StartStoppBlockVerwaltung ();
  }
  
  /**
   * Methode bestimmt die nächste freie StartStoppBlock Id. Die StartStopp Block Id setzt sich wie folgt
   * zusammen: Rechnername + RechnerAdresse + Kennung StartStoppBlock + NummerStartStoppBlock 
   * @return StartStopp Block ID
   */
  public String getStartStoppBlockId ()
  {
    m_startStoppBlockNummer++;
    
    return getStartStoppBlockId(m_startStoppBlockNummer);
  }

  /**
   * Methode bestimmt die StartStopp Block ID eines StartStopp Blocks indem eine Nummer für
   * den StartStopp Block übergeben wird. Die StartStopp Block Id setzt sich wie folgt
   * zusammen: Rechnername + RechnerAdresse + Kennung StartStoppBlock + NummerStartStoppBlock
   * @param nummer Nummer die verwendet werden soll
   * @return StartStopp Block ID
   */
  public String getStartStoppBlockId (int nummer)
  {
    // Bestimmen der StartStoppId
    
    m_startStoppBlockNummer++;
/*    
    String id = StartStoppApp.getStartStoppInkarnationsName() + "_" +
                StartStoppApp.getRechnerName() + "_" + 
                StartStoppApp.getRechnerAdresse() + "_" +
                "StartStoppBlock" + "_" +
                nummer;
*/
    String id = StartStoppApp.getStartStoppInkarnationsName() + "_" +
//                StartStoppApp.getRechnerName() + "_" + 
                StartStoppApp.getRechnerAdresse() + "_" +
                "SSB" + "_" +
                nummer;

    return id; 
  }

  /**
   * Methode zum Anlegen eines neuen StartStopp Blocks
   * @param root Verweis auf orignal Wurzelement des StartStoppBlock (JDOM)
   * @param rootModifiziert Verweis auf modifiziertes Wurzelement des StartStoppBlock (JDOM) 
   * (aufgelöste Makros)
   * @return generierter StartStopp Block
   */
  public StartStoppBlock addStartStoppBlock( Element root, Element rootModifiziert )
  {
    // ID bestimmen
    
    String startStoppBlockId = getStartStoppBlockId();
    
    // Name des orignal (1.) StartStopp Blocks merken
    
    if (m_orignalStartStoppBlockId == null)
      m_orignalStartStoppBlockId = startStoppBlockId;
    
    // Neuen StartStoppBlock anlegen
    
    StartStoppBlock ssb = new StartStoppBlock (startStoppBlockId);
    
    // JDMOM Elemente hinzufügen
    
    ssb.setRoot( root );
    
    ssb.setRootModifiziert( rootModifiziert );
    
    // In Hashmap eintragen
    
    m_startStoppBlockDaten.addDaten( startStoppBlockId, ssb );
    
    // Telnet Schnittstelle aktualisieren
    
    if (StartStoppApp.getNutzerSchnittstelle() != null)
      StartStoppApp.getNutzerSchnittstelle().erzeugeStartStoppEintraege();
    
    return ssb;
  }
  
  /**
   * Methode zum Löschen eines StartStopp Blocks
   * @param startStoppBlockId Id des zu löschenden StartStopp Blocks
   * @param absenderId Absender Id
   */
  public void loescheStartStoppBlock (String startStoppBlockId, long absenderId)
  {
    System.out.println("Entferne " + startStoppBlockId);
    
    if (m_startStoppBlockDaten.isStartStoppBlockBekannt( startStoppBlockId ))
    {
      StartStoppBlock ssb = m_startStoppBlockDaten.getDaten( startStoppBlockId );

      ssb.loescheProzesse( absenderId );

      m_startStoppBlockDaten.removeDaten( startStoppBlockId );
    }

    if (StartStoppApp.getNutzerSchnittstelle() != null)
      StartStoppApp.getNutzerSchnittstelle().erzeugeStartStoppEintraege();
    
    // Information über geänderten StartStopp Info an DaV übertragen
    
    DaVKommunikation.getInstanz().sendeStartStoppInfo();
  }
  
  /**
   * Methode zum Auslesen eines StartStopp Blocks. Der StartStopp Block wird über
   * seine Id referenziert.
   * @param startStoppBlockId Id des StartStoppBlock
   * @return StartStoppBlock oder null im Fehlerfall
   */
  public StartStoppBlock getStartStoppBlock (String startStoppBlockId)
  {
    return m_startStoppBlockDaten.getDaten( startStoppBlockId );
  }

  /**
   * Methode zum Lesen aller StartStopp Blöcke 
   * @return Liste mit StartStopp Blöcken
   */
  public List<StartStoppBlock> getAllStartStoppBloecke ()
  {
    List<StartStoppBlock> l = new ArrayList<StartStoppBlock> ();

    HashMap<String, StartStoppBlock> copy = m_startStoppBlockDaten.getAlleDaten();

    Iterator it = copy.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry me = (Map.Entry)it.next();
      
      l.add( (StartStoppBlock) me.getValue());
    }
    
    return l;
  }

  /**
   * Methode zum Lesen der IDs aller der Applikation bekannten StartStopp Blöcke.
   * @return IDs der StartStopp Blöcke
   */
  public List<String> getStartStoppBlockIds ()
  {
    List<String> l = new ArrayList<String> ();

    HashMap<String, StartStoppBlock> copy = m_startStoppBlockDaten.getAlleDaten();

    Iterator it = copy.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry me = (Map.Entry)it.next();
      
      l.add( (String) me.getKey() );
    }
    
    return l;
  }
  
  /**
   * Methode zum Setzen des Status des StartStopp Blocks
   * @param startStoppBlockId Id des StartStoppBlocks
   * @param status Status des StartStopp Blocks 
   * (siehe de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums)
   */
  public void setzeZustand (String startStoppBlockId, E_STATUS_STARTSTOPPBLOCK status)
  {
    if (m_startStoppBlockDaten.isStartStoppBlockBekannt( startStoppBlockId ))
      m_startStoppBlockDaten.getDaten( startStoppBlockId ).setStatus( status );
  }

  /**
   * @return liefert die Klassenvariable m_orignalStartStoppBlockId zurück
   */
  public String getOrignalStartStoppBlockId()
  {
    return m_orignalStartStoppBlockId;
  }
  
  /**
   * Methode löscht alle StartStopp Blöcke
   */
  public void reset ()
  {
    m_startStoppBlockDaten.reset (); 
  }
}
