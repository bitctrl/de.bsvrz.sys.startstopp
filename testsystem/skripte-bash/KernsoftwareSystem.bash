#!/bin/bash
. einstellungen.sh

# Datenverteiler im Hintergrund starten
$java \
 -cp ../distributionspakete/de.bsvrz.dav.dav/de.bsvrz.dav.dav-runtime.jar \
 -Xmx200m \
 de.bsvrz.dav.dav.main.Transmitter \
 ${dav1einstellungen} \
 -rechtePruefung=nein \
 -benutzer=TestDatenverteilerBenutzer \
 -authentifizierung=passwd \
 -debugLevelStdErrText=INFO \
 -debugLevelFileText=CONFIG \
 &

# Zwei Sekunden warten bis der Datenverteiler Verbindungen akzeptiert
sleep 2

# Konfiguration im Hintergrund starten
$java \
 -cp ../distributionspakete/de.bsvrz.puk.config/de.bsvrz.puk.config-runtime.jar \
 -Xmx300m \
 de.bsvrz.puk.config.main.ConfigurationApp \
 ${dav1OhneAuthentifizierung} \
 -benutzer=configuration \
 -authentifizierung=passwd \
 -verwaltung=../konfiguration/verwaltungsdaten.xml \
 -benutzerverwaltung=../konfiguration/benutzerverwaltung.xml \
 -debugLevelStdErrText=INFO \
 -debugLevelFileText=CONFIG \
 &

# Verzeichnis für Parameter anlegen, wenn noch nicht vorhanden
mkdir -p ../parameter


# Parametrierung im Hintergrund starten
$java \
 -cp ../distributionspakete/de.kappich.puk.param/de.kappich.puk.param-runtime.jar \
 de.kappich.puk.param.main.ParamApp \
 ${dav1OhneAuthentifizierung} \
 -benutzer=parameter \
 -authentifizierung=passwd \
 -sleep=200 \
 -parameterVerzeichnis=../parameter \
 -debugLevelStdErrText=WARNING \
 -debugLevelFileText=CONFIG \
 &
# -parametrierung=parametrierung.global

# Betriebsmeldungsverwaltung im Hintergrund starten
$java \
 -cp ../distributionspakete/de.kappich.vew.bmvew/de.kappich.vew.bmvew-runtime.jar \
 de.kappich.vew.bmvew.main.SimpleMessageManager \
 ${dav1} \
 -debugLevelStdErrText=WARNING \
 -debugLevelFileText=CONFIG \
 &

# Auf das Ende von allen im Hintergrund gestarteten Prozessen warten
wait
