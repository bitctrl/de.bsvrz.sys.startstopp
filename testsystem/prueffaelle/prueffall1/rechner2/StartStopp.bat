@echo off
call einstellungen.bat

title StartStopp

%java% ^
 -cp ../../../distributionspakete/de.bsvrz.sys.startstopp/de.bsvrz.sys.startstopp-runtime.jar ^
 de.bsvrz.sys.startstopp.startstopp.StartStopp ^
 -benutzerKonfiguration=../../../konfiguration/benutzerverwaltung.xml ^
 -authentifizierung=passwd ^
 -betriebsMeldungVersenden=nein

rem Fenster nicht sofort wieder schließen, damit eventuelle Fehler noch lesbar sind.
pause
