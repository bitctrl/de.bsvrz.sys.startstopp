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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import de.bsvrz.dav.daf.main.ApplicationCloseActionHandler;
import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.CommunicationError;
import de.bsvrz.dav.daf.main.ConnectionException;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.InconsistentLoginException;
import de.bsvrz.dav.daf.main.MissingParameterException;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.Data.Array;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.ConfigurationObjectType;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageCauser;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;
import de.bsvrz.sys.funclib.operatingMessage.MessageSender;
import de.bsvrz.sys.funclib.operatingMessage.MessageState;
import de.bsvrz.sys.funclib.operatingMessage.MessageType;
import de.bsvrz.sys.startstopp.skriptvew.GlobalEreignis;
import de.bsvrz.sys.startstopp.skriptvew.GlobaleDaten;
import de.bsvrz.sys.startstopp.skriptvew.IGlobaleDatenListener;
import de.bsvrz.sys.startstopp.skriptvew.RechnerGlobal;
import de.bsvrz.sys.startstopp.skriptvew.StartStoppApp;
import de.bsvrz.sys.startstopp.skriptvew.StartStoppBlock;
import de.bsvrz.sys.startstopp.skriptvew.StartStoppVerwaltung;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_FEHLER_STARTSTOPPBLOCK;
import de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_ZUSTAND;
/**
 * Klasse zur Abhandlung der Kommunikation mit dem DaV. Die Klasse wird als Singelton
 * ausgelegt.
 * @author Dambach Werke GmbH
 */
public class DaVKommunikation implements  ClientReceiverInterface, ClientSenderInterface, Serializable
{
  private static final long serialVersionUID = 1L;

  /**
   * Debug
   */
  private static final Debug logger = Debug.getLogger();

  /**
   * Die ApplikationsKennung wird hier gespeichert. Sie besteht aus den Aufrufargumenten, dem Klassennamen (wie beim
   * Debug) und der Pid des lokalen Verantwortlichen.
   */
  private static StringBuilder _applicationLabel;

  /**
   * Der Name der Applikation, die den StandardApplicationRunner nutzt.
   */
  private static String _applicationName = "";

  /**
   * Debug Möglichkeit
   */  
  private final boolean _debugAnmelde = false;
  
  /**
   * Debug Möglichkeit
   */  
  private final boolean _debugFertigMeldung = false;
  
  /**
   * Liste mit den IDs der am Dav angemeldeten Applikationen
   */
  private  HashMap<Long, Integer> m_listeDavApplikationen = new HashMap<Long, Integer> ();

  /**
   * Liste mit der Zuordnung IDs zu Inkarnationsnamen der am Dav angemeldeten Applikationen
   */
  private  HashMap<Long, String> m_zuordnungIdZuInkarnationen = new HashMap<Long, String> ();

  /**
   * Kennung ob StartStopp mit einem DaV verbunden ist
   */
  static boolean m_verbunden;
  
  /**
   * Kennung ob StartStopp an einem DaV angemeldet ist
   */
  public boolean m_angemeldet=false;
  
  /**
   * Verbindung zum DaV
   */
  public ClientDavInterface m_connection;

  /**
   * Verbindung zum DaV (Grundlage für alle folgenden Verbindungen)
   */
  public ClientDavInterface m_connectionFuerImmer;
  
  /**
   * Pid des Rechners
   */
  private String m_pidRechner;
  
  /**
   * Merker ob die Attributgruppe "atg.prozessInfo" gesendet werden darf
   */
  private boolean m_sendeProzessInfo    = true;
  
  /**
   * Merker ob die Attributgruppe "atg.startStoppInfo" gesendet werden darf
   */
  private boolean m_sendeStartStoppInfo = true;
  
  /**
   * Liste der Applikationen, bei denen noch auf die Attributgruppe "atg.applikationsFertigmeldung"
   * gewartet wird.
   */
  private List<Long> m_warteApplikationsFertigmeldung = new ArrayList<Long>();
  /**
   * Liste mit den angemeldeten Sendern.
   */
  private Vector<AngemeldeterSender> m_angemeldeteSender = new Vector<AngemeldeterSender>();
  /**
   * Liste mit den angemeldeten Empfängern.
   */
  private Vector<AngemeldeterEmpfaenger> m_angemeldeteEmpfaenger = new Vector<AngemeldeterEmpfaenger>();

  /**
   * Privater Konstruktor der Klasse (Singelton)
   */
  private DaVKommunikation()
  {
    try
		{
			m_connectionFuerImmer = new ClientDavConnection();
		} 
    catch (MissingParameterException e)
		{
			// Da keine Parameter übergeben werden, kann die Exception nicht auftreten
		}
    
    m_verbunden = false;
  }

  /**
   * Geschachtelte Innere Klasse (wird verwendet um zu garantieren, dass
   * wirklich nur eine Instanz der Klasse angelegt wird).
   * @author Dambach Werke GmbH
   *
   */
  private static class Inner
  {
    private static DaVKommunikation INSTANCE = new DaVKommunikation ();
  }

  /**
   * Methode liefert die einzige Instanz der Klasse
   * @return einzige Instanz der Klasse
   */
  public static DaVKommunikation getInstanz()
  {
    return Inner.INSTANCE;
  }

  /**
   * Methode baut Verbindung zum DaV auf
   * @return true: Verbindung zum DaV steht, false: Verbindung zum DaV steht nicht
   */
  public boolean baueVerbindungDaVAuf ()
  {
    m_connection = verbindeMitDaV();

    m_verbunden = (m_connection != null);
    
    if (!m_verbunden)
      logger.info("Keine Verbindung zum DaV aufgebaut");
    else
      logger.info("Verbindung zum DaV aufgebaut");
    
    return m_verbunden;
  }

  /**
   * Methode stellt die Verbindung zum DaV her. IP-Adresse und Port
   * wird der Klasse GlobaleDaten entnommen.
   * @return Verbindung zum DaV
   */
  private ClientDavInterface verbindeMitDaV ()
  {
    GlobaleDaten glob = GlobaleDaten.getInstanz();
    
    logger.info( "Verbinde mit DaV");

    String ip = glob.getDavAdresse().toString();
    int port  = glob.getDavPort();
    
    String davAddress[] = { ip, "" + port };

    if (davAddress == null)
    {
      logger.error( "Fehler beim Einlesen der DaV Adresse");
      return null;
    }

    ClientDavInterface connection = null;

    try
		{
			connection = new ClientDavConnection();
		} 
    catch (MissingParameterException e1)
		{
			// Da keine Parameter übergeben werden, kann die Exception nicht auftreten

    	e1.printStackTrace();
		}

    logger.info(davAddress [0] + " " + davAddress [1]);

    connection.getClientDavParameters().setDavCommunicationAddress(davAddress [0] );
    connection.getClientDavParameters().setDavCommunicationSubAddress(Integer.parseInt( davAddress [1] ) );

    logger.info( "Verbinde mit DaV " + davAddress [0] + " " + davAddress [1] );

    connection.getClientDavParameters().setApplicationName( "StartStopp" );
    try
    {
      logger.config("********* vor Connect");
      
      connection.connect();

      logger.config("********* nach Connect");
    }
    catch (CommunicationError e)
    {
      logger.info("Kommunikationsfehler");
      
      e.printStackTrace();
      
      return null;
    }
    catch (ConnectionException e)
    {
      logger.info("ConnectionException");

//    e.printStackTrace();
      
      return null;
    }

    String benutzer = glob.getDavBenutzer().toString();
    String passwort = glob.getDavPasswort().toString();

    try
    {
      logger.config("********* vor Login");
      
      connection.login(benutzer, passwort);

      logger.config("********* nach Login");
    }
    catch (InconsistentLoginException e)
    {
      logger.config("********* Login ", exceptionMeldungBestimmen(e));

      StartStoppApp.beendeStartStoppWegenFehler("Falsche Zugangsdaten DaV");      
    }
    catch (CommunicationError e)
    {
      logger.config("********* Login ", exceptionMeldungBestimmen(e));

      // leeren CloseHandler setzen, damit das Disconnect nicht zur Programmbeendigung führt
      connection.setCloseHandler(new ApplicationCloseActionHandler() 
      {
        public void close(String error)
        {
        }
      });
      
      // Verbindung beenden, da bei erneutem Versuch des Login auch eine neue Verbindung erstellt wird
      connection.disconnect(false, "CommunicationError aufgetreten");

      return null;
    }
    catch (RuntimeException e)
    {
      logger.config("********* Login ", exceptionMeldungBestimmen(e));

      // leeren CloseHandler setzen, damit das Disconnect nicht zur Programmbeendigung führt
      connection.setCloseHandler(new ApplicationCloseActionHandler() 
      {
        public void close(String error)
        {
        }
      });
      
      // Verbindung beenden, da bei erneutem Versuch des Login auch eine neue Verbindung erstellt wird
      connection.disconnect(false, "RuntimeException aufgetreten");

      return null;
    }
    catch (Exception e)
    {
      logger.config("********* Login ", exceptionMeldungBestimmen(e));

      // leeren CloseHandler setzen, damit das Disconnect nicht zur Programmbeendigung führt
      connection.setCloseHandler(new ApplicationCloseActionHandler() 
      {
        public void close(String error)
        {
        }
      });
      
      // Verbindung beenden, da bei erneutem Versuch des Login auch eine neue Verbindung erstellt wird
      connection.disconnect(false, "Exception aufgetreten");

      return null;
    }
    
    if (connection != null)
    {
      logger.info("Mit DaV verbunden");
    }
    
    return connection;
  }
  
