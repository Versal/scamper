# About

Scamper is a collection of RESTful libraries and frameworks, organized as a testbed for comparing the raw performance characteristics of each.

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

## Run and test each server

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
(cd scamper/play2 ; sbt run)
```

### [Play 2.0 mini](https://github.com/typesafehub/play2-mini)

```
(cd scamper/play2-mini ; sbt run)
```

### [spray-can](https://github.com/spray/spray-can)

```
(cd scamper/spray-can ; sbt run)
```

### Servlet 3.0

```
(cd scamper/servlet-3.0 ; sbt run)
```

#### Asynchronous Servlet 3.0

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
