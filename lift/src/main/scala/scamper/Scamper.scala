package scamper

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.GetRequest
import net.liftweb.http.Req

object Scamper extends RestHelper {

  // https://www.assembla.com/wiki/show/liftweb/REST_Web_Services

  serve {
    case Req("fast" :: _, _, GetRequest) => <h1>slept for { sleep(0) } ms</h1>
    case Req("medium" :: _, _, GetRequest) => <h1>slept for { sleep(150) } ms</h1>
    case Req("slow" :: _, _, GetRequest) => <h1>slept for { sleep(300) } ms</h1>
  }

  def sleep(ms: Long): Long = {
    val start = System.currentTimeMillis
    Thread.sleep(ms)
    val stop = System.currentTimeMillis
    stop - start
  }

}
