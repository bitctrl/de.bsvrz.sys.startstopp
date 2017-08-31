#!/bin/bash
. einstellungen.sh

bereiche=kb.testModell,kb.testObjekte

$java \
 -cp ../distributionspakete/de.bsvrz.puk.config/de.bsvrz.puk.config-runtime.jar \
 -Xmx300m \
 de.bsvrz.puk.config.main.ConfigurationApp \
 -import=$bereiche \
 -verzeichnis=../versorgungsdateien \
 -verwaltung=../konfiguration/verwaltungsdaten.xml \
 $debugDefaults \
 -debugLevelStdErrText=INFO \
 -debugLevelFileText=CONFIG \

