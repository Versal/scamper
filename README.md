# Quick start

* Install sbt
  * https://github.com/harrah/xsbt/wiki/Getting-Started-Setup
* Download _scamper_
  * `git clone git://github.com/Versal/scamper.git`
* Run the server
  * `cd scamper/servlet`
  * `sbt "~;container:start; container:reload /"`
* Load test the server
  * `ab -c 10 -n 100 http://localhost:9002/slow`
