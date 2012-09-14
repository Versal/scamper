# Quick start

## Download JMeter

http://jmeter.apache.org/

## Install Node.js

```
brew install nodejs
npm install -g express
(cd scamper/node.js ; npm link express)
```

## Install sbt

https://github.com/harrah/xsbt/wiki/Getting-Started-Setup

## Download scamper

```
git clone git://github.com/Versal/scamper.git
```

## Run and test each server


### [BlueEyes](https://github.com/jdegoes/blueeyes)

```
(cd scamper/blueeyes ; sbt "run --configFile blueeyes.config")
```

```
jmeter -n -t scamper/blueeyes/load-test.jmx
```

### [Finagle](https://github.com/twitter/finagle)

```
(cd scamper/finagle ; sbt run)
```

```
jmeter -n -t scamper/finagle/load-test.jmx
```

### [Lift](https://github.com/lift/lift)

```
(cd scamper/lift ; sbt "~container:start")
```

```
jmeter -n -t scamper/lift/load-test.jmx
```

### [Pinky](https://github.com/pk11/pinky)

```
(cd scamper/pinky ; sbt update ~jetty-run)
```

```
jmeter -n -t scamper/pinky/load-test.jmx
```

### [Play 2.0](https://github.com/playframework/Play20)

```
(cd scamper/play2 ; sbt run)
```

```
jmeter -n -t scamper/play2/load-test.jmx
```

### [Play 2.0 mini](https://github.com/typesafehub/play2-mini)

```
(cd scamper/play2-mini ; sbt run)
```

```
jmeter -n -t scamper/play2-mini/load-test.jmx
```

### [spray-can](https://github.com/spray/spray-can)

```
(cd scamper/spray-can ; sbt run)
```

```
jmeter -n -t scamper/spray-can/load-test.jmx
```

### Servlet 3.0

```
(cd scamper/servlet-3.0 ; sbt run)
```

```
jmeter -n -t scamper/servlet-3.0/basic-load-test.jmx
jmeter -n -t scamper/servlet-3.0/async-load-test.jmx
jmeter -n -t scamper/servlet-3.0/scalatra-load-test.jmx
jmeter -n -t scamper/servlet-3.0/scalatra-async-load-test.jmx
```

### Node.js

```
(cd scamper/node.js ; node app.js)
```

```
jmeter -n -t scamper/node.js/load-test.jmx
```