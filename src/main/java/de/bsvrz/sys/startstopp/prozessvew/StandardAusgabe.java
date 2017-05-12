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

import de.bsvrz.sys.startstopp.skriptvew.GlobaleDaten;
import de.bsvrz.sys.startstopp.skriptvew.ProtokollDatei;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_AUSGABE;

/**
 * Klasse zum Realisieren der Einstellungen für die Ausgabe der Standardausgaben eines
 * Prozessen
 * @author Dambach Werke GmbH
 */
  public class StandardAusgabe
  {
    /**
     * ID des StartStoppBlocks dessen globale Definition verwendet werden soll
     */
    private String m_startStoppBlockId;
    
    /** 
     * Option
     */
    private E_AUSGABE m_option;
    
    /**
     * Alias der Datei
     */
    private String m_dateiAlias;

    /**
     * Konstruktor der Klasse
     * @param startStoppBlockId ID des StartStoppBlocks dessen globale Definition verwendet werden soll
     * @param option Option
     * @param alias Alias
     */
    public StandardAusgabe (String startStoppBlockId, E_AUSGABE option, String alias)
    {
      m_startStoppBlockId = startStoppBlockId;
      m_option            = option;
      m_dateiAlias        = alias;
    }
    
    /**
     * @return liefert die Klassenvariable m_dateiAlias zurück
     */
    public String getDateiAlias()
    {
      return m_dateiAlias;
    }

    /**
     * @param dateiAlias setzt die Klassenvariable m_dateiAlias
     */
    public void setDateiAlias( String dateiAlias )
    {
      m_dateiAlias = dateiAlias;
    }

    /**
     * @return liefert die Klassenvariable m_option zurück
     */
    public E_AUSGABE getOption()
    {
      return m_option;
    }

    /**
     * @param option setzt die Klassenvariable m_option
     */
    public void setOption( E_AUSGABE option )
    {
      m_option = option;
    }

    /**
     * @return liefert die Klassenvariable m_startStoppBlockId zurück
     */
    public String getStartStoppBlockId()
    {
      return m_startStoppBlockId;
    }

    /**
     * @param startStoppBlockId setzt die Klassenvariable m_startStoppBlockId
     */
    public void setStartStoppBlockId( String startStoppBlockId )
    {
      m_startStoppBlockId = startStoppBlockId;
    }

    /**
     * Methode liefert den realen Namen der Datei. Hierbei wird die Aliasbezeichnung der Datei mit Hilfe
     * der globalen Daten aufgelöst.
     * @return realer Name der Datei.
     */
    public String getDateiName()
    {
      ProtokollDatei pd = GlobaleDaten.getInstanz().getProtokollDateiByAlias( m_startStoppBlockId, m_dateiAlias );
      if (pd != null)
        return pd.getNameDatei();
      
      return null;
    }

    /**
     * Methode liefert die Dateigrösse der Datei in MB.
     * @return Dateigrösse in MB
     */
    public long getMaxGroesse()
    {
      ProtokollDatei pd = GlobaleDaten.getInstanz().getProtokollDateiByAlias( m_startStoppBlockId, m_dateiAlias );
      if (pd != null)
        return pd.getGroesse();
      
      return 0;
    }
  } 
