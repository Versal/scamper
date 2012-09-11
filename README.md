## Quick start

### Install sbt

https://github.com/harrah/xsbt/wiki/Getting-Started-Setup

### Download scamper

```
git clone git://github.com/Versal/scamper.git
```

### Run the servers

```
cd scamper/servlet-3.0
sbt run
```

```
cd scamper/play-2
sbt run
```

### Load test the servers

```
ab -c 10 -n 100 http://localhost:9000/slow
```

```
ab -c 10 -n 100 http://localhost:9001/basic/slow
ab -c 10 -n 100 http://localhost:9001/async/slow
ab -c 10 -n 100 http://localhost:9001/scalatra/slow
```
