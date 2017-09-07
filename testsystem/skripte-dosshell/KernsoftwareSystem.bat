@echo off
call einstellungen.bat

title KernsoftwareSystem

rem Um einzelne Programme in eigenen Console-Fenstern zu starten, kann man
rem  einfach das "/b" hinter dem jeweiligen "start" Befehl entfernen

rem Datenverteiler im Hintergrund starten
start /b %java% ^
 -cp ..\distributionspakete\de.bsvrz.dav.dav\de.bsvrz.dav.dav-runtime.jar ^
 -Xmx200m ^
 de.bsvrz.dav.dav.main.Transmitter ^
 %dav1einstellungen% ^
 -rechtePruefung=nein ^
 -benutzer=TestDatenverteilerBenutzer ^
 -warteAufParametrierung=nein ^
 -authentifizierung=passwd ^
 -debugLevelStdErrText=INFO ^
 -debugLevelFileText=CONFIG

rem Zwei Sekunden warten bis der Datenverteiler Verbindungen akzeptiert
%java% ^
 -cp ..\distributionspakete\de.kappich.tools.sleep\de.kappich.tools.sleep-runtime.jar ^
 de.kappich.tools.sleep.main.Sleep pause=2s

rem Konfiguration im Hintergrund starten
start /b %java% ^
 -cp ..\distributionspakete\de.bsvrz.puk.config\de.bsvrz.puk.config-runtime.jar ^
 -Xmx300m ^
 de.bsvrz.puk.config.main.ConfigurationApp ^
 %dav1OhneAuthentifizierung% ^
 -benutzer=configuration ^
 -authentifizierung=passwd ^
 -verwaltung=..\konfiguration\verwaltungsdaten.xml ^
 -benutzerverwaltung=..\konfiguration\benutzerverwaltung.xml ^
 -debugLevelStdErrText=INFO ^
 -debugLevelFileText=CONFIG

rem Verzeichnis für Parameter anlegen, wenn noch nicht vorhanden
if not exist ..\parameter mkdir ..\parameter

rem Parametrierung im Hintergrund starten
start /b %java% -jar ..\distributionspakete\de.bsvrz.puk.param\de.bsvrz.puk.param-runtime.jar ^
 %dav1OhneAuthentifizierung% ^
 -benutzer=parameter ^
 -authentifizierung=passwd ^
 -persistenzModul=de.bsvrz.puk.param.param.DerbyPersistenz ^
 -persistenz=..\parameter ^
 -cacheGroesse=200000 ^
 -oldDefault=nein ^
 -debugLevelStdErrText=WARNING ^
 -debugLevelFileText=FINE
 rem -parametrierung=parametrierung.global

rem Betriebsmeldungsverwaltung im Hintergrund starten
start /b %java% ^
 -cp ..\distributionspakete\de.kappich.vew.bmvew\de.kappich.vew.bmvew-runtime.jar ^
 de.kappich.vew.bmvew.main.SimpleMessageManager ^
 %dav1% ^
 -debugLevelStdErrText=WARNING ^
 -debugLevelFileText=CONFIG

rem Fenster nicht sofort wieder schließen, damit eventuelle Fehler noch lesbar sind.
pause