  /**
   * Informationen einer Exception in einen String packen.<br>
   * Es werden die Exception-Klasse, die Exception-Meldung und der StackTrace verarbeitet.<br>
   * 
   * @param exception zu verarbeitende Exception.
   * @return Informationen der verarbeiteten Exception. 
   */
  private String exceptionMeldungBestimmen(Exception exception)
  {
    String SEP = System.getProperty("line.separator");
    String message = exception.getClass().getName() + " " + exception.getMessage();
    
    StackTraceElement[] stack = exception.getStackTrace();
    
    for (int i = 0; i < stack.length; i++)
    {
      message += (SEP + stack[i]);
    }
    
    return message;
  }
  
  /**
   * Method trennt die Verbindung zum DaV
   */
  public void trenneDaV ()
  {
    if (m_verbunden)
    {
      logger.info("Verbindung zum DaV trennen");
  
      try
      {
        m_connection.disconnect(false, null);
      }
      catch (Exception e) 
      {
        e.printStackTrace();
      }
      
      m_verbunden = false;
  
      m_connection = null;
    }
  }
  
  /**
   * Methode meldet sich am DaV an zum:
   * - Senden "atg.prozessInfo"  "asp.zustand"
   * - Lesen "atg.startStoppAnfrage", "asp.anfrage"
   * - Lesen "atg.prozessInfo", "asp.zustand" (für jeden Rechner der in der Klasse "GlobaleDaten" definiert ist
   */
  public void anmeldeDaV () 
  {
    GlobaleDaten glob = GlobaleDaten.getInstanz();
    
    // Anmelden zum Senden der Betriebsmeldungen
    
    _applicationLabel = StartStoppApp._applicationLabel;

    if (m_connection != null) 
    {
      _applicationLabel.append(m_connection.getLocalConfigurationAuthority().getPid()); // ApplikationsKennung
 
      MessageSender.getInstance().init(m_connection, _applicationName, _applicationLabel.toString());
    } 
    else 
    {
      throw new RuntimeException("Die Verbindung zum Datenverteiler konnte nicht hergestellt werden.");
    }

    // Adresse des Rechners feststellen unter dem die StartStopp Informationen publiziert werden sollen

    String host = HostRechner.getInstanz().getHostName();
    
    if (host != null)
    {
      // Pid dieses Rechners feststellen

      m_pidRechner = HostRechner.getInstanz().getPidRechner();

//      m_pidRechner = bestimmePidRechnerByHost( host );
      
      if (m_pidRechner != null)
      {
        //---------------------------------------------------------------------------------------
        // Anmelden zum Versenden der Prozessinformationen
        //---------------------------------------------------------------------------------------

        if (!StartStoppApp.isStartStoppWirdBeendet())
          anmeldeSendeDaV ( m_pidRechner, "atg.prozessInfo", "asp.zustand", SenderRole.source() );

        if (!StartStoppApp.isStartStoppWirdBeendet())
          anmeldeSendeDaV ( m_pidRechner, "atg.startStoppInfo", "asp.zustand", SenderRole.source() );
        
        if (!StartStoppApp.isStartStoppWirdBeendet())
          anmeldeLeseDaV ( m_pidRechner, "atg.startStoppAnfrage", "asp.anfrage", ReceiverRole.drain());
        
      } // if (m_pidRechner != null)
      
      //---------------------------------------------------------------------------------------
      // Anmelden zum Lesen der Prozessinformationen anderer Rechner
      //---------------------------------------------------------------------------------------
      
      anmeldeLeseRechner();
      
      // Listener anmelden für den Fall, dass sich die Liste der Rechner geändert hat
      
      glob.addListener( new IGlobaleDatenListener () 
      {
        public void exec( GlobalEreignis e )
        {
          anmeldeLeseRechner();
        }
      });
      
      //---------------------------------------------------------------------------------------
      // Anmelden zum Lesen der Initialisierungsmeldungen der Applikationen
      //---------------------------------------------------------------------------------------

//      SystemObject dav = m_connection.getDataModel().getObject("dav.nw.leverkusen.nba.koko");
      SystemObject dav = m_connection.getLocalDav(); 

      if (dav != null)
      {
        AttributeGroup atg = m_connection.getDataModel().getAttributeGroup("atg.angemeldeteApplikationen");
        Aspect asp         = m_connection.getDataModel().getAspect("asp.standard");
      
        DataDescription dd = new DataDescription(atg, asp);
      
        logger.config("********* subscribeReceiver an " + dav.getPid() + " dd " + dd);
        
        m_connection.subscribeReceiver(this, dav, dd, ReceiveOptions.normal(), ReceiverRole.receiver());

        logger.config("--------- subscribeReceiver an " + dav.getPid() + " dd " + dd);

        synchronized (m_angemeldeteEmpfaenger)
        {
          m_angemeldeteEmpfaenger.add(new AngemeldeterEmpfaenger(this, dav, dd));
        }
      }
      
      //---------------------------------------------------------------------------------------
      // Anmelden zum Lesen der Stati der USV
      //---------------------------------------------------------------------------------------
      
      if (!StartStoppApp.isStartStoppWirdBeendet())
      {
        String pidUsv = glob.getUsvPid();
        
        if (pidUsv != "" && pidUsv != null)
        {
          if (!pruefeUsv(pidUsv))
          {
            String buffer = "USV mit Pid \"" + pidUsv + "\" nicht bekannt !";

            StartStoppApp.beendeStartStoppWegenFehler(buffer);
          }
          else
          {
            anmeldeLeseDaV (pidUsv, "atg.usvZustandKritisch", "asp.zustand", ReceiverRole.receiver());
          }
        }
      }
      
      m_angemeldet = true;
    }
  }

  /**
   * Methode zum Anmelden der Attributgruppe "atg.prozessInfo" am DaV. In dieser Methode
   * meldet sich die StartStopp Applikation für jeden im globalen Teil der startStopp Datei
   * definierten Rechner am DaV an. Die Methode muss immer dann aufgerufen werden, wenn
   * sich die Rechnerliste ändert (z.B. durch empfangene StartStopp Blöcke über den DaV). 
   */
  private void anmeldeLeseRechner ()
  {
    GlobaleDaten glob = GlobaleDaten.getInstanz();
    
    // Anmelden zum Lesen der Prozessinformationen anderer Rechner
    
    List<RechnerGlobal> rechner = glob.getAlleRechner();
    
    for (int i=0; i<rechner.size(); ++i)
    {
      String ip  = rechner.get( i ).getTcpAdresse();
      String pidRechner = bestimmePidRechnerByIp( ip );
      
      if (pidRechner != null)
      {
        anmeldeLeseDaV (pidRechner, "atg.prozessInfo", "asp.zustand", ReceiverRole.receiver());
        anmeldeLeseDaV (pidRechner, "atg.startStoppInfo", "asp.zustand", ReceiverRole.receiver());
      }
    }
  }

  /**
   * Bestimmt die Pid eines Rechners aufgrund des übergebenen Rechnernamens. 
   * @param host Rechnername
   * @return Pid oder null im Fehlerfall
   */
  public String bestimmePidRechnerByHost ( String host )
  {
    return bestimmePidRechner( 0, host );
  }
  
  /**
   * Bestimmt die Pid eines Rechners aufgrund der übergebenen TCP/IP-Adresse 
   * @param ip TCP/IP Adresse
   * @return Pid oder null im Fehlerfall
   */
  public String bestimmePidRechnerByIp ( String ip )
  {
    return bestimmePidRechner( 1, ip );
  }

