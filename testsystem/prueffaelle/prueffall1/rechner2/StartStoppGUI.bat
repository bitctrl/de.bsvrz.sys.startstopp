@echo off
call einstellungen.bat

title StartStopp

%java% ^
 -cp ../../../distributionspakete/de.bsvrz.sys.startstopp/de.bsvrz.sys.startstopp-runtime.jar ^
 de.bsvrz.sys.startstopp.console.StartStoppConsole 

rem Fenster nicht sofort wieder schließen, damit eventuelle Fehler noch lesbar sind.
pause
