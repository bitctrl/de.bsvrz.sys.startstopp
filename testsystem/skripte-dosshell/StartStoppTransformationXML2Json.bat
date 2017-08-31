@echo off
call einstellungen.bat

title Transformation XML2Json

%java% ^
 -cp ..\distributionspakete\de.bsvrz.sys.startstopp\de.bsvrz.sys.startstopp-runtime.jar ^
 de.bsvrz.sys.startstopp.console.StartStoppKonverter ^
 -input=../startstopp/startStopp01_1.xml ^
 -output=../startstopp/startStopp01_1.json -force

rem Fenster nicht sofort wieder schließen, damit eventuelle Fehler noch lesbar sind.
pause
