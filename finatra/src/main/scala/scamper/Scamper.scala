package scamper

import com.twitter.finatra.Controller
import com.twitter.finatra.FinatraServer

object Scamper extends App {
  val scamper = new Scamper

  FinatraServer.register(scamper)
  FinatraServer.start(port = 9000)
}

class Scamper extends Controller {

  get("/fast") { request => render.body("<h1>slept for %d ms</h1>".format(sleep(0))).header("Content-Type", "text/html") }
  get("/medium") { request => render.body("<h1>slept for %d ms</h1>".format(sleep(150))).header("Content-Type", "text/html") }
  get("/slow") { request => render.body("<h1>slept for %d ms</h1>".format(sleep(300))).header("Content-Type", "text/html") }

  def sleep(ms: Long): Long = {
    val start = System.currentTimeMillis
    Thread.sleep(ms)
    val stop = System.currentTimeMillis
    stop - start
  }
}

