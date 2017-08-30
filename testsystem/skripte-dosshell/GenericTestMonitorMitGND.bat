@echo off
call einstellungen.bat

title GenericTestMonitor

if not exist %TEMP%\GTMConfigCache mkdir %TEMP%\GTMConfigCache

%java% ^
 -cp ..\distributionspakete\de.kappich.pat.gnd\de.kappich.pat.gnd-runtime.jar ^
 -Xmx3500m ^
 de.bsvrz.pat.sysbed.main.GenericTestMonitor ^
 %dav1% ^
 -debugLevelStdErrText=WARNING ^
 -debugLevelFileText=CONFIG ^
 -plugins=de.kappich.pat.gnd.gnd.GNDPlugin ^
 -lokaleSpeicherungKonfiguration=%TEMP%\GTMConfigCache


rem Fenster nicht sofort wieder schlieﬂen, damit eventuelle Fehler noch lesbar sind.
pause
