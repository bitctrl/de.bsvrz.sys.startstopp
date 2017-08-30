#!/bin/bash
. einstellungen.sh

mkdir -p /tmp/GTMConfigCache

# GTM im Hintergrund starten:
$java \
 -cp ../distributionspakete/de.kappich.pat.gnd/de.kappich.pat.gnd-runtime.jar \
 -Xmx3500m \
 de.bsvrz.pat.sysbed.main.GenericTestMonitor \
 ${dav1} \
 -debugLevelStdErrText=WARNING \
 -debugLevelFileText=CONFIG \
 -plugins=de.kappich.pat.gnd.gnd.GNDPlugin \
 -lokaleSpeicherungKonfiguration=/tmp/GTMConfigCache \
 &

# Auf das Ende von allen im Hintergrund gestarteten Prozessen warten
wait
