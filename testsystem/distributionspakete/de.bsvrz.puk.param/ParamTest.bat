@ECHO OFF

REM ############################################################################
REM Folgende Parameter m�ssen überpr�ft und evtl. angepasst werden

REM Argumente für die Java Virtual Machine
SET jvmArgs=-showversion -Dfile.encoding=ISO-8859-1 -Xmx128m

REM ############################################################################
REM Ab hier muss nichts mehr angepasst werden

REM Java-Klassenpfad
SET cp=de.bsvrz.puk.param-runtime.jar;de.bsvrz.puk.param-test.jar

REM Applikation starten
CHCP 1252
TITLE Pr�ffall Parametrierung
java %jvmArgs% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.puk.param.TestSpezifikation
	
REM Nach dem Beenden warten, damit Meldungen gelesen werden k�nnen
PAUSE
