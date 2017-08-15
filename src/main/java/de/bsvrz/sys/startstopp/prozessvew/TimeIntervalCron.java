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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Diese Klasse stellt ein ZeitIntervallCron dar.
 * 
 * Die Spezifikation erfolgt durch die Angabe von 5 Terminfeldern, wobei im ersten Feld die Minute
 * [0;59], im zweiten Feld die Stunde [0;23], im dritten Feld der Tag [1;31], im viertem Feld der
 * Monat [1;12] und im letzten Feld der Wochentag {Mo,Di,Mi,Do,Fr,Sa,So} angegeben werden kann. Pro
 * Terminfeld sind mehrere Einträge, die durch Kommata getrennt werden müssen, möglich. Die Eingabe
 * eines '*' deckt den gesamten Bereich eines Terminfeldes ab. Mit dem Zeichen '-' können
 * Teilbereiche abgedeckt werden. Die Angaben aller Terminfelder sind UND-verknüpft.
 * 
 * Reihenfolge und Bedeutung der Felder
 * 
 * <table>
 * <tr><th align="right">Feld</th><th align="right">Mögliche Werte</th></tr>
 * <tr><td align="right">Minute</td><td align="right">0-59</td></tr>
 * <tr><td align="right">Stunde</td><td align="right">0-23</td></tr>
 * <tr><td align="right">Tag</td><td align="right">1-31</td></tr>
 * <tr><td align="right">Monat</td><td align="right">1-12</td></tr>
 * <tr><td align="right">Wochentag</td><td align="right">>Mo/Di/Mi/Do/Fr/Sa/So</td></tr>
 * </table>
 * 
 * Die Syntax ist im Vergleich zu Cron reduziert. Insbesondere können keine Schritte (etwa 
 * '* / 5' um einen Befehl alle 5 Minuten auszuführen) angegeben werden.
 * 
 * Außerdem muss beachtet werden, dass alle Felder UND-Verknüpft sind. Das heißt, '0 0 1 * Mo' steht für "um Mitternacht 
 * wenn der Montag auch der erste eines Monats ist". Unter Linux/Cron bedeutet diese Angabe jedoch
 * "Mitternachts an jedem Montag und am ersten eines Monats." 
 * 
 * @author beck et al. projects GmbH
 * @author Phil Schrettenbrunner
 * @version $Revision: 1.5 $ / $Date: 2008/02/08 15:07:46 $ / ($Author: ObertM $)
 */
public class TimeIntervalCron {
    private static final HashMap<String, Integer> DAYNAMES = new HashMap<String, Integer>(7);
    static {
        DAYNAMES.put("so", 0);
        DAYNAMES.put("mo", 1);
        DAYNAMES.put("di", 2);
        DAYNAMES.put("mi", 3);
        DAYNAMES.put("do", 4);
        DAYNAMES.put("fr", 5);
        DAYNAMES.put("sa", 6);
    }
    
    private boolean hasParseErrors = false;
    private String originalTimeIntervalCron = "";
    private boolean minute[];
    private boolean hour[];
    private boolean dayOfMonth[];
    private boolean month[];
    private boolean dayOfWeek[];
    private Calendar c = Calendar.getInstance();

    
    /**
     * Standardkonstruktor. Es wird keine Zeit gesetzt, daher wird bei jeder Anfrage false (also
     * nicht-starten) geantwortet, bis mittels set() ein String in Cron-Sytnax übergeben wird.
     */
    public TimeIntervalCron() {
        init();
    }


    /**
     * @param fields
     */
    public TimeIntervalCron(String fields) {
        setFields(fields);
    }


