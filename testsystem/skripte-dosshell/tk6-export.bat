@echo off
call einstellungen.bat

title Export

rem Um alles zu exportieren, keinen Bereich angeben
rem set bereiche=

set bereiche=kb.testModell,kb.testObjekte

%java% ^
 -cp ..\distributionspakete\de.bsvrz.puk.config\de.bsvrz.puk.config-runtime.jar ^
 -Xmx300m ^
 de.bsvrz.puk.config.main.ConfigurationApp ^
 -export=%bereiche% ^
 -verzeichnis=..\versorgungsdateien ^
 -verwaltung=..\konfiguration\verwaltungsdaten.xml ^
 %debugDefaults% ^
 -debugLevelStdErrText=INFO ^
 -debugLevelFileText=CONFIG


rem Fenster nicht sofort wieder schlieﬂen, damit eventuelle Fehler noch lesbar sind.
pause