package scamper

import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.webapp.WebAppContext
import akka.dispatch.Future
import blueeyes.core.data.ByteChunk
import blueeyes.core.data.BijectionsChunkString
import blueeyes.core.http.HttpRequest
import blueeyes.core.http.HttpResponse
import blueeyes.BlueEyesServer
import blueeyes.BlueEyesServiceBuilder
import blueeyes.ServletServer
import blueeyes.core.service.engines.servlet.ServletEngine

object NettyScamperServer extends BlueEyesServer with ScamperService

class ScamperServlet extends ServletServer with ServletEngine with ScamperService

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

object JettyScamperServer extends App {

  val server = new Server()

  val connector = new SelectChannelConnector()
  connector.setPort(9000)
  connector.setThreadPool(new QueuedThreadPool(24))

  server.addConnector(connector)

  val webapp = new WebAppContext()
  webapp.setContextPath("/")
  webapp.setWar("src/main/webapp")
  server.setHandler(webapp)

  server.start()
  server.join()
}