    /**
     * Analysiert den übergebenen String und setzt intern die Felder für das entsprechende Datum.
     * Alte Einstellungen gehen dabei verlohren.
     * 
     * Wenn der übergebene String Syntaxfehler enthält, werden alle alle Felder deaktiviert so 
     * dass es nie zu einer Ausführung kommt. 
     * 
     * @param fields
     */
    public void setFields(String fields) {
        init();
        originalTimeIntervalCron = fields;
        hasParseErrors = false; // alles ok...
        
        if (fields.length()==0)
            return; // Keine Daten -> läuft nie

        String field[] = fields.split("\\s+");
        if (field.length != 5) {
            hasParseErrors = true;
            return;
        }

        try {
            parseField(field[0], minute,     0);     // Minuten, beginnend bei 0
            parseField(field[1], hour,       0);     // Stunden, beginnend bei 0
            parseField(field[2], dayOfMonth, 1);     // Tag des Monates, beginnend bei 1
            parseField(field[3], month,      1);     // Monat, beginnend bei 1
            parseField(field[4], dayOfWeek,  0);     // Wochentag, beginnend bei 0
        } catch (ParseException e) {
            init();
            hasParseErrors = true;
        }
    }
    
    
    /**
     * Prüft, ob die übergebene Zeit in das Interval fällt, zu dem der Job laufen soll. Die Sekunden
     * werden dabei ignoriert, d.h. es wird immer abgerundet.
     * 
     * @param date Milisekunde eines Zeitpunkts, der überprüft werden soll
     * @return Wahr, wenn der Zeitpunkt durch die gesetzten Zeitspannen abgedeckt wird, ansonten
     * falsch
     */
    public boolean shouldRun(Date date) {
        c.setTime(date);

        int min = c.get(Calendar.MINUTE);
        int h   = c.get(Calendar.HOUR_OF_DAY);
        int dom = c.get(Calendar.DAY_OF_MONTH)-1; // kleinster Wert 1 -> minus 1
        int mon = c.get(Calendar.MONTH);          // kleinster Wert 1 -> minus 1
        int dow = 0;
        switch (c.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:    dow = 1; break;
            case Calendar.TUESDAY:   dow = 2; break;
            case Calendar.WEDNESDAY: dow = 3; break;
            case Calendar.THURSDAY:  dow = 4; break;
            case Calendar.FRIDAY:    dow = 5; break;
            case Calendar.SATURDAY:  dow = 6; break;
            case Calendar.SUNDAY:    dow = 0; break;
        }

        return (minute[min] && hour[h] && dayOfMonth[dom] && month[mon] && dayOfWeek[dow]);
    }
    
    
    /**
     * Berechnet das Datum des nächsten Ausführungstages. Berücksichtigt wird dabei nur der
     * Zeitraum eines Jahres.
     * 
     * @param startingFrom Datum von dem ab gesucht werden soll. Null falls von "jetzt" an gesucht werden soll.
     * @return Datum des nächsten Ausführungstages innerhalb des nächsten Jahres, ansonsten null.
     */
    public Calendar getNextRun(Calendar startingFrom){
    	Calendar nextRun = Calendar.getInstance();
    	if(startingFrom != null)
    		nextRun.setTime(startingFrom.getTime());       // Startzeitpunkt ist vom user definiert.
    	else
    		nextRun.add(Calendar.MINUTE, 1);		       // nicht mehr in dieser Minute... :-)
    	nextRun.set(Calendar.SECOND, 0);                   // Sekunden ignorieren
    	
    	Calendar lastTry = Calendar.getInstance();         // Wir schauen höchstens 1 Jahr + 1 Tag in die Zukunft.
    	lastTry.setTime(nextRun.getTime());
    	lastTry.add(Calendar.YEAR, 1);            
    	lastTry.add(Calendar.DAY_OF_MONTH, 1); 

    	while(true){
            if (nextRun.after(lastTry))                    // Wir schauen höchstens 1 Jahr + 1 Tag in die Zukunft.
	        	return null;							   // kein Ergebnis
	        else if(shouldRun(nextRun.getTime()))
    			return nextRun;						       // Ergebnis gefunden
	        else {
	        	if(!month[nextRun.get(Calendar.MONTH)]){   // nicht in diesem Monat
	        		nextRun.set(Calendar.MINUTE, 0);       // Um 0Uhr0 am 1. des Folgemonats weitersuchen.
	        		nextRun.set(Calendar.HOUR_OF_DAY, 0);
	        		nextRun.set(Calendar.DAY_OF_MONTH, 1); 
	        		nextRun.add(Calendar.MONTH, 1);
	        	}
	        	else if(!dayOfWeek[nextRun.get(Calendar.DAY_OF_WEEK)-1]){    // diesen Monat, aber nicht heute. Falscher Wochentag
	        		nextRun.set(Calendar.MINUTE, 0);                         // Um 0Uhr0 am nächsten Tag weitersuchen.
	        		nextRun.set(Calendar.HOUR_OF_DAY, 0);
	        		nextRun.add(Calendar.DAY_OF_MONTH, 1);
	        	}
	        	else if(!dayOfMonth[nextRun.get(Calendar.DAY_OF_MONTH)-1]){  // diesen Monat, aber nicht heute. Falsches Datum
	        		nextRun.set(Calendar.MINUTE, 0);                         // Um 0Uhr0 am nächsten Tag weitersuchen.
	        		nextRun.set(Calendar.HOUR_OF_DAY, 0);
	        		nextRun.add(Calendar.DAY_OF_MONTH, 1);
	        	}
	        	else if(!hour[nextRun.get(Calendar.HOUR_OF_DAY)]){  // heute, aber nicht in dieser Stunde
	        		nextRun.set(Calendar.MINUTE, 0);       	        // ab Minute 0 der nächsten Stunde weitersuchen.
	        		nextRun.add(Calendar.HOUR_OF_DAY, 1);
	        	}
	        	else{
	        		nextRun.add(Calendar.MINUTE, 1);
	        	}
	        }
	    }
    }
    
        
    /**
     * Gibt an, ob Zeiten gespeichert sind.
     * @return Wahr, wenn Zeiten gespeichert sind. Falsch, wenn es niemals zu einer Ausführung kommen würden
     */
    public boolean hasValues() {
        if (hasParseErrors)
            return false;
        
        for (int i = 0; i < minute.length; i++)
            if (minute[i])
                return true;
        for (int i = 0; i < hour.length; i++)
            if (hour[i])
                return true;
        for (int i = 0; i < dayOfMonth.length; i++)
            if (dayOfMonth[i])
                return true;
        for (int i = 0; i < month.length; i++)
            if (month[i])
                return true;
        for (int i = 0; i < dayOfWeek.length; i++)
            if (dayOfMonth[i])
                return true;
        return false;
    }
    
    
    /**
     * Gibt das übergebene String-Pattern wieder aus, oder 'ParseError', wenn er sich nicht hat
     * parsen lassen
     */
    public String toString() {
        if (hasParseErrors)
            return originalTimeIntervalCron + " (deaktiviert - ParseErrors)";
        return originalTimeIntervalCron.length() == 0 ? "(deaktiviert)" : originalTimeIntervalCron;
    }


