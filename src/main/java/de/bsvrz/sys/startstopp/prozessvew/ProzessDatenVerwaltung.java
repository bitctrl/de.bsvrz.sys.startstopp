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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Klasse zum Verwalten der Prozessinformationen der Remoterechner
 * @author Dambach Werke GmbH
 */
public class ProzessDatenVerwaltung
{
  /**
   * Hashmap mit den Prozessdaten der Remoterechner
   */
  private HashMap<String,ProzessDaten> m_prozessDaten = new HashMap<String, ProzessDaten>();
  
  /**
   * Parameterloser Konstruktor der Klasse, legt eine leere Hashmap an
   */
  public ProzessDatenVerwaltung ()
  {
  }
  
  /**
   * Methode zum Hinzufügen der Prozessdaten. Die Prozessdaten werden unter dem Key abgelegt, der
   * in dem Attribut ProzessId der Prozessdaten festgelegt ist.
   * @param prozessDaten Prozessdaten die hinzugefügt werden sollen
   */
  public void addProzessDaten( ProzessDaten prozessDaten )
  {
    synchronized (m_prozessDaten)
    {
      m_prozessDaten.put( prozessDaten.getProzessId(), prozessDaten );
    }
  }

  /**
   * Methode zum Hinzufügen der Prozessdaten 
   * @param id Id unter der die Prozessdaten abgelegt werden
   * @param prozessDaten Prozessdaten die hinzugefügt werden sollen
   */
  public void addProzessDaten( String id, ProzessDaten prozessDaten )
  {
    synchronized (m_prozessDaten)
    {
      m_prozessDaten.put( id, prozessDaten );
    }
  }

  /**
   * Methode zum Entfernen der Prozessdaten eines Prozesses. Der Prozess wird über seine
   * ID identifiziert
   * @param id Id des zum Entfernenden Prozesses
   */
  public void removeProzessDaten ( String id )
  {
    synchronized (m_prozessDaten)
    {
      if (m_prozessDaten.containsKey( id ))
        m_prozessDaten.remove( id );
    }
  }

  /**
   * Methode liefert die Prozessdaten eines Prozesses mit einer bestimmten ID. 
   * @param id Id des zu lesenden Prozesses
   * @return Prozessdaten des Prozesses sofern vorhanden, sonst null.
   */
  public ProzessDaten getProzessDaten ( String id )
  {
    synchronized (m_prozessDaten)
    {
      if (m_prozessDaten.containsKey( id ))
        return m_prozessDaten.get( id );
      else
        return null;
    }
  }

  /**
   * Methode prüft, ob ein Prozess mit einer bestimmten ID bereits versorgt ist.
   * @param id Id des zu prüfenden Prozesses
   * @return Prozess bereits bekannt: true, sonst false
   */
  public boolean isProzessBekannt (String id)
  {
    synchronized (m_prozessDaten)
    {
      return m_prozessDaten.containsKey( id );
    }
  }
  
  /**
   * Methode liefert alle Prozessdaten in Form einer Hashmap. Key = ID eines Prozesses,
   * Value = Prozessdaten eines Prozesses
   * @return alle Prozessdaten
   */
  public HashMap<String,ProzessDaten> getAlleProzessDaten ()
  {
    HashMap<String,ProzessDaten> copy = new HashMap<String, ProzessDaten>();
    
    synchronized (m_prozessDaten)
    {
      Iterator it = m_prozessDaten.entrySet().iterator();
      
      while (it.hasNext())
      {
        Map.Entry me = (Map.Entry)it.next();
        
        String key = (String)me.getKey();
        ProzessDaten value = (ProzessDaten) me.getValue();
        
        copy.put( key, value );
      }
    }
    return copy;
  }
}