  /**
   * Methode bestimmt die PID zu einem Rechner.
   * @param art 0 - Bestimmen der Pid auf Basis des Rechnernamens
   * 1 - Bestimmen der Pid auf Basis der TCP/IP Adresse 
   * @param value Rechnername oder IP-Adresse 
   * @return Pid des Rechners oder wirft im Fehlerfall eine IllegalArgument Exception
   */
  private String bestimmePidRechner( int art,  String value )
  {
    SystemObjectType typeSysObj = null;
    
    DataModel dataModel = m_connection.getDataModel();

    typeSysObj = dataModel.getType("typ.rechner");
      
    if (typeSysObj == null)
      return null;
        
    if (typeSysObj.isConfigurating())
      typeSysObj = (ConfigurationObjectType) typeSysObj;
    else
      typeSysObj = (DynamicObjectType) typeSysObj;
      
    // Elemente dieses Systemobjekts einlesen
      
    List typobjekte = typeSysObj.getElements();
      
    Iterator iterator_Typen = typobjekte.iterator();
    
    while (iterator_Typen.hasNext()) 
    {
      SystemObject sysObj = (SystemObject) iterator_Typen.next();

      if (sysObj != null)
      {
        Data data = sysObj.getConfigurationData( dataModel.getAttributeGroup( "atg.rechnerInformation"));
      
        if (data == null)
          logger.error("Für Rechner \"" + sysObj.getName() + "\" ist Attributgruppe \"atg.rechnerInformation\" nicht definiert");
        else
        {
          if (art == 0)  // Bestimmen über Hostname
          {
            if (value.equalsIgnoreCase( data.getItem( "Name" ).valueToString()))
              return sysObj.getPid();
          }

          if (art == 1)  // Bestimmen über IP-Adresse
          {
            if (value.equalsIgnoreCase( data.getItem( "TCPIP" ).valueToString()))
              return sysObj.getPid();
          }
        }
      }
    }

    StartStoppApp.beendeStartStoppWegenFehler ("Konfigurationsobjekt Rechner \"" + value + "\" nicht gefunden !");
    
    return null;
  }

  /**
   * Methode prüft ob eine übergeben Pid eine gültige Pid eines Objekts vom Typ Rechner ist
   * @param pidRechner zu prüfende Pid
   * @return Pid gültig
   */
  public boolean isPidRechnerGueltig ( String pidRechner )
  {
    SystemObjectType typeSysObj = null;
    
    DataModel dataModel = m_connection.getDataModel();

    typeSysObj = dataModel.getType("typ.rechner");
      
    if (typeSysObj == null)
      return false;
        
    if (typeSysObj.isConfigurating())
      typeSysObj = (ConfigurationObjectType) typeSysObj;
    else
      typeSysObj = (DynamicObjectType) typeSysObj;
      
    // Elemente dieses Systemobjekts einlesen
      
    List typobjekte = typeSysObj.getElements();
      
    Iterator iterator_Typen = typobjekte.iterator();
    
    while (iterator_Typen.hasNext()) 
    {
      SystemObject sysObj = (SystemObject) iterator_Typen.next();

      if (sysObj != null)
      {
        Data data = sysObj.getConfigurationData( dataModel.getAttributeGroup( "atg.rechnerInformation"));
      
        if (data == null)
          logger.error("Für Rechner \"" + sysObj.getName() + "\" ist Attributgruppe \"atg.rechnerInformation\" nicht definiert");
        else
        {
          if (sysObj.getPid() != null)
            if (sysObj.getPid().equals( pidRechner ))
              return true;
        }
      }
    }

    return false;
  }

  /**
   * Methode bestimmt die IP-Adresse eines Rechners, dessen PID übergeben wird.
   * @param pidRechner Pid des Rechners 
   * @return TCP/IP Adresse als String
   */
  private String bestimmeIpRechner( String pidRechner )
  {
    SystemObjectType typeSysObj = null;
    
    DataModel dataModel = m_connection.getDataModel();

    typeSysObj = dataModel.getType("typ.rechner");
      
    if (typeSysObj == null)
      return null;
        
    if (typeSysObj.isConfigurating())
      typeSysObj = (ConfigurationObjectType) typeSysObj;
    else
      typeSysObj = (DynamicObjectType) typeSysObj;
      
    // Elemente dieses Systemobjekts einlesen
      
    List typobjekte = typeSysObj.getElements();
      
    Iterator iterator_Typen = typobjekte.iterator();
    
    while (iterator_Typen.hasNext()) 
    {
      SystemObject sysObj = (SystemObject) iterator_Typen.next();

      if (sysObj != null)
      {
        if (sysObj.getPid().equals( pidRechner ))
        {
          Data data = sysObj.getConfigurationData( dataModel.getAttributeGroup( "atg.rechnerInformation"));
      
          if (data == null)
            logger.error("Für Rechner \"" + sysObj.getName() + "\" ist Attributgruppe \"atg.rechnerInformation\" nicht definiert");
          else
            return data.getItem( "TCPIP" ).valueToString();
        }
      }
    }

    return null;
  }

  /**
   * Methode die das Objekt mit der PID objPid beim Datenverteiler anmeldet zum
   * Senden der Attibutgruppe atgPid unter dem Aspekt aspPid. 
   * @param objPid Pid des Objekts 
   * @param atgPid Pid der Attributgruppe
   * @param aspPid Pid des Aspekts
   */

  public DataDescription anmeldeSendeDaV (String objPid, String atgPid, String aspPid, SenderRole role)
  {
    String buffer = "Anmelden am DaV (Senden): " + objPid + " " + atgPid + "  " + aspPid + " " + role.toString();
    
    if (_debugAnmelde)
      System.out.println(buffer);
    
    logger.info( buffer );
    
    ClientDavInterface verb = m_connection;
    
    AttributeGroup atg = verb.getDataModel().getAttributeGroup(atgPid);
    Aspect asp = verb.getDataModel().getAspect(aspPid);

    DataDescription dd = new DataDescription(atg, asp);

    SystemObject sysObj = verb.getDataModel().getObject(  objPid); 

    try
    {
      verb.subscribeSender( this, sysObj, dd, SenderRole.source());
      
      synchronized (m_angemeldeteSender)
      {
        m_angemeldeteSender.add(new AngemeldeterSender(this, sysObj, dd));
      }
    }
    catch ( OneSubscriptionPerSendData e1 )
    {
      e1.printStackTrace();
    }
    
    return dd;
  } 

  /**
   * Methode die das Objekt mit der ID objId beim Datenverteiler anmeldet zum
   * Senden der Attibutgruppe atgPid unter dem Aspekt aspPid. 
   * @param objId Id des Objekts 
   * @param atgPid Pid der Attributgruppe
   * @param aspPid Pid des Aspekts
   */

  public DataDescription anmeldeSendeDaV (long objId, String atgPid, String aspPid, SenderRole role)
  {
    String buffer = "Anmelden am DaV (Senden): " + objId + " " + atgPid + "  " + aspPid + " " + role.toString();
    
    if (_debugAnmelde)
      System.out.println(buffer);
    
    logger.info( buffer );
    
    ClientDavInterface verb = m_connection;
    
    AttributeGroup atg = verb.getDataModel().getAttributeGroup(atgPid);
    Aspect asp = verb.getDataModel().getAspect(aspPid);

    DataDescription dd = new DataDescription(atg, asp);

    SystemObject sysObj = verb.getDataModel().getObject(  objId ); 

    try
    {
      verb.subscribeSender( this, sysObj, dd, SenderRole.source());
      
      synchronized (m_angemeldeteSender)
      {
        m_angemeldeteSender.add(new AngemeldeterSender(this, sysObj, dd));
      }
    }
    catch ( OneSubscriptionPerSendData e1 )
    {
      logger.error(e1.getMessage());
//      e1.printStackTrace();
    }
    
    return dd;
  } 

  /**
   * Methode die das Objekt objId beim Datenverteiler abmeldet zum
   * Senden der Attibutgruppe atgPid unter dem Aspekt aspPid.
   * @param objId Id des Objekts 
   * @param atgPid Attributgruppe die abgemeldet werden soll
   * @param aspPid Apekt der abgemeldet werden soll
   */
  public void abmeldeSendeDaV (long objId, String atgPid, String aspPid)
  {
    String buffer = "Abmelden am DaV (Senden): " + objId + " " + atgPid + "  " + aspPid;

    if (_debugAnmelde)
      System.out.println(buffer);
    
    logger.info( buffer);

    AttributeGroup atg = m_connection.getDataModel().getAttributeGroup(atgPid);
    Aspect asp = m_connection.getDataModel().getAspect(aspPid);

    DataDescription dd = new DataDescription(atg, asp);
      
    SystemObject sysObj = m_connection.getDataModel().getObject(objId); 

    m_connection.unsubscribeSender(this, sysObj, dd);
  }

