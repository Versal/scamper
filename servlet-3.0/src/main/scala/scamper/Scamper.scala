package scamper

import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.webapp.WebAppContext

import javax.servlet.http.HttpServletResponse

object Responder {

  def sleep(ms: Long): Long = {
    val start = System.currentTimeMillis
    Thread.sleep(ms)
    val stop = System.currentTimeMillis
    stop - start
  }

  def fast(res: HttpServletResponse) { res.getWriter().write("<h1>slept for %d ms</h1>".format(sleep(0))) }
  def medium(res: HttpServletResponse) { res.getWriter().write("<h1>slept for %d ms</h1>".format(sleep(150))) }
  def slow(res: HttpServletResponse) { res.getWriter().write("<h1>slept for %d ms</h1>".format(sleep(300))) }
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
