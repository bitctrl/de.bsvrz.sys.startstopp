var hooks = require('hooks');
var before = hooks.before;

before("StartStopp > StartStopp Applikation beenden > StartStopp-Applikation beenden", function (transaction) {
  transaction.skip = true;
});