  /**
   * Methode die das Objekt objId beim Datenverteiler abmeldet zum
   * Lesen der Attibutgruppe atgPid unter dem Aspekt aspPid.
   * 
   * @param objId Id des Objekts 
   * @param atgPid Attributgruppe die abgemeldet werden soll
   * @param aspPid Apekt der abgemeldet werden soll
   */
  public void abmeldeLeseDaV(long objId, String atgPid, String aspPid)
  {
  	SystemObject sysObj = m_connection.getDataModel().getObject(objId); 

    if (sysObj == null) // Datenempfang nur abmelden, wenn SystemObjekt noch vorhanden ist
    {
    	return;
    }
    
    String buffer = "Abmelden am DaV (Lesen): " + objId + " " + atgPid + "  " + aspPid;

    if (_debugAnmelde)
      System.out.println(buffer);
    
    logger.info(buffer);

    AttributeGroup atg = m_connection.getDataModel().getAttributeGroup(atgPid);
    Aspect asp = m_connection.getDataModel().getAspect(aspPid);

    DataDescription dd = new DataDescription(atg, asp);
      
  	m_connection.unsubscribeReceiver(this, sysObj, dd);
  }

  /**
   * Methode die das Objekt mit der ID objId beim Datenverteiler anmeldet zum
   * Lesen der Attibutgruppe atgPid unter dem Aspekt aspPid. 
   * @param objId Id des Objekts 
   * @param atgPid Attributgruppe die angemeldet werden soll
   * @param aspPid Apekt der angemeldet werden soll
   * @param role Rolle des Empfängers (siehe stauma.dav.clientside.ReceiveOptions)
   */
  private void anmeldeLeseDaV (long objId, String atgPid, String aspPid, ReceiverRole role)
  {
    ClientDavInterface verb = m_connection;
    
    AttributeGroup atg = verb.getDataModel().getAttributeGroup(atgPid);
    Aspect asp = verb.getDataModel().getAspect(aspPid);

    DataDescription dd = new DataDescription(atg, asp);

    SystemObject sysObj = verb.getDataModel().getObject(  objId ); 

    String buffer = "Anmelden am DaV (Lesen): " + objId + " " + atgPid + "  " + aspPid + " " + role.toString();
    
    if (_debugAnmelde)
      System.out.println(buffer);
    
    logger.info( buffer );

    m_connection.subscribeReceiver(this, sysObj, dd, ReceiveOptions.normal(), role);
    
    synchronized (m_angemeldeteEmpfaenger)
    {
      m_angemeldeteEmpfaenger.add(new AngemeldeterEmpfaenger(this, sysObj, dd));
    }
  }

  /**
   * Methode die das Objekt mit der PID objPid beim Datenverteiler anmeldet zum
   * Lesen der Attibutgruppe atgPid unter dem Aspekt aspPid. 
   * @param objPid Pid des Objekts 
   * @param atgPid Attributgruppe die angemeldet werden soll
   * @param aspPid Apekt der angemeldet werden soll
   * @param role Rolle des Empfängers (siehe stauma.dav.clientside.ReceiveOptions)
   */
  private void anmeldeLeseDaV (String objPid, String atgPid, String aspPid, ReceiverRole role)
  {
    String buffer = "Anmelden am DaV (Lesen): " + objPid + " " + atgPid + "  " + aspPid;
    
    if (_debugAnmelde)
      System.out.println(buffer);
    
    logger.info( buffer);

    ClientDavInterface verb = m_connection;
    
    AttributeGroup atg = verb.getDataModel().getAttributeGroup(atgPid);
    Aspect asp = verb.getDataModel().getAspect(aspPid);

    DataDescription dd = new DataDescription(atg, asp);

    SystemObject obj = m_connection.getDataModel().getObject(objPid); 
      
    m_connection.subscribeReceiver(this, obj, dd, ReceiveOptions.normal(), role);
    
    synchronized (m_angemeldeteEmpfaenger)
    {
      m_angemeldeteEmpfaenger.add(new AngemeldeterEmpfaenger(this, obj, dd));
    }
  }
  
  /**
   * Rückgabe ob die Verbindung zum DaV steht
   * @return true: Verbnindung steht, false: Verbindung steht nicht
   */
  public boolean istVerbunden ()
  { 
     return m_verbunden;
  }

