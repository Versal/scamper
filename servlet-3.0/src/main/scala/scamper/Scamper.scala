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

class BasicServlet extends HttpServlet {
  override def doGet(req: HttpServletRequest, res: HttpServletResponse) =
    req.getRequestURI() match {
      case "/basic/fast" => Responder.fast(res)
      case "/basic/medium" => Responder.medium(res)
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
      case "/async/fast" =>
        req.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true)
        AsyncExecutor.execute(req.startAsync())(Responder.fast(res))
      case "/async/medium" =>
        req.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true)
        AsyncExecutor.execute(req.startAsync())(Responder.medium(res))
      case "/async/slow" =>
        req.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true)
        AsyncExecutor.execute(req.startAsync())(Responder.slow(res))
    }
}

class ScamperScalatraServlet extends ScalatraServlet {

  get("/fast") {
    contentType = "text/html"
    <h1>slept for { Responder.sleep(0) } ms</h1>
  }

  get("/medium") {
    contentType = "text/html"
    <h1>slept for { Responder.sleep(150) } ms</h1>
  }

  get("/slow") {
    contentType = "text/html"
    <h1>slept for { Responder.sleep(300) } ms</h1>
  }
}

class AsyncScamperScalatraServlet extends ScalatraServlet {

  override def handle(req: HttpServletRequest, res: HttpServletResponse) {
    AsyncExecutor.execute(req.startAsync())(super.handle(req, res))
  }

  get("/scalatra-async/fast") {
    contentType = "text/html"
    <h1>slept for { Responder.sleep(0) } ms</h1>
  }

  get("/scalatra-async/medium") {
    contentType = "text/html"
    <h1>slept for { Responder.sleep(150) } ms</h1>
  }

  get("/scalatra-async/slow") {
    contentType = "text/html"
    <h1>slept for { Responder.sleep(300) } ms</h1>
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
