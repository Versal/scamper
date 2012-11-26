package com.earldouglas.rubble.tests

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.webapp.WebAppContext

import com.earldouglas.rubble.Rubble._

import javax.servlet.http._

object RubbleTests extends App {
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

class ExampleServlet extends HttpServlet {
  override def doGet(req: HttpServletRequest, res: HttpServletResponse) =
    req.uri match {
      case "/fast"    => res.respond(body = "<h1>slept for { duration } ms</h1>")
      case _          => res.respond(status = 404)
    }
}