  /**
   * Umpdate Methode zum Empfang der Daten des DaV
   */
  public void update( ResultData[] arg0 )
  {
    ProzessVerwaltung pv = ProzessVerwaltung.getInstanz();
    
    //Alle Empfangene Daten auslesen
    
    for (int i = 0; i < arg0.length; i++) 
    {
      ResultData dat = arg0[i];

      //----------------------------------------------------------------------
      // Auslesen der "atg.prozessInfo"
      //----------------------------------------------------------------------

      if (dat.getDataDescription().getAttributeGroup().getPid().equals("atg.prozessInfo"))
      {
        // Absender feststellen
        
        String absenderPid = dat.getObject().getPid();
        String absenderIp  = bestimmeIpRechner( absenderPid );

        if (dat.hasData())
        {
          Array e = dat.getData().getArray( "Prozesse" );
          
          int anzahl = e.getLength();
          
          for (int a = 0; a < anzahl; a++) 
          {
            ProzessDaten pd = new ProzessDaten();
            
            pd.setName      ( e.getItem( a ).getTextValue( "Name" ).getValueText());
            pd.setProzessId ( e.getItem( a ).getTextValue( "ProzessID" ).getValueText() );
            
            pd.setZustand   ( E_ZUSTAND.getEnum( e.getItem( a ).getTextValue( "Zustand" ).getValueText()));
            
            pd.setLetzterStart         ( e.getItem( a ).getTextValue( "StartZeitpunkt" ).getValueText() );
            
            pd.setLetzterStopp         ( e.getItem( a ).getTextValue( "StoppZeitpunkt" ).getValueText() );
            pd.setApplikationsStart    ( e.getItem( a ).getTextValue( "StartZeitpunkt" ).getValueText() );
            pd.setLetzteInitialisierung( e.getItem( a ).getTextValue( "InitialisierungsZeitpunkt" ).getValueText() );
            pd.setNaechsterStart       ( e.getItem( a ).getTextValue( "NächsterStartZeitpunkt" ).getValueText() );
            
            pd.setIpAdresse ( absenderIp );
            
            // Prüfen ob ProzessID vom eigenen Rechner kommt (passiert, wenn im 
            // globalen Teil der startStopp.xml der eigene Rechner eingetragen wird)
            // ansonsten werden die Daten im Remote Feld gespeichert
            
            if (!absenderIp.equals( StartStoppApp.getRechnerAdresse()))
            {
              pv.addProzessDatenRemote (pd);
              
              if (StartStoppApp.getNutzerSchnittstelle() != null)
                StartStoppApp.getNutzerSchnittstelle().aktualisiereProzessEintraegeRemote();
            }
  
          } // for (int a = 0; a < anzahl; a++)
        }
        else
        {
          logger.info("Pid " + absenderPid + " IP " + absenderIp + " keine Daten (atg.prozessInfo)");
          
          pv.loescheProzessDatenRemote( absenderIp );
          
          if (StartStoppApp.getNutzerSchnittstelle() != null)
            StartStoppApp.getNutzerSchnittstelle().aktualisiereProzessEintraegeRemote();
        }
      }

      //----------------------------------------------------------------------
      // Auslesen der "atg.applikationsFertigmeldung"
      //----------------------------------------------------------------------
      
      if (dat.getDataDescription().getAttributeGroup().getPid().equals("atg.applikationsFertigmeldung"))
      {
        // Absender feststellen
        
        long absenderId = dat.getObject().getId();

        // Merker löschen, dass auf Applikation gewartet wird
        
        m_warteApplikationsFertigmeldung.remove( absenderId );
        
        if (dat.hasData())
        {
          String inkarnation   = dat.getData().getTextValue( "Inkarnationsname" ).getValueText();
          String initialisiert = dat.getData().getTextValue( "InitialisierungFertig" ).getValueText();
  
          if (_debugFertigMeldung)
          {
            if (initialisiert.equalsIgnoreCase( "JA" ))
              logger.info("Pid " + absenderId + " Inkarnation: " + inkarnation + " ---> initialisiert");
            else
              logger.info("Pid " + absenderId + " Inkarnation: " + inkarnation + " ---> nicht initialisiert");
          }

          // Interne Liste ProzessId - Inkarnationsbezeichnung merken
          
          m_zuordnungIdZuInkarnationen.put (absenderId, inkarnation );

          // Prüfen ob der StartStopp Applikation ein Prozess mit diesem Inkarnationsnamen
          // bekannt ist.
          
          ProzessDaten pd = pv.getProzessDaten( inkarnation );
          
          if (pd != null)
          {
            // Nur wenn diese Instanz der StartStopp Applikation auch den Prozess gestartet
            // hat (Zustand == "gestartet" darf der Wert "Initialisiert" übernommen werden.
            // Sonst handelt es sich um einen Prozess der nicht von dieser StartStopp Applikation
            // gestartet wurde.
            
            if (pd.isGestartet())
              if (initialisiert.equalsIgnoreCase( "JA" ))
                pd.getInkarnation().setzeZustand( E_ZUSTAND.INITIALISIERT, 0 );
          }
        }
      }

      //----------------------------------------------------------------------
      // Auslesen der "atg.angemeldeteApplikationen"
      //----------------------------------------------------------------------
      
      if (dat.getDataDescription().getAttributeGroup().getPid().equals("atg.angemeldeteApplikationen"))
      {
        if (dat.hasData())
        {
          Data data = dat.getData();
          
          List<Long> abgemeldeteIds = new ArrayList<Long> ();
          
          Array array = data.getArray("angemeldeteApplikation");
          
          int anzahl = array.getLength();
          
          // Löschen der bisherigen Prozess Zustände
          
          Iterator it = m_listeDavApplikationen.entrySet().iterator();
          while (it.hasNext())
          {
            Map.Entry me = (Map.Entry) it.next();
          
            Long key = (Long) me.getKey();
            
            m_listeDavApplikationen.put( key, 0 );
          }
           
          // Aktuelle Prozesse übernehmen
            
          for (int j = 0; j < anzahl; j++)
          {
            long id = array.getItem(j).getReferenceValue("applikation").getSystemObject().getId();
  
            if (m_listeDavApplikationen.containsKey( id ))
              m_listeDavApplikationen.put( id, 1 ); // Prozess aktiv und bereits angemeldet
            else
              m_listeDavApplikationen.put( id, 2 ); // Prozess aktiv und noch nicht angemeldet
          }
          
          // Anmelden/Abmelden für neue Prozesse
                    
          it = m_listeDavApplikationen.entrySet().iterator();
          while (it.hasNext())
          {
            Map.Entry me = (Map.Entry) it.next();
          
            int value = (Integer) me.getValue();
            
            // Anmelden
            
            if (value == 2)
            {
              Long key = (Long) me.getKey();
              
              anmeldeLeseDaV( key, "atg.applikationsFertigmeldung", "asp.standard", ReceiverRole.receiver() );
              
              // Prozess zur Liste der noch zu wartenden Prozesse hinzufügen
              
              m_warteApplikationsFertigmeldung.add( key );
              
              // Eintrag anlegen
              
              m_zuordnungIdZuInkarnationen.put( key, null );
            }
  
            // Abmelden
            
            if (value == 0)
            {
              Long key = (Long) me.getKey();
              
              abmeldeLeseDaV( key, "atg.applikationsFertigmeldung", "asp.standard" );
              
              abgemeldeteIds.add( key );
            }
          }
          
          // Löschen nicht mehr existender Applikationen
          
          for (int ii=0; ii<abgemeldeteIds.size(); ++ii)
          {
            m_listeDavApplikationen.remove( abgemeldeteIds.get( ii ) );
            
            m_zuordnungIdZuInkarnationen.remove( abgemeldeteIds.get( ii ) );
            
            if (_debugFertigMeldung)
              System.out.println("Löschen ID " + abgemeldeteIds.get( ii ));
          }
        }
      }

      //----------------------------------------------------------------------
      // Auslesen der "atg.usvZustandKritisch"
      //----------------------------------------------------------------------
      
      if (dat.getDataDescription().getAttributeGroup().getPid().equals("atg.usvZustandKritisch"))
      {
        if (dat.hasData())
        {
          String value = dat.getData().getTextValue( "KritischerZustand" ).getValueText();
  
          logger.fine("atg.usvZustandKritisch: " + value);
  
          if (value.equals("nicht ermittelbar"))
          {
          }
  
          if (value.equals("noch nicht erreicht"))
          {
            
          }
          
          if (value.equals("erreicht"))
          {
            StartStoppApp.beendeStartStoppWegenFehler("USV hat kritischen Zustand erreicht !");
          }
        }
      }
        
      //----------------------------------------------------------------------
      // Auslesen der "atg.startStoppAnfrage"
      //----------------------------------------------------------------------
      
      if (dat.getDataDescription().getAttributeGroup().getPid().equals("atg.startStoppAnfrage"))
      {
        if (dat.hasData())
          new StartStoppAnfrage (dat);
      }
    }
  }

  /**
   * Methode zum Absetzen einer Betriebsmeldung an den Datenverteiler
   * @param text Text der als Betriebsmeldung gesendet werden soll
   */
  public void sendeBetriebsmeldung (String text)
  {
    if (!ProzessVerwaltung.getInstanz().isKernSystemGestartet())
      return;
      
    MessageType typ = MessageType.APPLICATION_DOMAIN;
    String messageTypeAddOn = "Zustand";
    MessageGrade grade = MessageGrade.INFORMATION;
    String message = text;
    
    try
    {
      sendMessage (typ, messageTypeAddOn, grade, message);
    }
    catch (Exception e) 
    {
      e.printStackTrace();
    }
  } 

  /**
   * Methode erzeugt eine Betriebsmeldung und sendet diese an den
   * Datenverteiler. Eingestellt werden können MeldungsTyp, MeldungsTypZusatz
   * und die Meldungsklasse.
   * @param type             der MeldungsTyp
   * @param messageTypeAddOn der MeldungsTypZusatz
   * @param grade            die MeldungsKlasse
   * @param message          Text der Meldung
   */
  public void sendMessage (MessageType type, 
                           String messageTypeAddOn,
                           MessageGrade grade,
                           String message)
  {
    if (!m_verbunden)
      return;
    
    if (m_pidRechner == null)
      return;
    
    MessageSender msg = MessageSender.getInstance();
    
    if (msg == null)
    {
      logger.info("MessageSender = null");
      return;
    }
    
    msg.setApplicationLabel(m_pidRechner);

//    System.out.println("Betriebsmeldung: " + m_pidRechner + " - " + message);

    String id = "" + m_connection.getLocalApplicationObject().getId();
    
    SystemObject referenceObject = null;
    MessageState state = MessageState.CHANGE_MESSAGE;
    MessageCauser causer = new MessageCauser(null, "", "");

    /*
     * @param id               ID der Meldung. Dieses Attribut kann von der Applikation gesetzt werden, um einen Bezug zu
     *                         einer vorherigen Meldung herzustellen.
     * @param type             der MeldungsTyp
     * @param messageTypeAddOn der MeldungsTypZusatz
     * @param grade            die MeldungsKlasse
     * @param referenceObject  Referenz auf ein beliebiges Konfigurationsobjekt, auf das sich die Meldung bezieht.
     * @param state            Gibt den Zustand einer Meldung an.
     * @param causer           Urlasserinformation (Referenz auf den Benutzer, der die Meldung erzeugt hat, Angabe einer
     *                         Ursache für die Meldung und der Veranlasser für die Meldung)
     * @param message          Text der Meldung
     */

    // id == null führt zu einem Beenden des Threads ohne eine Fehlermeldung
    
    if (id == null)
      id = "StartStopp";
    
    msg.sendMessage(id, type, messageTypeAddOn, grade, referenceObject, state, causer, message);    
  }

