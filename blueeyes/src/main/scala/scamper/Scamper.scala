package scamper

import akka.dispatch.Future

import blueeyes.BlueEyesServiceBuilder
import blueeyes.core.http.{HttpRequest, HttpResponse, HttpStatus}
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.core.data.{ByteChunk, BijectionsChunkString}

import blueeyes.BlueEyesServer

object ScamperServer extends BlueEyesServer with ScamperService

trait ScamperService extends BlueEyesServiceBuilder with BijectionsChunkString {
  
  def sleep(ms: Long): Long = {
    val start = System.currentTimeMillis
    Thread.sleep(ms)
    val stop = System.currentTimeMillis
    stop - start
  }

  val scamperService = service("scamperService", "1.0.0") {
    context =>
      startup {
        Future { () }
      } ->
      request { config: Unit =>
        path("/fast") {
          (request: HttpRequest[ByteChunk]) =>
              Future {
                HttpResponse[ByteChunk](content = Some("<h1>slept for %d ms</h1>".format(sleep(0))))
              }
        } ~
        path("/medium") {
          (request: HttpRequest[ByteChunk]) =>
              Future {
                HttpResponse[ByteChunk](content = Some("<h1>slept for %d ms</h1>".format(sleep(150))))
              }
        } ~
        path("/slow") {
          (request: HttpRequest[ByteChunk]) =>
              Future {
                HttpResponse[ByteChunk](content = Some("<h1>slept for %d ms</h1>".format(sleep(300))))
              }
        }
      } ->
      shutdown { config =>
        println("Shutting down")
        Future { () }
      }
  }
}

