@echo off
call einstellungen.bat

title Import

set bereiche=kb.testModell,kb.testObjekte,kb.testRechner,kb.testUsv

%java% ^
 -cp ..\distributionspakete\de.bsvrz.puk.config\de.bsvrz.puk.config-runtime.jar ^
 -Xmx300m ^
 de.bsvrz.puk.config.main.ConfigurationApp ^
 -import=%bereiche% ^
 -verzeichnis=..\versorgungsdateien ^
 -verwaltung=..\konfiguration\verwaltungsdaten.xml ^
 %debugDefaults% ^
 -debugLevelStdErrText=INFO ^
 -debugLevelFileText=CONFIG


rem Fenster nicht sofort wieder schlieﬂen, damit eventuelle Fehler noch lesbar sind.
pause