  /**
   * Methode sendet die Attributgruppe "atg.startStoppAntwort" an den Prozess mit der Id absenderId.
   * @param absenderId Id des Absenderprozesses
   * @param absenderZeichen Zeichen des Absendeprozesses
   * @param id des Prozesses, bzw. des StartStopp-Blocks dessen Status übertragen wird
   * @param zustand Zustand des Prozesses bzw. des StartStopp Blocks (de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.Zustand)
   * @param fehler Fehlerkennung (de.bsvrz.sys.startstopp.skriptvew.SkriptVewEnums.E_FEHLER_STARTSTOPPBLOCK)
   */
  public void sendeStartStoppAntwort ( long absenderId,
                                       String absenderZeichen,
                                       String id, E_ZUSTAND zustand, E_FEHLER_STARTSTOPPBLOCK fehler )
  {
    if (!ProzessVerwaltung.getInstanz().isKernSystemGestartet())
      return;

    ClientDavInterface verb = m_connection;  
    
    SystemObject sysObj = verb.getDataModel().getObject(absenderId);
    
    AttributeGroup atg = (AttributeGroup) m_connection.getDataModel().getAttributeGroup("atg.startStoppAntwort");
    
    Data data = m_connection.createData( atg );

    data.getTextValue( "absenderZeichen"  ).setText( absenderZeichen );

    data.getTextValue( "id"  ).setText( id );
    
    if (ProzessVerwaltung.getInstanz().getProzessDaten( id ) != null)
      data.getTextValue( "art" ).setText( "prozess" );
    else
      data.getTextValue( "art" ).setText( "prozess" );
      
    data.getTextValue( "zustand" ).setText( zustand.getText() );
    data.getTextValue( "fehler" ).setText( fehler.getText() );
    
    logger.fine("sendeStartStoppAntwort: absenderId = " + absenderId + 
                       " absenderZeichen = " + absenderZeichen +
                       " Id = " + id +
                       " Zustand = " + zustand + 
                       " fehler = " + fehler.getText()); 
    
    if (data != null)
    {
      Aspect asp = verb.getDataModel().getAspect("asp.antwort");
      
      DataDescription dd = new DataDescription(atg, asp);

      ResultData dr = new ResultData( sysObj, dd, new Date().getTime(), data );

      // Es wird versucht die Daten zu senden, wenn dies fehlschlägt weil das Programm
      // sich noch nicht angemeldet hat, so erfolgt die Anmeldung und danach ein 
      // erneutes Senden.
      
      try
      {
        m_connection.sendData( dr );
      }
      catch ( DataNotSubscribedException e )
      {
        logger.info( "StartStopp Antwort: Anmelden am DaV" );
        
        anmeldeSendeDaV (absenderId, "atg.startStoppAntwort", "asp.antwort", SenderRole.sender());
        
        try
        {
          logger.info( "StartStopp Antwort: Senden der Daten" );
          
          m_connection.sendData( dr );
        }
        catch ( DataNotSubscribedException e1 )
        {
          logger.info( "StartStopp: " + e1.getMessage());
          
          e1.printStackTrace();
        }
        catch ( SendSubscriptionNotConfirmed e1 )
        {
          logger.info( "StartStopp: " + e1.getMessage());
          
          e1.printStackTrace();
        }
      }
      catch ( SendSubscriptionNotConfirmed e1 )
      {
        logger.info( "StartStopp: " + e1.getMessage());
        
        e1.printStackTrace();
      }

      abmeldeSendeDaV ( absenderId,  "atg.startStoppAntwort", "asp.antwort");
    }
  }
  
  /**
   * Methode zum Senden der Attributgruppe "atg.prozessInfo" an den DaV
   */

  public synchronized void sendeProzessInfo ()
  {
    if (!ProzessVerwaltung.getInstanz().isKernSystemGestartet())
      return;

    if (!m_sendeProzessInfo)
      return;

    ClientDavInterface verb = m_connection;  
    
    if (m_connection == null)
      return;
    
    if (m_pidRechner == null)
      return;
    
    SystemObject sysObj = verb.getDataModel().getObject(m_pidRechner);
    
    Data data = erstelleProzessInfo ();
    
    if (data != null)
    {
      AttributeGroup atg = verb.getDataModel().getAttributeGroup ("atg.prozessInfo");
      
      Aspect asp = verb.getDataModel().getAspect("asp.zustand");
      
      DataDescription dd = new DataDescription(atg, asp);

      ResultData dr = new ResultData( sysObj, dd, new Date().getTime(), data );

      // Es wird versucht die Daten zu senden, wenn dies fehlschlägt weil das Programm
      // sich noch nicht angemeldet hat, so erfolgt die Anmeldung und danach ein 
      // erneutes Senden.
      
      try
      {
        m_connection.sendData( dr );
      }
      catch ( DataNotSubscribedException e1 )
      {
        logger.info( "sendeDaten: " + e1.getMessage());
        
        e1.printStackTrace();
      }
      catch ( SendSubscriptionNotConfirmed e1 )
      {
        logger.info( "sendeDaten: " + e1.getMessage());
        
        e1.printStackTrace();
      }
    }
  }

  /**
   * Methode zum Erstellen der Attributgruppe "atg.prozessInfo"
   * @return Attributgruppe 
   */
  private Data erstelleProzessInfo ()
  {
    ProzessVerwaltung pv = ProzessVerwaltung.getInstanz();

    List<ProzessDaten> pListe = pv.getAlleProzessDaten();
    
    //----------------------------------------------------------------------
    // Vorbereitung Senden
    //----------------------------------------------------------------------
    
    AttributeGroup atg = (AttributeGroup) m_connection.getDataModel().getAttributeGroup("atg.prozessInfo");
    
    Data d = m_connection.createData(atg );
    
    int laenge = pListe.size();
    
    d.getArray ( "Prozesse" ).setLength(laenge);

    for (int i=0; i<laenge; ++i)
    {
      ProzessDaten pd = pListe.get( i );

      Data d1 = d.getArray( "Prozesse" ).getItem( i );
      Data d2 = null;
      
      Array a1 = null;

      //----------------------------------------------------------------------
      // Füllen der ersten 4 Textfelder
      //----------------------------------------------------------------------

      d1.getTextValue( "ProzessID").setText( pd.getProzessId());  
      d1.getTextValue( "Name").setText( pd.getName());
      d1.getTextValue( "AusfuehrbareDatei" ).setText( pd.getAusfuehrbareDatei());
     
      //----------------------------------------------------------------------
      // Füllen der Aufrufparameter
      //----------------------------------------------------------------------
      
      int anzPara =pd.getAufrufParameter().size();

      a1 = d1.getArray( "AufrufParameter" );
      
      a1.setLength(anzPara);
      
      Iterator<String> aufruf= (Iterator<String>)pd.getAufrufParameter().iterator();
      for (int j = 0; j  < anzPara; ++j)
        a1.getItem( j ).getTextValue( "AufrufparameterWert" ).setText( aufruf.next().toString() );
      
      //----------------------------------------------------------------------
      // Füllen der StartArt
      //----------------------------------------------------------------------
      
      StartArt stA = pd.getStartArt();
      
      d2 = d1.getItem( "StartArt" );
      
      d2.getScaledValue( "OptionStart" ).setText( stA.getOption().getText() );
      d2.getScaledValue( "NeuStart" ).setText(stA.getNeuStart().toString() );
      d2.getTextValue( "Intervall" ).setText(stA.getIntervallZeit().toString() );
      
      //----------------------------------------------------------------------
      //Füllen der StartBedingung
      //----------------------------------------------------------------------
      
      int anzSTB = pd.getStartBedingung().size();

      a1 = d1.getArray( "StartBedingung" );
      
      a1.setLength(anzSTB);
      
      Iterator<StartBedingung> startbedingung= pd.getStartBedingung().iterator();
      for (int j = 0; j  < anzSTB; ++j)
      {     
        StartBedingung zw=startbedingung.next();

        a1.getItem( j ).getTextValue( "Vorgaenger" ).setText( zw.getProzessName() );
        a1.getItem( j ).getScaledValue( "WarteArt" ).setText( zw.getWarteArt().getText() );
        a1.getItem( j ).getTextValue( "Rechner" ).setText(zw.getRechnerAlias() );
        a1.getItem( j ).getTimeValue( "WarteZeit" ).setSeconds( zw.getWarteZeit() );
      }
      
      //----------------------------------------------------------------------
      // Füllen der StoppBedingung
      //----------------------------------------------------------------------
      
      int anzStopp =pd.getStoppBedingung().size();

      a1 = d1.getArray( "StoppBedingung" );
      
      a1.setLength(anzStopp);
      
      Iterator<StoppBedingung> stoppbedingung= pd.getStoppBedingung().iterator();
      for (int j = 0; j  < anzStopp; ++j)
      {     
        StoppBedingung zw=stoppbedingung.next();
        
        a1.getItem( j ).getTextValue( "Nachfolger" ).setText( zw.getProzessName());
        a1.getItem( j ).getTextValue( "Rechner" ).setText(zw.getRechnerAlias() );
        a1.getItem( j ).getTimeValue( "WarteZeit" ).setSeconds( zw.getWarteZeit() );
      }
      
      //----------------------------------------------------------------------
      // Füllen der StandardAusgabe
      //----------------------------------------------------------------------
      
      d2 = d1.getItem( "StandardAusgabe");
      
      d2.getScaledValue( "OptionStandardAusgabe" ).setText( pd.getStandardAusgabe().getOption().getText() );
      d2.getTextValue( "Datei" ).setText( pd.getStandardAusgabe().getDateiAlias() );
      
      //----------------------------------------------------------------------
      // Füllen der FehlerAusgabe
      //----------------------------------------------------------------------
      
      d2 = d1.getItem( "FehlerAusgabe" );
      
      d2.getScaledValue( "OptionFehlerAusgabe" ).setText( pd.getStandardFehlerAusgabe().getOption().getText() );
      d2.getTextValue( "Datei" ).setText( pd.getStandardFehlerAusgabe().getDateiAlias() );
      
      //----------------------------------------------------------------------
      // Füllen der StartVerhalten Fehler
      //----------------------------------------------------------------------
      
      d2 = d1.getItem( "StartVerhaltenFehler" );
      
      d2.getScaledValue( "StartVerhaltenFehlerOption" ).setText( pd.getStartVerhaltenFehler().getOption().getText() );
      d2.getScaledValue( "Wiederholrate" ).set(  pd.getStartVerhaltenFehler().getWiederholungen() );
      
      //----------------------------------------------------------------------
      // Füllen der StartVerhalten Fehler
      //----------------------------------------------------------------------
      
      d2 = d1.getItem( "StoppVerhaltenFehler" );
      
      d2.getScaledValue( "StoppVerhaltenFehlerOption" ).setText( pd.getStoppVerhaltenFehler().getOption().getText() );
      d2.getScaledValue( "Wiederholrate" ).set(  pd.getStoppVerhaltenFehler().getWiederholungen() );
      
      //----------------------------------------------------------------------
      // Füllen der StartVerhalten Fehler
      //----------------------------------------------------------------------
      
      d1.getScaledValue( "SimulationsVariante" ).set(  pd.getSimulationsVariante() );
      
      //----------------------------------------------------------------------
      // Füllen vom Zustand 
      //----------------------------------------------------------------------

      d1.getScaledValue( "Zustand" ).setText(   pd.getZustand().getText() );
     
      //----------------------------------------------------------------------
      // Füllen der Zeiten 
      //----------------------------------------------------------------------

      d1.getTextValue( "StartZeitpunkt").setText( pd.getLetzterStartAsString());

      d1.getTextValue( "StoppZeitpunkt").setText( pd.getLetzterStoppAsString());
      d1.getTextValue( "InitialisierungsZeitpunkt").setText( pd.getLetzteInitialisierungAsString());
      d1.getTextValue( "NächsterStartZeitpunkt").setText( pd.getNaechsterStartAsString());
         
    } // for (int i=0; i<laenge; ++i)
  
    return d;
  }

