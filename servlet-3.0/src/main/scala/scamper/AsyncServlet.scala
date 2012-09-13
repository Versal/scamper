package scamper

import java.util.concurrent.Executors

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.AsyncContext

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