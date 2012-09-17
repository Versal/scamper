var exec = require('child_process').exec;

function sleep(ms, res) {
  var start = new Date().getTime();
  exec('sleep 1', { "timeout" : ms }, function (error, stdout, stderr) {
    var end = new Date().getTime();
    res.send('<h1>slept for ' + (end - start) + ' ms</h1>');
  });
}


exports.fast = function(req, res) {
  res.send('<h1>slept for 0 ms</h1>');
}

exports.medium = function(req, res) {
  sleep(150, res);
}

exports.slow = function(req, res) {
  sleep(300, res);
}