  /**
   * Methode zum Senden der Attributgruppe "atg.startStoppInfo" an den DaV
   * Besteht zum Zeitpunkt des Sendens die Verbindung zum DaV noch nicht 
   * (z.B. nach einem Systemstart) so startet die Methode einen Listener,
   * der auf die Verbindung wartet und dann erst die Attributgruppe versendet.
   */
  
  public synchronized void sendeStartStoppInfo ()
  {
    ProzessVerwaltung pv = ProzessVerwaltung.getInstanz();
    
    if (!pv.isKernSystemGestartet())
    {
      pv.addListener( new IKernsystemListener () 
      {
        public void exec( KernsystemEreignis e )
        {
          if (e.isGestartet())
            _sendeStartStoppinfo();
        }
      });
      
      return;
    }
 
    if (!m_sendeStartStoppInfo)
      return;
    
    _sendeStartStoppinfo();
  }
  
  /**
   * Eigentliche Methode zum Senden der Attributgruppe "atg.startStoppInfo".
   * Wird von der Methode "sendeStartStoppInfo" verwendet.
   */
  private void _sendeStartStoppinfo ()
  {
    ClientDavInterface verb = m_connection;  
    
    if (m_pidRechner == null)
      return;
    
    SystemObject sysObj = verb.getDataModel().getObject(m_pidRechner);
    
    Data data = erstelleStartStoppInfo ();
    
    if (data != null)
    {
      AttributeGroup atg = verb.getDataModel().getAttributeGroup ("atg.startStoppInfo");
      
      Aspect asp = verb.getDataModel().getAspect("asp.zustand");
      
      DataDescription dd = new DataDescription(atg, asp);

      ResultData dr = new ResultData( sysObj, dd, new Date().getTime(), data );

      // Es wird versucht die Daten zu senden, wenn dies fehlschlägt weil das Programm
      // sich noch nicht angemeldet hat, so erfolgt die Anmeldung und danach ein 
      // erneutes Senden.
      
      try
      {
        m_connection.sendData( dr );
      }
      catch ( DataNotSubscribedException e1 )
      {
        logger.error( "sendeDaten: " + e1.getMessage());
        
        e1.printStackTrace();
      }
      catch ( SendSubscriptionNotConfirmed e1 )
      {
        logger.error( "sendeDaten: " + e1.getMessage());
        
        e1.printStackTrace();
      }
    }
  }

  /**
   * Methode zum Erstellen der Attributgruppe "atg.startStoppInfo"
   * @return Attributgruppe 
   */
  private Data erstelleStartStoppInfo ()
  {
    StartStoppVerwaltung ssv = StartStoppVerwaltung.getInstanz(); 
    
    //----------------------------------------------------------------------
    // Vorbereitung Senden
    //----------------------------------------------------------------------
    
    AttributeGroup atg = (AttributeGroup) m_connection.getDataModel().getAttributeGroup("atg.startStoppInfo");
    
    Data d = m_connection.createData(atg );
    
    List<String> l = ssv.getStartStoppBlockIds();
    
    int laenge = l.size();
    
    d.getArray ( "StartStoppBloecke" ).setLength(laenge);

    for (int i=0; i<laenge; ++i)
    {
      String startStoppId = l.get (i); 
      
      // StartStopp Block laden
      
      StartStoppBlock sb = ssv.getStartStoppBlock( startStoppId );
        
      List<String> inkarnationen = sb.getAllProcessIds();
      
      Data d1 = d.getArray( "StartStoppBloecke" ).getItem( i );
      
      Array a1 = null;

      //----------------------------------------------------------------------
      // Füllen der Felder
      //----------------------------------------------------------------------

      d1.getTextValue( "StartStoppID").setText( sb.getStartStoppBlockId() );
      
      //----------------------------------------------------------------------
      // Füllen der InkarnationsId
      //----------------------------------------------------------------------
      
      int anzInkarnationen = inkarnationen.size();

      a1 = d1.getArray( "Inkarnationen" );
      
      a1.setLength(anzInkarnationen);
      for (int j=0; j<anzInkarnationen; ++j)
        a1.getItem (j).getTextValue( "ProzessID" ).setText( inkarnationen.get( j ));
      
      //----------------------------------------------------------------------
      // Füllen vom Zustand 
      //----------------------------------------------------------------------

      d1.getScaledValue( "Zustand" ).setText( sb.getStatus().getText() );
     
      //----------------------------------------------------------------------
      // Füllen des Startzeitpunktes 
      //----------------------------------------------------------------------

      d1.getTextValue( "StartZeitpunkt").setText( sb.getStatusZeitpunktAlsString() );
         
    } // for (int i=0; i<laenge; ++i)
  
    return d;
  }
  
  /* (non-Javadoc)
   * @see stauma.dav.clientside.ClientSenderInterface#dataRequest(stauma.dav.configuration.interfaces.SystemObject, stauma.dav.clientside.DataDescription, byte)
   */
  public void dataRequest( SystemObject arg0, DataDescription arg1, byte arg2 )
  {
//    String objPid = arg0.getPid();
    
    logger.config("dataRequest " + arg0.getPid() + " dd " + arg1 + " byte " + arg2);
    
    String atgPid = arg1.getAttributeGroup().getPid();
    logger.fine("Pid = " + atgPid);
    Collection<Aspect> col = arg1.getAttributeGroup().getAspects();
    
    // Attributgruppe auswerten
    
    if (atgPid.equals("atg.prozessInfo"))
    {
      Iterator it = col.iterator();

      while (it.hasNext())
      {
        Aspect asp = (Aspect) it.next();
        if (asp.getPid().equals( "asp.zustand" ))
          m_sendeProzessInfo = (arg2 == START_SENDING);
      }
    }
    
    if (atgPid.equals("atg.startStoppInfo"))
    {
      Iterator it = col.iterator();

      while (it.hasNext())
      {
        Aspect asp = (Aspect) it.next();
        if (asp.getPid().equals( "asp.zustand" ))
          m_sendeStartStoppInfo = (arg2 == START_SENDING);
      }
    }
  }

/*
 * (Kein Javadoc)
 * @see stauma.dav.clientside.ClientSenderInterface#isRequestSupported(stauma.dav.configuration.interfaces.SystemObject, stauma.dav.clientside.DataDescription)
 */
  public boolean isRequestSupported( SystemObject arg0, DataDescription arg1 )
  {
    return false;
  }
  
