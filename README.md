# Quick start

* Install sbt
  * https://github.com/harrah/xsbt/wiki/Getting-Started-Setup
* Download scamper
  * `git clone git://github.com/Versal/scamper.git`
* Run the servers
  * `cd scamper/servlet`
  * `sbt "~;container:start; container:reload /"`
  * `cd scamper/async`
  * `sbt "~;container:start; container:reload /"`
* Load test the servers
  * `ab -c 10 -n 100 http://localhost:9002/slow`
  * `ab -c 10 -n 100 http://localhost:9003/slow`
