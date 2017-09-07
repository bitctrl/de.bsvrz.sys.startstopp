#!/bin/bash
if test "${JAVA_HOME}" == "" ;then java=java; else java=${JAVA_HOME}/bin/java; fi
 
$java -cp ../../de.bsvrz.pat.sysbed/de.bsvrz.pat.sysbed-runtime.jar:../../de.bsvrz.vew.bmvew/de.bsvrz.vew.bmvew.jar:../../de.bsvrz.vew.bmvew/test/de.bsvrz.vew.bmvew-test.jar:../../de.bsvrz.vew.bmvew/test/junit-4.4.jar:../../de.bsvrz.vew.bmvew/lib/commons-collections-3.2.1.jar:../../de.bsvrz.vew.bmvew/lib/de.bsvrz.sys.funclib.dambach.jar \
 -Xmx300m \
 org.junit.runner.JUnitCore \
 de.bsvrz.vew.bmvew.bmvew.AllTests 
 
# Auf das Ende von allen im Hintergrund gestarteten Prozessen warten 
wait
