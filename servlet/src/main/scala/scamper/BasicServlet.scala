package scamper

import org.mortbay.jetty.nio.SelectChannelConnector
import org.mortbay.jetty.webapp.WebAppContext
import org.mortbay.jetty.Server
import org.mortbay.thread.QueuedThreadPool

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class BasicServlet extends HttpServlet {

  override def doGet(req: HttpServletRequest, res: HttpServletResponse) =
    req.getRequestURI() match {
      case "/simple" => res.getWriter().write("<h1>simple</h1>")
      case "/slow" =>
        val start = System.currentTimeMillis
        Thread.sleep(200)
        val stop = System.currentTimeMillis
        res.getWriter().write("<h1>slept for %d ms</h1>".format(stop - start))
    }
}

object Runner extends App {

  val server = new Server()

  val connector = new SelectChannelConnector()
  connector.setHost("127.0.0.1")
  connector.setPort(9002)
  connector.setThreadPool(new QueuedThreadPool(10))

  server.addConnector(connector)

  val webapp = new WebAppContext()
  webapp.setContextPath("/")
  webapp.setWar("src/main/webapp")
  server.setHandler(webapp)

  server.start()
  server.join()

}