  /**
   * Methode zum Prüfen ob die USV mit der Pid pidUSV existiert
   * @param pidUsv Pid der USV
   * @return true USV mit der Pid existiert, sonst false
   */
  private boolean pruefeUsv (String pidUsv)
  {
    List<String> usv = bestimmeObjekte("typ.usv");
    
    return usv.contains(pidUsv);
  }
  
  /**
   * Methode zum Bestimmen der Objekte, die zu einer bestimmten PID
   * gehören (z.B. typ.de)
   * @param objPid Pid der Objekttypen
   * @return Liste mit den PIDs der Objekte 
   */
  private List<String> bestimmeObjekte (String objPid)
  {
    List<String> objekte = new ArrayList<String> ();
    
    logger.fine("bestimmeObjekte --> " + objPid);

    // Systemobjekt erzeugen

    SystemObjectType typeSysObj = null;

    typeSysObj = m_connection.getDataModel().getType(objPid);
    
    if (typeSysObj == null)
      return objekte;
      
    if (typeSysObj.isConfigurating())
      typeSysObj = (ConfigurationObjectType) typeSysObj;
    else
      typeSysObj = (DynamicObjectType) typeSysObj;
    
    // Elemente dieses Systemobjekts einlesen
    
    List typobjekte = typeSysObj.getElements();
    
    Iterator iterator_Typen = typobjekte.iterator();

    while (iterator_Typen.hasNext()) 
    {
      SystemObject sysObj = (SystemObject) iterator_Typen.next();
      objekte.add ( sysObj.getPid() );
    }

    return objekte;
  }

  /**
   * Methode liefert Informationen darüber, ob eine Applikation mit dem Inkarnationsnamen
   * "inkarnation" am DaV angemeldet ist oder nicht.
   * @param inkarnation Inkarnationsname der zu prüfenden Applikation
   * @return true: Applikation am DaV angemeldet, sonst false
   */
  public boolean isInkarnationAmDaVAngemeldet (String inkarnation)
  {
    return m_zuordnungIdZuInkarnationen.containsValue( inkarnation );
  }

	/**
	 * Holt die aktuelle Datenverteilerverbindung.
	 * 
	 * @return aktuelle Datenverteilerverbindung oder <code>null</code>, wenn keine besteht.
	 */
	public ClientDavInterface getConnection()
	{
		return m_connection;
	}
	
	/**
	 * Holt die zu Grunde liegende Datenverteilerverbindung.
	 * 
	 * @return zu Grunde liegende Datenverteilerverbindung.
	 */
	public ClientDavInterface getConnectionFuerImmer()
	{
		return m_connectionFuerImmer;
	}
	
	private class AngemeldeterSender
	{
		private ClientSenderInterface m_sender;
		private Vector<SystemObject> m_objekte = new Vector<SystemObject>();
		private DataDescription m_dataDescription;
		
		public AngemeldeterSender(ClientSenderInterface sender, SystemObject objekt, DataDescription dataDescription)
		{
			this(sender, new SystemObject[] {objekt}, dataDescription);
		}
		
		public AngemeldeterSender(ClientSenderInterface sender, SystemObject[] objekte, DataDescription dataDescription)
		{
			m_sender = sender;
			for (int i = 0; i < objekte.length; i++)
			{
				m_objekte.add(objekte[i]);
			}
			m_dataDescription = dataDescription;
		}
		
		public AngemeldeterSender(ClientSenderInterface sender, Collection<SystemObject> objekte, DataDescription dataDescription)
		{
			m_sender = sender;
			for (Iterator<SystemObject> iterator = objekte.iterator(); iterator.hasNext();)
			{
				m_objekte.add(iterator.next());
			}
			m_dataDescription = dataDescription;
		}

		public ClientSenderInterface getSender()
		{
			return m_sender;
		}

		public Vector<SystemObject> getObjekte()
		{
			return m_objekte;
		}

		public DataDescription getDataDescription()
		{
			return m_dataDescription;
		}

		@Override
		public String toString()
		{
			StringBuffer ergebnis = new StringBuffer("AngemeldeterSender");

			ergebnis.append(" " + m_sender);
			
			for (int i = 0; i < m_objekte.size(); i++)
			{
				if (i == 0)
				{
					ergebnis.append(" " + m_objekte.get(i).getPidOrNameOrId());
				}
				else
				{
					ergebnis.append(", " + m_objekte.get(i).getPidOrNameOrId());
				}
			}
			ergebnis.append(" " + m_dataDescription);
			
			return ergebnis.toString();
		}
	}

	private class AngemeldeterEmpfaenger
	{
		private ClientReceiverInterface m_empfaenger;
		private Vector<SystemObject> m_objekte = new Vector<SystemObject>();
		private DataDescription m_dataDescription;
		
		public AngemeldeterEmpfaenger(ClientReceiverInterface empfaenger, SystemObject objekt, DataDescription dataDescription)
		{
			this(empfaenger, new SystemObject[] {objekt}, dataDescription);
		}
		
		public AngemeldeterEmpfaenger(ClientReceiverInterface empfaenger, SystemObject[] objekte, DataDescription dataDescription)
		{
			m_empfaenger = empfaenger;
			for (int i = 0; i < objekte.length; i++)
			{
				m_objekte.add(objekte[i]);
			}
			m_dataDescription = dataDescription;
		}
		
		public AngemeldeterEmpfaenger(ClientReceiverInterface empfaenger, Collection<SystemObject> objekte, DataDescription dataDescription)
		{
			m_empfaenger = empfaenger;
			for (Iterator<SystemObject> iterator = objekte.iterator(); iterator.hasNext();)
			{
				m_objekte.add(iterator.next());
			}
			m_dataDescription = dataDescription;
		}

		public ClientReceiverInterface getEmpfaenger()
		{
			return m_empfaenger;
		}

		public Vector<SystemObject> getObjekte()
		{
			return m_objekte;
		}

		public DataDescription getDataDescription()
		{
			return m_dataDescription;
		}

		@Override
		public String toString()
		{
			StringBuffer ergebnis = new StringBuffer("AngemeldeterEmpfaenger");

			ergebnis.append(" " + m_empfaenger);
			
			for (int i = 0; i < m_objekte.size(); i++)
			{
				if (i == 0)
				{
					ergebnis.append(" " + m_objekte.get(i).getPidOrNameOrId());
				}
				else
				{
					ergebnis.append(", " + m_objekte.get(i).getPidOrNameOrId());
				}
			}
			ergebnis.append(" " + m_dataDescription);
			
			return ergebnis.toString();
		}
	}

	public Vector<AngemeldeterSender> getAngemeldeteSender()
	{
	  Vector<AngemeldeterSender> v = new Vector<AngemeldeterSender>();
	  synchronized (m_angemeldeteSender)
    {
      Iterator<AngemeldeterSender> it = m_angemeldeteSender.iterator();
      while (it.hasNext())
        v.add( it.next() );
    }
		return v;
	}

	public Vector<AngemeldeterEmpfaenger> getAngemeldeteEmpfaenger()
	{
	  Vector<AngemeldeterEmpfaenger> v = new Vector<AngemeldeterEmpfaenger> ();
	  synchronized (m_angemeldeteEmpfaenger)
    {
      Iterator<AngemeldeterEmpfaenger> it = m_angemeldeteEmpfaenger.iterator();
      while (it.hasNext())
        v.add( it.next() );
    }
		return v;
	}

	/**
	 * Alle zuvor angemeldeten Empfänger, Senken, Sender und Quellen abmelden.
	 */
	public void allesAbmelden()
	{
	  synchronized (m_angemeldeteEmpfaenger)
    {
  		for (Iterator iterator = m_angemeldeteEmpfaenger.iterator(); iterator.hasNext();)
  		{
  			AngemeldeterEmpfaenger empfaenger = (AngemeldeterEmpfaenger)iterator.next();
  			
  			m_connection.unsubscribeReceiver(empfaenger.getEmpfaenger(), empfaenger.getObjekte(), empfaenger.getDataDescription());
  		}
    }
		
	  synchronized (m_angemeldeteSender)
    {
  		for (Iterator iterator = m_angemeldeteSender.iterator(); iterator.hasNext();)
  		{
  			AngemeldeterSender sender = (AngemeldeterSender)iterator.next();
  			
  			m_connection.unsubscribeSender(sender.getSender(), sender.getObjekte(), sender.getDataDescription());
  		}
    }
	}

  /**
   * Methode liefert die Variable pidRechner zurück
   * @return Pid des lokalen Rechners
   */
  public String getPidRechner()
  {
    String pid = null;  
  
    // Adresse des eigenen Rechners feststellen

    String host = HostRechner.getInstanz().getHostName();
    if (host != null)
      pid = bestimmePidRechnerByHost( host );
      
    return pid;
  }
}
