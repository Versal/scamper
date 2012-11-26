package scamper

import com.twitter.finatra.Controller
import com.twitter.finatra.FinatraServer

object Scamper extends App {
  val scamper = new Scamper

  FinatraServer.register(scamper)
  FinatraServer.start()
}

class Scamper extends Controller {

  get("/fast") { request => render.body("<h1>slept for %d ms</h1>".format(sleep(0))).header("Content-Type", "text/html").toFuture }
  get("/medium") { request => render.body("<h1>slept for %d ms</h1>".format(sleep(150))).header("Content-Type", "text/html").toFuture }
  get("/slow") { request => render.body("<h1>slept for %d ms</h1>".format(sleep(300))).header("Content-Type", "text/html").toFuture }

  def sleep(ms: Long): Long = {
    val start = System.currentTimeMillis
    Thread.sleep(ms)
    val stop = System.currentTimeMillis
    stop - start
  }
}

