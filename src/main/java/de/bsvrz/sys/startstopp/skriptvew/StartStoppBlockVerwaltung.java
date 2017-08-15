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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Klasse zum Verwalten der StartStopp Blöcke
 * @author Dambach Werke GmbH
 */
public class StartStoppBlockVerwaltung
{
  /**
   * Hashmap mit den Daten der StartStopp Blöcke
   */
  private HashMap<String, StartStoppBlock> m_startStoppBlockDaten;
  
  /**
   * Mutex zum synchronisierten Zugriff auf die Hashmap
   */
  private Object mutex = new Object ();
  
  /**
   * Parameterloser Konstruktor der Klasse, legt eine leere Hashmap an
   */
  public StartStoppBlockVerwaltung ()
  {
    m_startStoppBlockDaten = new HashMap<String, StartStoppBlock>();
  }
  
  /**
   * Methode zum Hinzufügen der Daten 
   * @param id Id unter der die Daten abgelegt werden
   * @param daten Daten die hinzugefügt werden sollen
   */
  public void addDaten( String id, StartStoppBlock daten )
  {
    synchronized (mutex)
    {
      m_startStoppBlockDaten.put( id, daten );
    }
  }

  /**
   * Methode zum Entfernen der Daten eines StartStopp Blocks. Der Block wird über seine
   * ID identifiziert
   * @param id Id des zum Entfernenden StartStopp Blocks
   */
  public void removeDaten ( String id )
  {
    synchronized (mutex)
    {
      if (m_startStoppBlockDaten.containsKey( id ))
        m_startStoppBlockDaten.remove( id );
    }
  }

  /**
   * Methode liefert die Daten eines StartStopp Blocks mit einer bestimmten ID. 
   * @param id Id des zu lesenden StartStopp Blocks
   * @return Daten des StartStopp Blocks sofern vorhanden, sonst null.
   */
  public StartStoppBlock getDaten ( String id )
  {
    synchronized (mutex)
    {
      if (m_startStoppBlockDaten.containsKey( id ))
        return m_startStoppBlockDaten.get( id );
      else
        return null;
    }
  }

  /**
   * Methode prüft, ob ein StartStopp Block mit einer bestimmten ID bereits versorgt ist.
   * @param id Id des zu prüfenden Blocks
   * @return Block bereits bekannt: true, sonst false
   */
  public boolean isStartStoppBlockBekannt (String id)
  {
    synchronized (mutex)
    {
      return m_startStoppBlockDaten.containsKey( id );
    }
  }
  
  /**
   * Methode liefert alle StartStopp Blöcke in Form einer Hashmap. Key = ID eines StartStopp Blocks,
   * Value = Daten eines StartStopp Blocks
   * @return alle Prozessdaten
   */
  public HashMap<String,StartStoppBlock> getAlleDaten ()
  {
    HashMap<String,StartStoppBlock> copy = new HashMap<String, StartStoppBlock>();
    
    synchronized (mutex)
    {
      Iterator it = m_startStoppBlockDaten.entrySet().iterator();
      
      while (it.hasNext())
      {
        Map.Entry me = (Map.Entry)it.next();
        
        String key = (String)me.getKey();
        StartStoppBlock value = (StartStoppBlock) me.getValue();
        
        copy.put( key, value );
      }
    }
    
    return copy;
  }

  /**
   * Methode löscht alle StartStopp Blöcke
   */
  public void reset()
  {
    synchronized (mutex)
    {
      m_startStoppBlockDaten.clear();
    } 
  }
}
