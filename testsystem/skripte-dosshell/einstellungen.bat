@echo off
rem  Umlaute richtig darstellen
chcp 1252
rem  In den Einstellungen des Konsolefensters muss für die korrekte Darstellung von
rem  Umlaute ausserdem ein anderer Zeichensatz eingestellt werden (z.B. Lucida Console)
echo Bitte zur korrekten Darstellung von Umlauten (öäüßÖÄÜ) den Zeichensatz Lucida Console im Konsolfenster einstellen
rem ###################################################################################
rem  Globale Einstellungen

rem  Mit JAVA_HOME wird das Verzeichnis der lokalen Java-Installation angegeben.
rem  Wenn java sich im Suchpfad befindet oder JAVA_HOME systemglobal eingestellt
rem  ist, dann muß JAVA_HOME hier nicht spezifiziert werden. JAVA_HOME kann auch zum
rem  einfachen umschalten zwischen verschiedenen Java-Umgebungen benutzt werden.
rem set JAVA_HOME=D:\Programme\Java...

rem  Mit 'benutzer' wird der Name eines konfigurierten Benutzers spezifiziert unter dem sich
rem  Applikationen beim Datenverteiler authentifizieren.
set benutzer=AutostartApplikation

rem  Mit 'dav1Host' wird die IP-Adresse oder der Domainname des ersten Datenverteilers
rem  spezifiziert. Der eingestellte Wert wird von Applikationen benutzt, um die Verbindung
rem  zum Datenverteiler herzustellen. Wenn der Datenverteiler auf dem lokalen Rechner
rem  läuft, dann kann hier auch 'localhost' oder '127.0.0.1' angegeben werden.
set dav1Host=localhost

rem  Mit 'dav1DavPort' wird der TCP-Port des ersten Datenverteilers für Verbindungen mit
rem  anderen Datenverteilern spezifiziert. Der eingestellte Wert wird vom ersten Datenverteiler
rem  für den passiven Verbindungsaufbau (Server-Socket) benutzt.
set dav1DavPort=8082

rem  Mit 'dav1AppPort' wird der TCP-Port des ersten Datenverteilers für Verbindungen mit
rem  Applikationen spezifiziert. Der eingestellte Wert wird vom ersten Datenverteiler
rem  für den passiven Verbindungsaufbau (Server-Socket) benutzt. Außerdem wird der Wert von
rem  Applikationen benutzt, die sich aktiv mit dem ersten Datenverteiler verbinden sollen.
set dav1AppPort=8083

rem  'passwortDatei' spezifiziert eine lokale Datei in dem Applikationen nach dem Passwort
rem  des Benutzers für die Authentifizierung beim Datenverteiler suchen.
set passwortDatei=passwd

rem  Die Variable 'jvmArgs' enthält die Standard-Aufrufargumente der Java Virtual Machine
rem  Nicht mehr vorgegeben werden file.encoding und die initiale Heap-Groesse
rem set jvmArgs=-showversion -Dfile.encoding=ISO-8859-1 -Xms32m
set jvmArgs=


rem ########################################################################################
rem  Die folgenden Variablen sollten nicht angepasst werden, da sie von den oben definierten
rem  Variablen abgeleitet sind.

rem  Die Variable 'authentifizierung' enthält die Aufrufargumente, die zur Authentifizierung
rem  von Applikationen beim Datenverteiler verwendet werden.
set authentifizierung=-benutzer=%benutzer% -authentifizierung=%passwortdatei%

rem Das debug-Verzeichnis soll ein Verzeichnis höher angelegt werden
set debugDefaults=-debugFilePath=..

rem  Die Variable 'dav1' enthält Standard-Argumente für Applikationen, die sich mit dem
rem  ersten Datenverteiler verbinden sollen.
set dav1=-datenverteiler=%dav1Host%:%dav1AppPort% %authentifizierung% %debugDefaults%

rem  Die Variable 'dav1OhneAuthentifizierung' enthält Standard-Argumente für Applikationen, die sich mit dem
rem  ersten Datenverteiler verbinden sollen, ohne Benutzer und Passwortdatei vorzugeben.
set dav1OhneAuthentifizierung=-datenverteiler=%dav1Host%:%dav1AppPort% %debugDefaults%

rem  Die Variable 'dav1einstellungen' enthält Einstellungen für ersten Datenverteiler selbst.
set dav1einstellungen=-davAppPort=%dav1AppPort% -davDavPort=%dav1DavPort% %debugDefaults%

rem  Die Variable 'java' enthält den Programmnamen und die Standard-Aufrufargumente
rem  der Java Virtual Machine.
rem if "%JAVA_HOME%" == "" ( set java=java) else set java=%JAVA_HOME%\bin\java
set java=java %jvmArgs%


rem if "%JAVA_HOME%" == "" ( set javac=javac) else set javac=%JAVA_HOME%\bin\javac

rem echo cp[%cp%]  authentifizierung[%authentifizierung%]  dav1[%dav1%]  java[%java%]

rem Erzeugen von Standard-Verzeichnissen, falls diese noch nicht existieren
if not exist ..\logs mkdir ..\logs

