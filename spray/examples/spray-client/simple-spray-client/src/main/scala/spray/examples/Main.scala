package spray.examples

import scala.util.{Success, Failure}
import akka.actor.{Props, ActorSystem}
import spray.can.client.HttpClient
import spray.client.HttpConduit
import spray.io.IOExtension
import spray.httpx.SprayJsonSupport
import spray.http._
import spray.util._


object Main extends App {
  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("simple-spray-client")
  import system.log

  // every spray-can HttpClient (and HttpServer) needs an IOBridge for low-level network IO
  // (but several servers and/or clients can share one)
  val ioBridge = IOExtension(system).ioBridge

  // create and start a spray-can HttpClient
  val httpClient = system.actorOf(
    props = Props(new HttpClient(ioBridge)),
    name = "http-client"
  )

  startExample1()

  ///////////////////////////////////////////////////

  def startExample1() {
    log.info("Getting http://github.com ...")
    // an HttpConduit gives us access to an HTTP server,
    // it manages a pool of connections to _one_ host/port combination
    val conduit = system.actorOf(
      props = Props(new HttpConduit(httpClient, "github.com")),
      name = "http-conduit-1"
    )

    // send a simple request
    val pipeline = HttpConduit.sendReceive(conduit)
    val responseFuture = pipeline(HttpRequest(method = HttpMethods.GET, uri = "/"))
    responseFuture onComplete {
      case Success(response) =>
        log.info(
          """|Response for GET request to github.com:
             |status : {}
             |headers: {}
             |body   : {}""".stripMargin,
          response.status.value, response.headers.mkString("\n  ", "\n  ", ""), response.entity.asString
        )
        system.stop(conduit) // the conduit can be stopped when all operations on it have been completed
        startExample2()

      case Failure(error) =>
        log.error(error, "Couldn't get http://github.com")
        system.shutdown() // also stops all conduits (since they are actors)
    }
  }

  def startExample2() {
    log.info("Requesting the elevation of Mt. Everest from Googles Elevation API...")
    val conduit = system.actorOf(
      props = Props(new HttpConduit(httpClient, "maps.googleapis.com")),
      name = "http-conduit-2"
    )

    import HttpConduit._
    import ElevationJsonProtocol._
    import SprayJsonSupport._
    val pipeline = sendReceive(conduit) ~> unmarshal[GoogleApiResult[Elevation]]

    val responseF = pipeline(Get("/maps/api/elevation/json?locations=27.988056,86.925278&sensor=false"))
    responseF onComplete {
      case Success(response) =>
        log.info("The elevation of Mt. Everest is: {} m", response.results.head.elevation)
        system.shutdown() // also stops all conduits (since they are actors)

      case Failure(error) =>
        log.error(error, "Couldn't get elevation")
        system.shutdown() // also stops all conduits (since they are actors)
    }
  }
}