    /**
     * Gibt den analysierten String wieder aus, nur als Aufzählung (ohne "-" oder *)
     * 
     * @return der analysierte String
     */
    public String getParsedInterval() {
        StringBuilder sb = new StringBuilder();

        // Minute
        for (int i = 0; i < minute.length; i++)
            if (minute[i])
                sb.append(i + ",");
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',')
            sb.setLength(sb.length() - 1);
        sb.append("   ");

        // Stunde
        for (int i = 0; i < hour.length; i++)
            if (hour[i])
                sb.append(i + ",");
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',')
            sb.setLength(sb.length() - 1);
        sb.append("   ");

        // Tag des Monats (Werte um 1 erhöhen, da kleinster Wert 1)
        for (int i = 0; i < dayOfMonth.length; i++)
            if (dayOfMonth[i])
                sb.append((i + 1) + ",");
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',')
            sb.setLength(sb.length() - 1);
        sb.append("   ");

        // Monate (Werte ums 1 erhöhen, da kleinster Wert 1)
        for (int i = 0; i < month.length; i++)
            if (month[i])
                sb.append((i + 1) + ",");
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',')
            sb.setLength(sb.length() - 1);
        sb.append("   ");
        for (int i = 0; i < dayOfWeek.length; i++)
            if (dayOfWeek[i])
                sb.append(i + ",");
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',')
            sb.setLength(sb.length() - 1);

        if (sb.length() == 12)
            return "(deaktiviert)";
        return sb.toString();
    }
    
    /**
     * Liefert wahr, wenn es beim letzten setFields() zu Parsefehlern kam
     * 
     * @return Ob es Fehler beim Parsen des Strings beim letzten Aufruf von setFields gab.
     */
    public boolean hasParseErrors() {
        return hasParseErrors;
    }
    

    /**
     * Anaylsiert ein einzelnen Feld und setzt alle nötigen Einträge im Boolean Array dest auf true;
     * 
     * @param field String, der analysiert werden soll.
     * @param destArray Ziel array, in dem die zugehärigen Felder auf true gesetzt werden sollen
     * @param offset offset, falls das Array nicht bei 0 anfängt (Tag und Monat beginnen mit 1).
     */
    private void parseField(String field, boolean[] destArray, int offset) throws ParseException {
        // Fall 1: Stern, also alles
        if (field.equals("*")) {
            for (int i = 0; i < destArray.length; i++)
                destArray[i] = true;
            return;
        }
        
        // Fall 2, Aufzählung
        if (field.indexOf(",")>0) {
            String subField[] = field.split(",");
            for (int i = 0; i < subField.length; i++)
                parseField(subField[i], destArray, offset);
            return;
        }
        
        // Fall 3: Zeitspanne
        int index = field.indexOf("-"); 
        if (index > 0) {
            int min, max;            
            try {
                min = Integer.parseInt(field.substring(0, index));
            } catch (NumberFormatException e) {
                min = nameToNumber(field.substring(0, index));
            }
            
            try {
                max = Integer.parseInt(field.substring(index + 1));
            } catch (NumberFormatException e) {
                max = nameToNumber(field.substring(index + 1));
            }
            
            if (max < 0 || min < 0) {
                throw new ParseException("Fehler beim Parsen des Felders '" + field + "'", 0);
            }

            max = Math.min(max-offset, destArray.length);
            min = Math.max(min-offset, 0);
            
            if (max < min) {
                for (int i = max; i <= destArray.length-1; i++)
                    destArray[i] = true;
                for (int i = 0; i <= min; i++)
                    destArray[i] = true;
            } else {
                for (int i = min; i <= max; i++)
                    destArray[i] = true;
            }
            return;
        }

        // Fall 4: Einfacher Zahlen oder String-Wert
        try {
            index = Integer.parseInt(field);
            destArray[index-offset] = true;
        } catch (Exception e) {
            index = nameToNumber(field);
            if (index > -1) {
                destArray[index] = true;
            } else {
                throw new ParseException("Fehler beim Parsen des Felders '" + field + "'", 0);
            }
        }
    }
    

    private int nameToNumber(String s) {
        return (DAYNAMES.containsKey(s.toLowerCase())) ? DAYNAMES.get(s.toLowerCase()) : -1;
    }


    /**
     * Initialisert die internen Felder mit leeren Arrays
     */
    private void init() {
        minute      = new boolean[60]; 
        hour        = new boolean[24];
        dayOfMonth  = new boolean[31]; 
        month       = new boolean[12];
        dayOfWeek   = new boolean[7];
    }
    
    public static void main( String[] args )
    {
      String s = "0,15,30,45 11,12,13 * * *";
      
      TimeIntervalCron t = new  TimeIntervalCron ();
      
      t.setFields( s );
      if (t.hasParseErrors)
        System.out.println("Fehler beim Parsen des Strings");
      else
      {
        Calendar c = t.getNextRun( null );
        
        if (c == null)
          System.out.println("Keine Auführung möglich");
        else
        {
          SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy;HH:mm:ss");
          String zeit = sdf.format(c.getTime());
          
          System.out.println(zeit);
        }
      }
    }
}
