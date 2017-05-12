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

/**
 * Klasse beinhalte Enum Definitionen die für die StartStopp Appklikation
 * benötigt werden
 * @author Dambach Werke GmbH
 */

public class SkriptVewEnums
 {
  /**
   * Enum für die Startart
   */
  public enum E_STARTART 
  {
    /**
     * Automatischer Start
     */
    AUTOMATISCH ("automatisch"),
    /**
     * Manueller Start
     */
    MANUELL ("manuell"),
    /**
     * Zyklischer Start
     */
    INTERVALL ("intervall");
    
    private String m_text;
    
    E_STARTART (String text)
    {
      m_text = text;  
    }
    
    public String getText ()
    {
      return m_text;
    }
    
    public static E_STARTART getEnum (String text)
    {
      for (E_STARTART z : E_STARTART.values () )
        if (z.getText().equalsIgnoreCase( text ))
          return (z);
      
      return null;
    }
  };
  
  /**
   * Enum für das Neustartverhalten
   */
  public enum E_NEUSTART 
  {
    /**
     * Nach Absturz der Applikation wird die Applikation neugestartet.
     */
    Ja   ("Ja"),
    
    /**
     * Nach Absturz der Applikation wird die Applikation nicht neugestartet.
     */
    Nein ("Nein");
    
    private String m_text;
    
    E_NEUSTART (String text)
    {
      m_text = text;  
    }
    
    public String getText ()
    {
      return m_text;
    }
    
    public static E_NEUSTART getEnum (String text)
    {
      for (E_NEUSTART z : E_NEUSTART.values () )
        if (z.getText().equalsIgnoreCase( text ))
          return (z);
      
      return null;
    }
  }

  /**
   * Enum Warteart Startbedinung
   */
      
  public enum E_WARTEART
  {
    /**
     * Es wird auf den Startbeginn der Applikation gewartet.
     */
    BEGINN          ("Beginn"),
    /**
     * Es wird auf den Abschluss der Initialisierung der Applikation gewartet
     */
    INITIALISIERUNG ("Ende");
    
    private String m_text;
    
    E_WARTEART(String text)
    {
      m_text  = text;
    }
  
    public String getText()
    {
      return m_text;
    }
    
    public static E_WARTEART getEnum (String text)
    {
      for (E_WARTEART z : E_WARTEART.values () )
        if (z.getText().equalsIgnoreCase( text ))
          return (z);
      
      return null;
    }
  }
  
  /**
   * Enum Ausgabe in Datei
   */

  public enum E_AUSGABE
  {
    /**
     * Ignorieren der Ausgabe
     */
    IGNORIEREN("ignorieren"),
    
    /**
     * Ausgaben werden in eine eigene Datei geschrieben
     */
    EIGENEDATEI("eigene"),
    
    /**
     * Ausgaben werden in eine gemeinsame Datei geschrieben
     */
    GEMEINSAMEDATEI("gemeinsame");
    
    private String m_text;
    
    E_AUSGABE(String text)
    {
      m_text  = text;
    }
   
    public String getText()
    {
      return m_text;
    }

    public static E_AUSGABE getEnum (String text)
    {
      for (E_AUSGABE z : E_AUSGABE.values () )
        if (z.getText().equalsIgnoreCase( text ))
          return (z);
      
      return null;
    }
  }

  /**
   * Enum Verhalten bei Fehlern beim Stoppen
   */

  public enum E_STOPP_FEHLER_VERHALTEN
  {
    /**
     * Gesamter Stop-Vorgang abbrechen
     */
    ABBRUCH("Abbruch"),
    
    /**
     * Gesamter Stop-Vorgang abbrechen
     */
    STOPP("Stopp"),
    /**
     * Problem ignorieren, nächste Applikation stoppen
     */
    IGNORIEREN("ignorieren");
    
    private String m_text;
    
    E_STOPP_FEHLER_VERHALTEN (String text)
    {
      m_text  = text;
    }
   
    public String getText()
    {
      return m_text;
    }
    
    public static E_STOPP_FEHLER_VERHALTEN getEnum (String text)
    {
      for (E_STOPP_FEHLER_VERHALTEN z : E_STOPP_FEHLER_VERHALTEN.values () )
        if (z.getText().equalsIgnoreCase( text ))
          return (z);
      
      return null;
    }
  }

  /**
   * Enum Verhalten bei Fehlern beim Starten
   */

  public enum E_START_FEHLER_VERHALTEN
  {
    /**
     * Gesamter Start-Vorgang abbrechen und alle gestarteten Applikationen beenden
     */
    BEENDEN("beenden"),
    /**
     * Gesamten Start-Vorgang abbrechen und alle bereits gestarteten Applikationen laufen lassen
     */
    ABBRUCH("Abbruch"),
    /**
     * Problem ignorieren und nächste Applikation starten
     */
    IGNORIEREN("ignorieren");
    
    private String m_text;
    
    E_START_FEHLER_VERHALTEN (String text)
    {
      m_text  = text;
    }
   
    public String getText()
    {
      return m_text;
    }

    public static E_START_FEHLER_VERHALTEN getEnum (String text)
    {
      for (E_START_FEHLER_VERHALTEN z : E_START_FEHLER_VERHALTEN.values () )
        if (z.getText().equalsIgnoreCase( text ))
          return (z);
      
      return null;
    }
  }

  /**
   * Enum für die Zustände der Inkarnationen
   *
   */
  public enum E_ZUSTAND 
  {
    /**
     * Inkarnation wurde angelegt
     */
    ANGELEGT      ("angelegt"),               
    
    /**
     * Inkarnation wartet auf Erfüllung der Startbedingung
     */
    STARTENWARTEN ("warte Startbedingung"),   
    
    /**
     * Inkarnation wird gestartet
     */
    STARTEN       ("starten"),                
    
    /**
     * Inkarnation ist gestartet
     */
    GESTARTET     ("gestartet"), 
    
    /**
     * Inkarnation ist initialisiert (Rückmeldung von Applikation)
     */
    INITIALISIERT ("initialisiert"),          
    
    /**
     * Inkarnation wartet auf Erfüllung der Stoppbedingung
     */
    STOPPENWARTEN ("warte Stoppbedingung"),   
    
    /**
     * Inkarnation wird gestoppt
     */
    STOPPEN       ("stoppen"),
    
    /**
     * Inkarnation ist gestoppt
     */
    GESTOPPT      ("gestoppt"),
    
    /**
     * Inkarnation wartet auf den nächsten zyklischen Neustart
     */
    INTERVALL     ("warte Intervall"),        
    
    /**
     * beim Start der Inkarnation trat ein Fehler auf
     */
    FEHLER        ("Fehler"),                 

    /**
     * Inkarnation ist gelöscht
     */
    GELOESCHT     ("gelöscht");                 

    private String m_text;
    
    E_ZUSTAND (String text)
    {
      m_text  = text;
    }
  
    public String getText()
    {
      return m_text;
    }

    public static E_ZUSTAND getEnum (String text)
    {
      for (E_ZUSTAND z : E_ZUSTAND.values () )
        if (z.getText().equalsIgnoreCase( text ))
          return (z);
      
      return null;
    }
  };

  /**
   * Enum für die Zustände der StartStopp Blöcke
   *
   */
  public enum E_STATUS_STARTSTOPPBLOCK 
  {
    /**
     * StartStopp Block wurde angelegt
     */
    ANGELEGT  ("angelegt"),         

    /**
     * StartStopp ist gestartet
     */
    GESTARTET ("gestartet"),
    
    /**
     * StartStopp ist gestoppt
     */
    GESTOPPT  ("gestoppt"),   
    
    /**
     * beim Start des StartStopp Blocks trat ein Fehler auf
     */
    FEHLER    ("Fehler"),           

    /**
     * StartStopp ist gelöscht
     */
    GELOESCHT  ("gelöscht");   

    private String m_text;
    
    E_STATUS_STARTSTOPPBLOCK (String text)
    {
      m_text  = text;
    }
  
    public String getText()
    {
      return m_text;
    }

    public static E_STATUS_STARTSTOPPBLOCK getEnum (String text)
    {
      for (E_STATUS_STARTSTOPPBLOCK z : E_STATUS_STARTSTOPPBLOCK.values () )
        if (z.getText().equalsIgnoreCase( text ))
          return (z);
      
      return null;
    }
  };

  /**
   * Enum für die Fehler eines StartStopp Blöcke
   *
   */
  
  public enum E_FEHLER_STARTSTOPPBLOCK 
  {
    /**
     * Die Anforderung (atg.startStoppAnfrage) war fehlerfrei
     */
    KEIN                    ("kein Fehler"),                   
    
    /**
     * Aktion nicht bekannt
     */
    FEHLER_AKTION           ("Fehler Aktion"),
    
    /**
     * kein StartStopp Block mit dieser ID vorhanden
     */
    FEHLER_BLOCK_ID         ("Fehler StartStopp-Block ID"),    
    
    /**
     * kein Prozess mit dieser ID vorhanden
     */
    FEHLER_PROZESS_ID       ("Fehler Prozess ID"),
    
    /**
     * übergebene StartStopp Datei nicht gefunden
     */
    FEHLER_DATEI            ("Dateifehler"),     
    
    /**
     * XML Struktur entspricht nit der DTD
     */
    FEHLER_XML              ("Fehler XML-Struktur"),
    
    /**
     * kein Prozess mit diesem Namen vorhanden
     */
    FEHLER_PROZESS          ("Fehler Prozess Name"),           
    
    /**
     * Fehler in der Kombination Aktion - PID - Name
     */
    FEHLER_AKTION_PID_NAME  ("Fehler Aktion-PID-Name");        
    
    private String m_text;
    
    E_FEHLER_STARTSTOPPBLOCK (String text)
    {
      m_text  = text;
    }
  
    public String getText()
    {
      return m_text;
    }

    public static E_FEHLER_STARTSTOPPBLOCK getEnum (String text)
    {
      for (E_FEHLER_STARTSTOPPBLOCK z : E_FEHLER_STARTSTOPPBLOCK.values () )
        if (z.getText().equalsIgnoreCase( text ))
          return (z);
      
      return null;
    }
  };
}