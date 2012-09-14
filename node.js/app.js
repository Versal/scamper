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

/**
 * Run Node.js Server
 */
var server = http.createServer(app).listen(3000, '127.0.0.1', function(){
    /**
     * Start Message
     */
    console.log("Node.js Web server listening on port 3000");
});


