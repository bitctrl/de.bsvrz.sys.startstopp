#!/bin/bash
. einstellungen.sh

$java \
 -cp ../distributionspakete/de.kappich.pat.configBrowser/de.kappich.pat.configBrowser-runtime.jar \
 -Xmx200m \
 de.kappich.pat.configBrowser.main.ConfigConfigurationViewer \
 -konfiguration=../konfiguration/verwaltungsdaten.xml \
 $debugDefaults \
 -debugLevelStdErrText=WARNING \
 -debugLevelFileText=CONFIG \
 &

# Auf das Ende von allen im Hintergrund gestarteten Prozessen warten
wait
