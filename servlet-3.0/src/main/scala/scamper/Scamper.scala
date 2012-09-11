package scamper

import java.util.concurrent.Executors

import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.ScalatraServlet

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.AsyncContext

object Responder {
  def simple(res: HttpServletResponse) { res.getWriter().write("<h1>simple</h1>") }
  def slow(res: HttpServletResponse) {
    val start = System.currentTimeMillis
    Thread.sleep(200)
    val stop = System.currentTimeMillis
    res.getWriter().write("<h1>slept for %d ms</h1>".format(stop - start))
  }
}

class BasicServlet extends HttpServlet {
  override def doGet(req: HttpServletRequest, res: HttpServletResponse) =
    req.getRequestURI() match {
      case "/basic/simple" => Responder.simple(res)
      case "/basic/slow" => Responder.slow(res)
    }
}

object AsyncExecutor {
  private val execSvc = Executors.newFixedThreadPool(24)
  def runnable(f: => Any): Runnable = new Runnable { def run() { f } }
  def execute(ctx: AsyncContext)(f: => Any) =
    execSvc.execute(runnable { f; ctx.complete() })
}

class AsyncServlet extends HttpServlet {
  override def doGet(req: HttpServletRequest, res: HttpServletResponse) =
    req.getRequestURI() match {
      case "/async/simple" => Responder.simple(res)
      case "/async/slow" =>
        req.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true)
        AsyncExecutor.execute(req.startAsync())(Responder.slow(res))
    }
}

class ScamperScalatraServlet extends ScalatraServlet {

  get("/simple") {
    contentType = "text/html"
    <h1>simple</h1>
  }

  get("/slow") {
    contentType = "text/html"
    val start = System.currentTimeMillis
    Thread.sleep(200)
    val stop = System.currentTimeMillis
    <h1>slept for { stop - start } ms</h1>
  }
}

class AsyncScamperScalatraServlet extends ScalatraServlet {
  
  override def handle(req: HttpServletRequest, res: HttpServletResponse) {
    AsyncExecutor.execute(req.startAsync())(super.handle(req, res))
  }
  
  get("/scalatra-async/simple") {
    contentType = "text/html"
    <h1>simple</h1>
  }

  get("/scalatra-async/slow") {
    contentType = "text/html"
    val start = System.currentTimeMillis
    Thread.sleep(200)
    val stop = System.currentTimeMillis
    <h1>slept for { stop - start } ms</h1>
  }
}

object Launcher extends App {

  val server = new Server()

  val connector = new SelectChannelConnector()
  connector.setPort(9001)
  connector.setThreadPool(new QueuedThreadPool(24))

  server.addConnector(connector)

  val webapp = new WebAppContext()
  webapp.setContextPath("/")
  webapp.setWar("src/main/webapp")
  server.setHandler(webapp)

  server.start()
  server.join()
}
