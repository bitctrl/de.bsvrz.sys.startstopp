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

import java.util.Date;

import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_WARTEART;

/**
 * Klasse die eine Startbedingung einer Inkarnation realisiert
 * @author Dambach Werke GmbH
 */
public class StartBedingung
{
  /**
   * Prozess
   */
  private String m_prozessName;
  
  /**
   * Warteart
   */
  private E_WARTEART m_warteArt;
  
  /**
   * Rechner
   */
  private String m_rechnerAlias;
  
  /**
   * Wartezeit
   */
  private long m_warteZeit;
  
  /**
   * ID des Startstoppblocks auf den sich die Rechnerdefinition bezieht
   */
  private String m_startStoppBlockId;

  /**
   * Prozessdaten der Inkarnation, zu der die Startbedingung gehört
   */
  private ProzessDaten m_prozessDaten = null;
  
  /**
   * Konstruktor der Klasse
   * @param prozessDaten Prozessdaten der Instanz, zu der die Startbedingungen gehören
   * @param rechnerAlias Alias des Rechner auf dem der Prozess läuft
   * @param name Name des Prozesses
   * @param warteArt Warteart (Start oder Ende Initialisierung)
   * @param warteZeit Wartezeit in Sekunden
   * @param startStoppBlockId Id des StartStopp Blocks auf den sich der Rechner bezieht
   */
  public StartBedingung(ProzessDaten prozessDaten, String rechnerAlias, String name, E_WARTEART warteArt, long warteZeit, String startStoppBlockId)
  {
    m_prozessDaten      = prozessDaten;
    m_rechnerAlias      = rechnerAlias;
    m_prozessName       = name;
    m_warteArt          = warteArt;
    m_warteZeit         = warteZeit;
    m_startStoppBlockId = startStoppBlockId;
  }

  /**
   * @return liefert die Klassenvariable m_rechnerAlias zurück
   */
  public String getRechnerAlias()
  {
    return m_rechnerAlias;
  }

  /**
    * Methode liefert den Namen des Prozesses entsprechend den Konventionen der Klasse Tools, 
    * Methode bestimmeInkarnationsName() zurück.
   *  @return Name des Prozesses
   */
  public String getProzessName()
  {
    return Tools.bestimmeInkarnationsName( m_prozessName, m_prozessDaten.getSimulationsVariante() );    
  }

  /**
   * @return liefert die Klassenvariable m_warteArt zurück
   */
  public E_WARTEART getWarteArt()
  {
    return m_warteArt;
  }

  /**
   * @return liefert die Klassenvariable m_warteZeit zurück
   */
  public long getWarteZeit()
  {
    return m_warteZeit;
  }

  /**
   * @param warteZeit setzt die Klassenvariable m_warteZeit
   */
  public void setWarteZeit( long warteZeit )
  {
    m_warteZeit = warteZeit;
  }

  /**
   * Methode prüft ob die Startbedingung erfüllt ist
   * @return Startbedingung erfüllt dann true, sonst false
   */
  public boolean isErfuellt()
  {
    ProzessVerwaltung pv = ProzessVerwaltung.getInstanz();
    
    if (getProzessName().equals( "" ))  // Platzhalter
      return true;

    ProzessDaten pd = pv.getProzessDaten( m_startStoppBlockId, m_rechnerAlias, getProzessName() );
    
//    if (pd == null)
//      System.err.println("pd = null " + m_startStoppBlockId + " " +  m_rechnerAlias + " " + getProzessName() );
    
    if (pd != null)
    {
      if (m_warteArt == E_WARTEART.BEGINN)
      {
        if (pd.isGestartet() || pd.isInitialisiert())
        {
          // Wartezeit seit dem Start der Applikation berücksichtigen
          
          Date d = getFruehesterStartZeitpunkt();
          if (d != null)
          {
//            System.out.println(d.toLocaleString() + " " + (new Date().toLocaleString()));
            if (d.compareTo( new Date())  <= 0 )
              return true;
          }
        }

//        System.out.println("false");
      }

      if (m_warteArt == E_WARTEART.INITIALISIERUNG)
      {
        if (pd.isInitialisiert())
        {
          // Wartezeit seit dem Start der Applikation berücksichtigen
          
          Date d = getFruehesterStartZeitpunkt();
          if (d != null)
          {
//            System.out.println(d.toLocaleString() + " " + (new Date().toLocaleString()));
            if (d.compareTo( new Date())  <= 0 )
              return true;
          }
        }
        
//        System.out.println("false");
      }
    }
    
    return false;
  }
  
  /**
   * Methode liefert den frühesten Startzeitpunkt der Inkarantion. Dieser Zeitpunkt bestimmt
   * sich aus dem letzten Startzeitpunkt der Inkaranation + der Wartezeit die in der Klassenvariablen
   * m_wartezeit definiert ist.
   * @return frühesten Startzeitpunkt, im Fehlerfall null
   */
  public Date getFruehesterStartZeitpunkt ()
  {
    Date d = null;

    ProzessVerwaltung pv = ProzessVerwaltung.getInstanz();
    ProzessDaten pd = pv.getProzessDaten(m_startStoppBlockId, m_rechnerAlias, getProzessName());

    if (pd != null)
    {
      if (m_warteArt == E_WARTEART.INITIALISIERUNG)
      {
        if (pd.isInitialisiert())
        {
          Date start = pd.getLetzteInitialisierung();
          
          if (start != null)
          {
            d = new Date();
            
            d.setTime(start.getTime() + (m_warteZeit * 1000));
          }
        }
      }
      else
      {
        if (pd.isGestartet() || pd.isInitialisiert())
        {
          Date start = pd.getLetzterStart();
          
          if (start != null)
          {
            d = new Date();
            
            d.setTime(start.getTime() + (m_warteZeit * 1000));
          }
        }
      }
    }

    return d;
  }
}
