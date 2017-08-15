/**
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
 * beck et al. projects GmbH
 * Theresienhoehe 13
 * 80336 Munich
 * Phone: +49-89-5442530
 * mailto: info@bea.de
 */
package de.bsvrz.sys.startstopp.buv;

import java.io.BufferedReader;
import java.io.BufferedWriter;

/**
 * Abstrakte Klasse für Kommandos des {@link de.bsvrz.sys.startstopp.buv.CmdInterpreter}
 * 
 * @author beck et al. projects GmbH
 * @author Thomas Müller
 * @version $Revision: 1.8 $ / $Date: 2009/06/09 11:11:08 $ / ($Author: ObertM $)
 */
public abstract class Command {
    
    private CmdMenu parentNode;
    private int index;
    private String help,description;
    private BufferedReader in;
    private BufferedWriter out;
        
    /**
     * Kommando für {@link CmdInterpreter}
     * @param desc die Beschreibung
     * @param help der Hilfetext
     */
    public Command(String desc, String help) {
        this.description = (desc.equals("")) ? "Keine Beschreibung verfuegbar." : desc;
        this.help = (help.equals("")) ? "Keine Hilfe verfuegbar." : help;
    }
    
    /**
     * Reader und Writer für Ein- und Ausgabefunktionen des Kommandos setzen
     * @param in Eingabe-Reader
     * @param out E_AUSGABE-Writer
     */
    public void setStreams(BufferedReader in, BufferedWriter out)  {
        this.in = in;
        this.out = out;
    }
    
    /**
     * Eltern-Menü für Kommando lesen
     * @return Eltern-Menü
     */
    public CmdMenu getParent() {
        return this.parentNode;
    }
    
    /** Eltern-Menü für Kommando setzen
     * 
     * @param parent das Eltern-Menü
     */
    public void setParent(CmdMenu parent) {
        this.parentNode = parent;
        setIndex();
    }
    
    /**
     * Index des Kommandos setzen (abhängig vom Eltern-Menü und Geschwister-Einträgen)
     *
     */
    public void setIndex() {
        this.index = this.parentNode.getSubMenues().size()+this.parentNode.getCommands().size()-this.parentNode.getNumHiddenCommands();
    }
    
    /**
     * Index des Kommandos auslesen
     * @return Index (immer eindeutig in einem Menü)
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Hilfetext setzen
     * @param help Hilfetext
     */
    public void setHelp(String help) {
        this.help = help;
    }
    
    /**
     * Hilfetext lesen
     * @return der Hilfetext
     */
    public String getHelp() {
        return this.help;
    }
    
    /**
     * Beschreibung setzen (wird im Menü in eckigen Klammern angezeigt)
     * @param desc Beschreibung
     */
    public void setDesc(String desc) {
        this.description = desc;
    }
    
    /**
     * Beschreibung lesen
     * @return die Beschreibung
     */
    public String getDesc() {
        return this.description;
    }
    
    /**
     * Benutzereingabe in einem Kommando (Abbruch bei Überschreitung des Server-Timeout) 
     * @return userinput Benutzereingabe
     */
    public String readln() throws Exception {
        out.newLine();
        out.write(CmdInterpreter.PROMPT);
        out.newLine();
        out.flush();
        String input = in.readLine();
    return (input==null) ? "" : input.trim();
    }

    /**
     * Benutzereingabe in einem Kommando (Abbruch bei Überschreitung des Server-Timeout) 
     * @return userinput Benutzereingabe
     */
    public int read() throws Exception
    {
      int input = -1;
      if (in.ready())
        input = in.read();
      return input;
    }

    /**
     * Ausgabe auf Client-Konsole (z.b. via Telnet)
     * @param out der Ausgabe-String
     */
    public void println(String out) throws Exception {
            this.out.newLine();
            this.out.write(" ["+this.description+"] "+out);
            this.out.flush();
    }
    
    /**
     * Ausgabe auf Client-Konsole (z.b. via Telnet) ohne die aktuelle Option auszugeben
     * @param out der Ausgabe-String
     */
    public void printlnPlain(String out) throws Exception {
            this.out.newLine();
            this.out.write(out);
            this.out.flush();
    }
   
    /**
     * Ausgabe auf Client-Konsole (z.b. via Telnet) ohne die aktuelle Option auszugeben
     * @param out der Ausgabe-String
     */
    public void printPlain(String out) throws Exception {
            this.out.write(out);
            this.out.flush();
    }
    
    
    /**
     * Methode muss von einer implementierenden Klasse gefüllt werden
     * @throws Exception meist bei Server-Timeout (wird im {@link CmdInterpreter} abgefangen)
     */
    public abstract void execute() throws Exception;
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
    	return description;
    }
}
