package scamper

import play.api.mvc.Controller
import play.api.mvc.PlainResult
import play.api.mvc.ResponseHeader
import play.api.mvc.SimpleResult
import play.api.mvc.Action
import java.util.Date
import play.Configuration
import play.api.Play

object ScamperController extends Controller {

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
