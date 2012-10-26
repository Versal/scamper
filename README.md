# About

Scamper is a collection of RESTful libraries and frameworks, organized as a testbed for comparing the raw performance characteristics of each.

Each implementation exposes the same three endpoints:

* `GET /fast` blocks for 0 ms
* `GET /medium` blocks for 150 ms
* `GET /slow` blocks for 300 ms

Each endpoint responds with `<h1>slept for { duration } ms</h1>`, where `duration` is the actual amount of time spent blocking.

Each implementation runs on `localhost:9000` and can be tested with a variety of tools, such as [ApacheBench](http://en.wikipedia.org/wiki/ApacheBench) or [JMeter](http://jmeter.apache.org/).

# Test results

For full test results, see the [Wiki](https://github.com/Versal/scamper/wiki/Test-results).

## Summary

<table>
  <tr><th>Library</th><th>Fast Test (requests per second)</th></tr>
  <tr><td>Servlet 3.0</td><td>18772.3</td></tr>
  <tr><td>Asynchronous Servlet 3.0</td><td>17528.5</td></tr>
  <tr><td>BlueEyes (Netty)</td><td>14716.7</td></tr>
  <tr><td>spray-can</td><td>13338.7</td></tr>
  <tr><td>Scalatra</td><td>12434.7</td></tr>
  <tr><td>Play 2 mini (Prod)</td><td>9388.8</td></tr>
  <tr><td>Node.js</td><td>7401.4</td></tr>
  <tr><td>Lift</td><td>4875.2</td></tr>
  <tr><td>BlueEyes (Jetty)</td><td>4017.7</td></tr>
  <tr><td>Pinky</td><td>4008.8</td></tr>
  <tr><td>Play 2 (Dev)</td><td>1250.5</td></tr>
  <tr><td>Finatra</td><td>466.4</td></tr>
  <tr><td>Finagle</td><td>DNF</td></tr>
</table>

# Getting started

## Download scamper

```
git clone git://github.com/Versal/scamper.git
```

## Install JMeter

http://jmeter.apache.org/

## Install Node.js

```
brew install nodejs
npm install -g express
(cd scamper/node.js ; npm link express)
```

## Install sbt (0.12.0 or newer)

https://github.com/harrah/xsbt/wiki/Getting-Started-Setup

## Run and test each implementation

Each server can be tested with JMeter or ApacheBench:

```
jmeter -n -t scamper/load-test.jmx
jmeter -n -t scamper/fast-test.jmx
```

```
ab -c 5 -n 10000 127.0.0.1:9000/fast
```

### [BlueEyes](https://github.com/jdegoes/blueeyes)

```
(cd scamper/blueeyes ; sbt "run --configFile blueeyes.config")
```

### [Finagle](https://github.com/twitter/finagle)

```
(cd scamper/finagle ; sbt run)
```

### [Lift](https://github.com/lift/lift)

```
(cd scamper/lift ; sbt run)
```

### [Pinky](https://github.com/pk11/pinky)

```
(cd scamper/pinky ; sbt update run)
```

### [Play 2.0](https://github.com/playframework/Play20)

```
(cd scamper/play2 ; sbt start)
```

### [Play 2.0 mini](https://github.com/typesafehub/play2-mini)

```
(cd scamper/play2-mini ; sbt run)
```

### [spray-can](https://github.com/spray/spray-can)

```
(cd scamper/spray-can ; sbt run)
```

### [Servlet 3.0](http://jcp.org/aboutJava/communityprocess/final/jsr315/index.html)

```
(cd scamper/servlet-3.0 ; sbt run)
```

#### [Asynchronous Servlet 3.0](http://docs.oracle.com/javaee/6/api/index.html?javax/servlet/AsyncContext.html)

```
(cd scamper/servlet-3.0-async ; sbt run)
```

#### [Scalatra](https://github.com/scalatra/scalatra)

```
(cd scamper/scalatra ; sbt run)
```

### [Node.js](http://nodejs.org/)

```
(ulimit -n 4096 ; cd scamper/node.js ; node app.js)
```

### [Finatra](https://github.com/capotej/finatra)

```
(cd scamper/finatra ; sbt run)
```
