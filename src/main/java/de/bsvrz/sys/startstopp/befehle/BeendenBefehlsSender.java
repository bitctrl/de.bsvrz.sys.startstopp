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

package de.bsvrz.sys.startstopp.befehle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.startstopp.buv.CmdInterpreter;

/**
 * Diese Klasse sendet eine Sequenz ueber das Telnet-Interface von StartStopp, so dass StartStopp beendet wird.
 *
 * @version $Revision: 1.1 $ / $Date: 2008/10/29 13:17:43 $ / ($Author: Drapp $)
 *
 * @author Dambach-Werke GmbH
 * @author Thomas Drapp
 *
 */
public class BeendenBefehlsSender 
{
  /**
   * Max. Zeit der Inaktivität, nach der der Server die Verbindung zum Client beendet in Sekunden.
   */
  public final static int SEND_TIMEOUT = 60 * 5;
  /**
   * Das Prompt für die Darstellung einer Eingabeaufforderung an der Konsole.
   */
  public final static String PROMPT = "? ";

	/**
	 * Beendet das unter host:port befindliche StartStopp. Falls kein host angegeben ist, wird localhost verwendet.
	 * Meldet sich kein StartStopp, wird nach einem Timeout ein Fehler ausgegeben. Die korrekte Beendigung von
	 * StartStopp wird am connection reset erkannt. In diesem Fall wird 0 zurückgegeben, sonst 1.
	 * 
	 * @param arguments Kommandozeilenargumente.
	 */
	public static void main(String[] arguments) 
	{
	  ArgumentList argumentList = new ArgumentList(arguments);
	  
	  String host = argumentList.fetchArgument("-host=localhost").asNonEmptyString();
	  int port = argumentList.fetchArgument("-port=").intValue();
			
		try 
		{
			sendQuitCmd(host, port);

      System.exit(0);
		} 
		catch (UnknownHostException e) 
		{
			System.out.println("Fehler: Host '"+host+"' nicht gefunden");
			
			System.exit(1);
		} 
		catch (SocketTimeoutException e)
		{
			System.out.println("Fehler: Keine Antwort von StartStopp unter " + host + ":" + port + " nach " + SEND_TIMEOUT + " Sekunden");
			
			System.exit(1);
		}
    catch (IOException e) 
    {
      if (e.getMessage().endsWith("Connection reset"))
      {
        System.out.println("OK: StartStopp unter " + host + ":" + port + " beendet.");
        
        System.exit(0);
      }
      else
      {
        System.out.println("Fehler: Kommunikation zu StartStopp unter " + host + ":" + port + " fehlgeschlagen: " + e.getMessage());
        
        System.exit(1);
      }
    }
		catch (Exception e) 
    {
      e.printStackTrace();

      System.exit(1);
    }
  }

  /**
   * Beendet StartStopp über den Telnet Server.
   * 
   * @param host Host des Servers 
   * @param port Port des Servers
   * @throws UnknownHostException Unbekannter Host.
   * @throws IOException Fehler beim Erzeugen des Sockets.
   */
  public static void sendQuitCmd(String host, int port) throws UnknownHostException, IOException
  {
    Socket connection = new Socket(host, port);
    
    connection.setSoTimeout(SEND_TIMEOUT * 1000);
    
    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        
    readPrompt(in);
    write(out, "6"); // 'StartStopp beenden' anwählen
    readPrompt(in);
    write(out, "J_Programm"); // Sicherheitsabfrage mit Ja beantworten
    String rueckgabe = in.readLine(); // Zeilenumbruch einlesen
    rueckgabe = in.readLine(); // Beendigungsmeldung einlesen
    
    if (rueckgabe.contains("StartStopp Applikation wird heruntergefahren !")) // Beendigungsmeldung prüfen
    {
      System.out.println("StartStopp unter " + host + ":" + port + " beendet.");
    }
  }
  
  /**
   * Einlesen des Prompt über die Telnet-Verbindung.
   * 
   * @param in Reader aus dem eingelesen werden soll.
   * @throws IOException Fehler beim Einlesen aufgetreten.
   */
  private static void readPrompt(BufferedReader in) throws IOException
  {
    while (!in.readLine().endsWith(CmdInterpreter.PROMPT));
  }
  
  /**
   * Schreiben von Zeichen über die Telnet-Verbindung.
   * 
   * @param out Writer über den geschreiben werden soll.
   * @param s Zeichen die geschrieben werden sollen.
   * @throws IOException Fehler beim Schreiben aufgetreten.
   */
  private static void write(BufferedWriter out, String s) throws IOException
  {
    out.write(s);
    out.newLine();
    out.flush();
  }
}
