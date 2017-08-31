@echo off
call einstellungen.bat

title Migrationswerkzeug

%java% ^
 -cp ..\distributionspakete\de.bsvrz.dav.daf\de.bsvrz.dav.daf-runtime.jar ^
 -Xmx300m ^
 de.bsvrz.dav.daf.userManagement.UserManagement ^
 -debugLevelStdErrText=WARNING ^
 -debugLevelFileText=CONFIG


rem Fenster nicht sofort wieder schlieﬂen, damit eventuelle Fehler noch lesbar sind.
pause