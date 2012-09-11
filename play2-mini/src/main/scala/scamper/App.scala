package scamper

import com.typesafe.play.mini.Application
import com.typesafe.play.mini.GET
import com.typesafe.play.mini.Path

import play.api.mvc.Action
import play.api.mvc.PlainResult
import play.api.mvc.ResponseHeader
import play.api.mvc.SimpleResult

object App extends Application {
  
  def route = {
    case GET(Path("/simple")) => simple()
    case GET(Path("/slow")) => slow()
  }

  protected def response(code: Int, body: String, `type`: String): PlainResult = {
    new SimpleResult[String](header = ResponseHeader(code),
      body = play.api.libs.iteratee.Enumerator(body))
      .as(`type`)
      .withHeaders(("Access-Control-Allow-Origin", "*"))
  }

  def simple() = Action { implicit request =>
    response(200, "<h1>simple</h1>", "text/html")
  }

  def slow() = Action { implicit request =>
    val start = System.currentTimeMillis
    Thread.sleep(200)
    val stop = System.currentTimeMillis
    response(200, "<h1>slept for %d ms</h1>".format(stop - start), "text/html")
  }

}
