package scamper

import org.scalatra.ScalatraServlet

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