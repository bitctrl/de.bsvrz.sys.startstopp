#!/bin/bash
. einstellungen.sh

# StartStopp
$java \
   -cp ../distributionspakete/de.bsvrz.sys.startstopp/de.bsvrz.sys.startstopp-runtime.jar \
 de.bsvrz.sys.startstopp.console.StartStoppConsole

# Auf das Ende von allen im Hintergrund gestarteten Prozessen warten
wait
