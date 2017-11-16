@echo off 
if x%JAVA_HOME%x == xx ( set java=java) else set java=%JAVA_HOME%\bin\java
 
title JUnit 
 
%java% -cp ..\..\de.bsvrz.pat.sysbed\de.bsvrz.pat.sysbed-runtime.jar;..\..\de.bsvrz.vew.bmvew\de.bsvrz.vew.bmvew.jar;..\..\de.bsvrz.vew.bmvew\test\de.bsvrz.vew.bmvew-test.jar;..\..\de.bsvrz.vew.bmvew\test\junit-4.4.jar;..\..\de.bsvrz.vew.bmvew\lib\commons-collections-3.2.1.jar;..\..\de.bsvrz.vew.bmvew\lib\de.bsvrz.sys.funclib.dambach.jar ^
 -Xmx300m ^
 org.junit.runner.JUnitCore ^
 de.bsvrz.vew.bmvew.bmvew.AllTests 
 
echo errorlevel %errorlevel% 
 
rem Fenster nicht sofort wieder schlieen, damit eventuelle Fehler noch lesbar sind. 
pause 