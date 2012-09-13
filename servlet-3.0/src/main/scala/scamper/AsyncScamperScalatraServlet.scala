package scamper

import org.scalatra.ScalatraServlet

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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
