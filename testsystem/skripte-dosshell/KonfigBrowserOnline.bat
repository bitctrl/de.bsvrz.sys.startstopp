@echo off
call einstellungen.bat

title KonfigBrowser

%java% ^
 -cp ..\distributionspakete\de.kappich.pat.configBrowser\de.kappich.pat.configBrowser-runtime.jar ^
 -Xmx100m ^
 de.kappich.pat.configBrowser.main.OnlineConfigurationViewer ^
 %dav1% ^
 -debugLevelStdErrText=WARNING ^
 -debugLevelFileText=CONFIG


rem Fenster nicht sofort wieder schlieﬂen, damit eventuelle Fehler noch lesbar sind.
pause
