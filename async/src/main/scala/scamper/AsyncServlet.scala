package scamper

import java.util.concurrent.Executors

import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.webapp.WebAppContext

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

object AsyncServlet {
  val exec = Executors.newFixedThreadPool(100)
}

class AsyncServlet extends HttpServlet {

  override def doGet(req: HttpServletRequest, res: HttpServletResponse) =
    req.getRequestURI() match {
      case "/simple" => res.getWriter().write("<h1>simple</h1>")
      case "/slow" =>
        val ctx = req.startAsync()

        AsyncServlet.exec.execute(new Runnable {
          def run() {
            val start = System.currentTimeMillis
            Thread.sleep(200)
            val stop = System.currentTimeMillis
            res.getWriter().write("<h1>slept for %d ms</h1>".format(stop - start))
            ctx.complete()
          }
        })
    }
}

object Runner extends App {

  val server = new Server()

  val connector = new SelectChannelConnector()
  connector.setHost("127.0.0.1")
  connector.setPort(9003)
  connector.setThreadPool(new QueuedThreadPool(10))

  server.addConnector(connector)

  val webapp = new WebAppContext()
  webapp.setContextPath("/")
  webapp.setWar("src/main/webapp")
  server.setHandler(webapp)

  server.start()
  server.join()

}
