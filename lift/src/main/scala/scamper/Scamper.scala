package scamper

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.GetRequest
import net.liftweb.http.Req
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.util.thread.QueuedThreadPool

object Scamper extends RestHelper {

  // https://www.assembla.com/wiki/show/liftweb/REST_Web_Services

  serve {
    case Req("fast" :: _, _, GetRequest) => <h1>slept for { sleep(0) } ms</h1>
    case Req("medium" :: _, _, GetRequest) => <h1>slept for { sleep(150) } ms</h1>
    case Req("slow" :: _, _, GetRequest) => <h1>slept for { sleep(300) } ms</h1>
  }

  def sleep(ms: Long): Long = {
    val start = System.currentTimeMillis
    Thread.sleep(ms)
    val stop = System.currentTimeMillis
    stop - start
  }

}

object Launcher extends App {

  val server = new Server()

  val connector = new SelectChannelConnector()
  connector.setPort(9000)
  connector.setThreadPool(new QueuedThreadPool(24))

  server.addConnector(connector)

  val webapp = new WebAppContext()
  webapp.setContextPath("/")
  webapp.setWar("src/main/webapp")
  server.setHandler(webapp)

  server.start()
  server.join()
}
