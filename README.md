Scamper is a collection of RESTful libraries and frameworks, organized as a testbed for comparing the raw performance characteristics of each.

Each implementation exposes the same three endpoints:

* `GET /fast` blocks for 0 ms
* `GET /medium` blocks for 150 ms
* `GET /slow` blocks for 300 ms

Each endpoint responds with `<h1>slept for { duration } ms</h1>`, where `duration` is the actual amount of time spent blocking.

Each implementation runs on `localhost:9000` and can be tested with a variety of tools, such as [weighttp](https://github.com/lighttpd/weighttp) or [JMeter](http://jmeter.apache.org/).

# Test results

For full test results, see the [Wiki](https://github.com/Versal/scamper/wiki/Test-results).

![Fast Test](https://raw.github.com/Versal/scamper/master/readme/fast-test.png)

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

Each server can be tested with JMeter or weighttp:

```
jmeter -n -t scamper/load-test.jmx
jmeter -n -t scamper/fast-test.jmx
```

```
weighttp -n 500000 -c 20 -t 4 -k http://localhost:9000/fast
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
(cd scamper/spray ; sbt "project simple-http-server" run)
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

### [Rubble](https://github.com/JamesEarlDouglas/rubble)

```
(cd scamper/rubble ; sbt run)
```
