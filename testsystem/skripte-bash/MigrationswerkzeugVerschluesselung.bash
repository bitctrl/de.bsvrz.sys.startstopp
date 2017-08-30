#!/bin/bash
. einstellungen.sh

# Migrationswerkzeug wegen Interaktionen im Vordergrund starten:
$java \
 -cp ../distributionspakete/de.bsvrz.dav.daf/de.bsvrz.dav.daf-runtime.jar \
 -Xmx300m \
 de.bsvrz.dav.daf.userManagement.UserManagement \
 -debugLevelStdErrText=WARNING \
 -debugLevelFileText=CONFIG \
 ;
