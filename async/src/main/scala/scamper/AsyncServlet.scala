package scamper

import java.util.concurrent.Executors
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.concurrent.Executor

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
