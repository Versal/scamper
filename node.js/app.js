/**
 * Nodejs Scamper
 * 
 * @package Application
 * @author  Alex Zelensky
 */

/**
 * Require Modules
 */
var express = require('express'),
    http = require('http'),
    scamper = require('./api/scamper');

/**
 * Create Application
 */
var app = express();

/**
 *  Application Routers
 */
//sleep 0ms
app.get('/fast', scamper.fast);

//sleep 150ms
app.get('/medium', scamper.medium);

//sleep 300ms
app.get('/slow', scamper.slow);


// cluster module
// http://nodejs.org/api/cluster.html#cluster_cluster

var cluster = require('cluster');
var http = require('http');
var numCPUs = require('os').cpus().length;

if (cluster.isMaster) {
  // Fork workers.
  for (var i = 0; i < numCPUs; i++) {
    cluster.fork();
  }

  cluster.on('exit', function(worker, code, signal) {
    console.log('worker ' + worker.process.pid + ' died');
  });
} else {
  // Workers can share any TCP connection
  // In this case its a HTTP server
  http.createServer(app).listen(9000, '127.0.0.1', function(){
    console.log("Node.js Web server listening on port 9000");
  });
}
