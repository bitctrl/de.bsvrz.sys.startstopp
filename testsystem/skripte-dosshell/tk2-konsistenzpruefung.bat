@echo off
call einstellungen.bat

title Konsistenzpruefung

%java% ^
 -cp ..\distributionspakete\de.bsvrz.puk.config\de.bsvrz.puk.config-runtime.jar ^
 -Xmx300m ^
 de.bsvrz.puk.config.main.ConfigurationApp ^
 -konsistenzprüfung ^
 -verwaltung=..\konfiguration\verwaltungsdaten.xml ^
 %debugDefaults% ^
 -debugLevelStdErrText=INFO ^
 -debugLevelFileText=CONFIG


rem Fenster nicht sofort wieder schließen, damit eventuelle Fehler noch lesbar sind.
pause
