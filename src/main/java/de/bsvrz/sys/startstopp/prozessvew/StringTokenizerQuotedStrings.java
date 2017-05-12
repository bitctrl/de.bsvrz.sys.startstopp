package de.bsvrz.sys.startstopp.prozessvew;
import java.util.Vector;


public class StringTokenizerQuotedStrings
{
  private static String argument = "  /usr/java/jdk1.6.0_01/bin/java -cp /bet/vba_koko/kernsoftware/distributionspakete/de.bsvrz.dav.daf/de.bsvrz.dav.daf.jar:/bet/vba_koko/kernsoftware/distributionspakete/de.bsvrz.sys.funclib.application/de.bsvrz.sys.funclib.application.jar de.bsvrz.sys.dcf77.zeitverw.Zeitverwaltung -datenverteiler=localhost:8085 -benutzer=Tester -authentifizierung=/bet/vba_koko/kernsoftware/skripte-dosshell/passwd -debugFilePath=/bet/vba_koko/kernsoftware -rechner=rechner.Rechner5 -zyklusZeit=-1 -ntpKlasse=de.bsvrz.sys.dcf77.ntp.realclient.RealNTPClient -ntpKlassenParameter=\"cmd=/usr/sbin/ntpq -p ---zyklusZeit=5\" -debugLevelStdErrText=CONFIG -debugLevelFileText=CONFIG -inkarnationsName=StartStopp_192.0.1.116_SSB_1_Dcf77 \"-uebergabe=Hallo Ihr\" -uebergabe1=\"Hallo Sie\" -uebergabe2=\"Hallo Sie\" \"-uebergabe3=Hallo Sie\" -uebergabe4=Hallo Sie Hallo Sieder";
  
  /**
   * Programmeinstieg.<br>
   * 
   * @param args Kommandozeilenargumente.
   */
  public static void main(String[] args)
  {
//    new StringTokenizerQuot();
    
    String[] erg = StringTokenizerQuotedStrings.parse(argument);

    for (int i = 0; i < erg.length; i++)
    {
      System.out.println("arg[" + i + "] '" + erg[i] + "'");
    }
  }

  static String[] parse(String argument)
  {
    Vector<String> argv = new Vector<String>(10);
    
    int laenge = argument.length();
    
    boolean imString = false;
    boolean inQuot = false;
    
    int j = 0;
    
    for (int i = 0; i < laenge; i++)
    {
      char zeichen = argument.charAt(i);
      
      if (!imString)
      {
        j = i;

        if (zeichen != ' ')
        {
          imString = true;
        }

        if (zeichen == '"')
        {
          inQuot = true;
        }
      }
      else
      {
        if (zeichen == '"')
        {
          inQuot = inQuot ? false : true;
        }

        if (zeichen == ' ')
        {
          if (!inQuot)
          {
            imString = false;
            
            String ergTmp = new String(argument.substring(j, i).replaceAll("\"", ""));
            
            argv.add(ergTmp);
          }
        }
      }
    }

    if (imString)
    {
      String ergTmp = new String(argument.substring(j).replaceAll("\"", ""));
      
      argv.add(ergTmp);
    }

    String[] args = new String[argv.size()];

    for (int i = 0; i != argv.size(); i++)
      args[i] = argv.elementAt(i);
    
    return args;
  }
}
