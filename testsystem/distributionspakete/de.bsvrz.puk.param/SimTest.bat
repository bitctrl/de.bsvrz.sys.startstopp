@echo off

rem #########################################################################
rem #                                                                       #
rem #       Startskript für die SWE6.3 - Umfassende Datenanalyse            #
rem #                                                                       #
rem #  Das Skript ist als Beispiel zu betrachten und muss eventuell an die  #
rem #  Gegebenheiten des lokalen Projektes angepasst werden.                #
rem #                                                                       #
rem #  Es wird angenommen, dass die Kernsoftware gem�� der allgmeinen       #
rem #  installiert wurde, insbesondere die Installation der SWE in einzel-  #
rem #  Unterverzeichnissen unter dem Verzeichnis "distributionspakete"      #
rem #  und die Existenz einer allgemeinen Einstellungsdatei                 #
rem #  "einstellungen.bat" im Verzeichnis "..\..\skripte-dosshell" relativ  #
rem #  zum Verzeichnis in dem dieses Skript residiert, in dem die allge-    #
rem #  meinen Parameter für die Datenverteilerkopplung festgelegt sind.     #
rem #                                                                       #
rem #########################################################################
rem #  Folgende Parameter die aus der Datei einstellungen.bat ermittelt     #
rem #  werden m�ssen überpr�ft und evtl. angepasst werden.                  #
rem #########################################################################
rem #  Parameter für den Java-Interpreter                                   #
rem #  jvmArgs="-Dfile.encoding=ISO-8859-1"                                 #
rem #                                                                       #
rem #  Parameter für den Datenverteiler                                     #
rem #  dav1="-datenverteiler=localhost:8083 -benutzer=Tester \              # 
rem #           -authentifizierung=passwd -debugFilePath=.."                #
rem #########################################################################

call ..\..\skripte-dosshell\einstellungen.bat

rem Titel des Shell-Fensters, in dem die Umfassende Datenanalyse ausgeführt wird.
title Parametrierung - Simulationen anlegen

rem #########################################################################
rem #                                                                       #
rem #    Parameter für die Ausf�hrung der Umfassenden Datenananalyse        #
rem #                                                                       #
rem #########################################################################

rem #########################################################################
rem #                                                                       #
rem #  der Name des auszuf�hrenden Skripts (Dateiname)                      #
rem #                                                                       #
rem #########################################################################

set UDASKRIPT=uda/simulation.uda

rem #########################################################################
rem #                                                                       #
rem #  Konsolenausgabe einschalten ?                                        #
rem #                                                                       #
rem #########################################################################

set KONSOLE=ja

rem #########################################################################
rem #                                                                       #
rem #  Der Name der Datei, in die die Ausgaben erfolgen sollen.             # 
rem #  Wenn kein Name angegeben wird und die Konsolenausgabe ausgeschaltet  #
rem #  ist wird der Name aus dem Name des Skripts gebildet.                 #
rem #                                                                       #
rem #########################################################################

set PROTOKOLLDATEI=uda/simulation.prot

rem #########################################################################
rem #                                                                       #
rem #  Zus�tzliche Parameter für den Start der Java-VM                      #
rem #                                                                       #
rem #########################################################################

set jvmargs=%jvmargs%

rem #########################################################################
rem #                                                                       #
rem #                  Ausf�hrung der Softwareeinheit                       #
rem #                                                                       #
rem #  Ab hier darf in dem Skript keine �nderung mehr vorgenommen werden.   #
rem #                                                                       #
rem #########################################################################

java %jvmargs% -cp ../de.bsvrz.ibv.uda/de.bsvrz.ibv.uda-runtime.jar de.bsvrz.ibv.uda.interpreter.UdaInterpreter %dav1% ^
	-skriptName=%UDASKRIPT% ^
	-protokollName=%PROTOKOLLDATEI% ^
	-konsolenAusgabe=%KONSOLE% ^
	-debugLevelStdErrText=INFO ^
	-debugLevelFileText=CONFIG ^
	-debugSetLoggerAndLevel=:CONFIG 

rem Nach dem Beenden warten, damit Meldungen gelesen werden k�nnen
pause
