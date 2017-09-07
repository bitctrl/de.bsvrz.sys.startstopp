@echo off
call einstellungen.bat

title GenericTestMonitor

%java% ^
 -cp ..\distributionspakete\de.bsvrz.pat.sysbed\de.bsvrz.pat.sysbed-runtime.jar ^
 -Xmx2500m ^
 de.bsvrz.pat.sysbed.main.GenericTestMonitor ^
 %dav1% ^
 -debugLevelStdErrText=WARNING ^
 -debugLevelFileText=CONFIG


rem Fenster nicht sofort wieder schlieﬂen, damit eventuelle Fehler noch lesbar sind.
pause
