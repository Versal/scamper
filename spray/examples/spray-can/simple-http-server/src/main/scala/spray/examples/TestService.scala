package spray.examples

import java.util.concurrent.TimeUnit._
import scala.concurrent.duration.Duration
import akka.pattern.ask
import akka.actor._
import spray.io.{IOBridge, IOExtension}
import spray.can.server.HttpServer
import spray.util._
import spray.http._
import HttpMethods._
import MediaTypes._


class TestService extends Actor with ActorLogging {
  implicit val timeout: akka.util.Timeout = Duration(1, "sec") // for the actor 'asks' we use below

  def receive = {
    case HttpRequest(GET, "/", _, _, _) =>
      sender ! index

    case HttpRequest(GET, "/fast", _, _, _) =>
      sender ! HttpResponse(entity = "<h1>slept for 0 ms</h1>")

    case HttpRequest(GET, "/ping", _, _, _) =>
      sender ! HttpResponse(entity = "PONG!")

    case HttpRequest(GET, "/stream", _, _, _) =>
      val peer = sender // since the Props creator is executed asyncly we need to save the sender ref
      context.actorOf(Props(new Streamer(peer, 25)))

    case HttpRequest(GET, "/server-stats", _, _, _) =>
      val client = sender
      (context.actorFor("../http-server") ? HttpServer.GetStats).onSuccess {
        case x: HttpServer.Stats => client ! statsPresentation(x)
      }

    case HttpRequest(GET, "/io-stats", _, _, _) =>
      val client = sender
      (IOExtension(context.system).ioBridge ? IOBridge.GetStats).onSuccess {
        case IOBridge.StatsMap(map) => client ! statsPresentation(map)
      }

    case HttpRequest(GET, "/crash", _, _, _) =>
      sender ! HttpResponse(entity = "About to throw an exception in the request handling actor, " +
        "which triggers an actor restart")
      throw new RuntimeException("BOOM!")

    case HttpRequest(GET, uri, _, _, _) if uri.startsWith("/timeout") =>
      log.info("Dropping request, triggering a timeout")

    case HttpRequest(GET, "/stop", _, _, _) =>
      sender ! HttpResponse(entity = "Shutting down in 1 second ...")
      context.system.scheduler.scheduleOnce(Duration(1, SECONDS), new Runnable { def run() { context.system.shutdown() } })

    case _: HttpRequest => sender ! HttpResponse(status = 404, entity = "Unknown resource!")

    case Timeout(HttpRequest(_, "/timeout/timeout", _, _, _)) =>
      log.info("Dropping Timeout message")

    case Timeout(HttpRequest(method, uri, _, _, _)) =>
      sender ! HttpResponse(
        status = 500,
        entity = "The " + method + " request to '" + uri + "' has timed out..."
      )
  }

  ////////////// helpers //////////////

  lazy val index = HttpResponse(
    entity = HttpBody(`text/html`,
      <html>
        <body>
          <h1>Say hello to <i>spray-can</i>!</h1>
          <p>Defined resources:</p>
          <ul>
            <li><a href="/ping">/ping</a></li>
            <li><a href="/stream">/stream</a></li>
            <li><a href="/server-stats">/server-stats</a></li>
            <li><a href="/io-stats">/io-stats</a></li>
            <li><a href="/crash">/crash</a></li>
            <li><a href="/timeout">/timeout</a></li>
            <li><a href="/timeout/timeout">/timeout/timeout</a></li>
            <li><a href="/stop">/stop</a></li>
          </ul>
        </body>
      </html>.toString
    )
  )

  def statsPresentation(s: HttpServer.Stats) = HttpResponse(
    entity = HttpBody(`text/html`,
      <html>
        <body>
          <h1>HttpServer Stats</h1>
          <table>
            <tr><td>uptime:</td><td>{s.uptime.formatHMS}</td></tr>
            <tr><td>totalRequests:</td><td>{s.totalRequests}</td></tr>
            <tr><td>openRequests:</td><td>{s.openRequests}</td></tr>
            <tr><td>maxOpenRequests:</td><td>{s.maxOpenRequests}</td></tr>
            <tr><td>totalConnections:</td><td>{s.totalConnections}</td></tr>
            <tr><td>openConnections:</td><td>{s.openConnections}</td></tr>
            <tr><td>maxOpenConnections:</td><td>{s.maxOpenConnections}</td></tr>
            <tr><td>requestTimeouts:</td><td>{s.requestTimeouts}</td></tr>
            <tr><td>idleTimeouts:</td><td>{s.idleTimeouts}</td></tr>
          </table>
        </body>
      </html>.toString
    )
  )

  def statsPresentation(map: Map[ActorRef, IOBridge.Stats]) = HttpResponse(
    entity = HttpBody(`text/html`,
      <html>
        <body>
          <h1>IOBridge Stats</h1>
          <table>
            {
              val data = map.toSeq.map(t => t._1.path.elements.last :: t._2.productIterator.toList).transpose
              val headers = Seq("IOBridge", "uptime", "bytesRead", "bytesWritten", "connectionsOpened", "commandsExecuted")
              headers.zip(data).map { case (header, items) =>
                <tr><td>{header}:</td>{items.map(x => <td>{x}</td>)}</tr>
              }
            }
          </table>
        </body>
      </html>.toString
    )
  )

  // simple case class whose instances we use as send confirmation message for streaming chunks
  case class Ok(remaining: Int)

  class Streamer(peer: ActorRef, count: Int) extends Actor with ActorLogging {
    log.debug("Starting streaming response ...")

    // we use the successful sending of a chunk as trigger for scheduling the next chunk
    peer ! ChunkedResponseStart(HttpResponse(entity = " " * 2048)).withSentAck(Ok(count))

    def receive = {
      case Ok(0) =>
        log.info("Finalizing response stream ...")
        peer ! MessageChunk("\nStopped...")
        peer ! ChunkedMessageEnd()
        context.stop(self)

      case Ok(remaining) =>
        log.info("Sending response chunk ...")
        context.system.scheduler.scheduleOnce(Duration(100, MILLISECONDS)) {
          peer ! MessageChunk(DateTime.now.toIsoDateTimeString + ", ").withSentAck(Ok(remaining - 1))
        }

      case HttpServer.Closed(_, reason) =>
        log.info("Canceling response stream due to {} ...", reason)
        context.stop(self)
    }
  }
}
