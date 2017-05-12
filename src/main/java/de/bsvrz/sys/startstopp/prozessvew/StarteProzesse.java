package de.bsvrz.sys.startstopp.prozessvew;

import java.io.IOException;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.startstopp.skriptvew.StartStoppApp;

/**
 * Klasse die das Starten eines Prozesses ermöglicht. Die Klasse wird als Singelton ausgelegt,
 * das Starten selbst wird synchronized ausgeführt. Der Grund liegt darin dass nicht "gleichzeitig"
 * mehrere Prozesse gestartet werden. Grund dafür wiederung ist das Problem, dass unter Windows
 * kein Zugang zu Prozessids des Betriebssystem möglich ist. Daher wird vor dem Starten eines
 * Prozesses unter Windows eine Liste der aktuellen Prozesse erstellt und nach dem Starten überprüft
 * welcher Prozess neu hinzugekommen ist.
 * 
 * @version $Revision: 1.10 $ / $Date: 2011/10/27 14:24:31 $ / ($Author: Drapp $)
 *
 * @author Dambach-Werke GmbH
 * @author Matthias Obert
 */
public class StarteProzesse
{
  /**
   * Debug
   */
  private static final Debug logger = Debug.getLogger();

  /**
   * Methode zum Lesen der einzigen Instanz der Klasse
   * @return einzige Instanz der Klasse
   */
  public static StarteProzesse getInstanz()
  {
    return Inner.INSTANCE;
  }

  /**
   * Innere Klasse zum Sicherstellen, dass wirklich nur eine Instanz der Klasse
   * gebildet wird
   * @author Dambach-Werke
   */
  public static class Inner
  {
    private static StarteProzesse INSTANCE = new StarteProzesse();
  }

  /**
   * Parameterloser Konstruktor der Klasse
   */
  private StarteProzesse()
  {
    
  }
  
  /**
   * Methode zum Starten eines Prozesses
   * @param pd Prozessdaten des Prozesses
   * @return Instanz des gestarteten Prozesses, im Fehlerfall null
   */
  public synchronized Process start (ProzessDaten pd)
  {
    if (StartStoppApp.isStartStoppWirdBeendet())
      return null;
    
    String aufruf = pd.getAufruf();
    
    Logbuch.getInstanz().schreibe( pd.getName(), aufruf );
    
    BetriebssystemProzess betriebssystemProzess = pd.getBetriebssystemProzess();
  
    betriebssystemProzess.iniPidBestimmung();
  
    Process proc = null;
    
    try
    {
      // Aufruf selber in String-Feld zerlegen, da sonst ein Fehler bei Strings in Anführungszeichen auftritt 
      
      String[] cmdarray = StringTokenizerQuotedStrings.parse(aufruf);

      String[] env = {"LANG=de_DE@euro"};

      if (Tools.isWindows())
      {
        proc = Runtime.getRuntime().exec(cmdarray, null, null);
      }
      else
      {
        proc = Runtime.getRuntime().exec(cmdarray, env, null);
      }
    }
    catch ( IOException e )
    {
      proc = null;
    
      logger.error("Fehler beim Starten der Inkarnation " + pd.getName());
    }
    
    betriebssystemProzess.bestimmePid();
    
    return proc;
  }
}