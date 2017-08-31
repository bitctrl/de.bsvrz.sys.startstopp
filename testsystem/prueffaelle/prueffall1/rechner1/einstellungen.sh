# ###################################################################################
#  Globale Einstellungen

#  Mit JAVA_HOME wird das Verzeichnis der lokalen Java-Installation angegeben.
#  Wenn java sich im Suchpfad befindet oder JAVA_HOME systemglobal eingestellt
#  ist, dann muß JAVA_HOME hier nicht spezifiziert werden. JAVA_HOME kann auch zum
#  einfachen umschalten zwischen verschiedenen Java-Umgebungen benutzt werden.
# JAVA_HOME=/usr/lib/java

#  Mit 'benutzer' wird der Name eines konfigurierten Benutzers spezifiziert unter dem sich
#  Applikationen beim Datenverteiler authentifizieren.
export benutzer=AutostartApplikation

#  Mit 'dav1Host' wird die IP-Adresse oder der Domainname des ersten Datenverteilers
#  spezifiziert. Der eingestellte Wert wird von Applikationen benutzt, um die Verbindung
#  zum Datenverteiler herzustellen. Wenn der Datenverteiler auf dem lokalen Rechner
#  läuft, dann kann hier auch 'localhost' oder '127.0.0.1' angegeben werden.
export dav1Host=localhost

#  Mit 'dav1DavPort' wird der TCP-Port des ersten Datenverteilers für Verbindungen mit
#  anderen Datenverteilern spezifiziert. Der eingestellte Wert wird vom ersten Datenverteiler
#  für den passiven Verbindungsaufbau (Server-Socket) benutzt.
export dav1DavPort=8082

#  Mit 'dav1AppPort' wird der TCP-Port des ersten Datenverteilers für Verbindungen mit
#  Applikationen spezifiziert. Der eingestellte Wert wird vom ersten Datenverteiler
#  für den passiven Verbindungsaufbau (Server-Socket) benutzt. Außerdem wird der Wert von
#  Applikationen benutzt, die sich aktiv mit dem ersten Datenverteiler verbinden sollen.
export dav1AppPort=8083

#  'passwortDatei' spezifiziert eine lokale Datei in dem Applikationen nach dem Passwort
#  des Benutzers für die Authentifizierung beim Datenverteiler suchen.
export passwortDatei=passwd

#  Die Variable 'jvmArgs' enthält die Standard-Aufrufargumente der Java Virtual Machine
# Nicht mehr vorgegeben werden file.encoding und die initiale Heap-Groesse
# export jvmArgs="-showversion -Dfile.encoding=ISO-8859-1 -Xms32m"
export jvmArgs="-showversion"

# ########################################################################################
#  Die folgenden Variablen sollten nicht angepasst werden, da sie von den oben definierten
#  Variablen abgeleitet sind.

#  Die Variable 'authentifizierung' enthält die Aufrufargumente, die zur Authentifizierung
#  von Applikationen beim Datenverteiler verwendet werden.
export authentifizierung="-benutzer=${benutzer} -authentifizierung=${passwortDatei}"

# Das debug-Verzeichnis soll ein Verzeichnis höher angelegt werden
export debugDefaults="-debugFilePath=.."

#  Die Variable 'dav1' enthält Standard-Argumente für Applikationen, die sich mit dem
#  ersten Datenverteiler verbinden sollen.
export dav1="-datenverteiler=${dav1Host}:${dav1AppPort} ${authentifizierung} ${debugDefaults}"

#  Die Variable 'dav1OhneAuthentifizierung' enthält Standard-Argumente für Applikationen, die sich mit dem
#  ersten Datenverteiler verbinden sollen, ohne Benutzer und Passwortdatei vorzugeben.
export dav1OhneAuthentifizierung="-datenverteiler=${dav1Host}:${dav1AppPort} ${debugDefaults}"

#  Die Variable 'dav1einstellungen' enthält Einstellungen für ersten Datenverteiler selbst.
export dav1einstellungen="-davAppPort=${dav1AppPort} -davDavPort=${dav1DavPort} ${debugDefaults}"

#  Die Variable 'java' enthält den Programmnamen und die Standard-Aufrufargumente
#  der Java Virtual Machine.
if test "${JAVA_HOME}" == "" ;then java=java; else java=${JAVA_HOME}/bin/java; fi
java="$java $jvmArgs"

if test "${JAVA_HOME}" == "" ;then javac=javac; else set javac=${JAVA_HOME}/bin/javac; fi

export JAVA_HOME
export java
export javac
# echo cp[${cp}]  authentifizierung[${authentifizierung}]  dav1[${dav1}]  java[${java}]

# Erzeugen von Standard-Verzeichnissen, falls diese noch nicht existieren
mkdir -p ../logs


