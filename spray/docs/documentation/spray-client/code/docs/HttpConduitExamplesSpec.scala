package docs

import org.specs2.mutable.Specification

// setup
import java.util.concurrent.TimeUnit._
import scala.concurrent.duration.Duration
import scala.concurrent.Future                // setup
import akka.pattern.ask
import akka.actor._                           // setup
import spray.json.DefaultJsonProtocol
import spray.can.client.HttpClient            // setup
import spray.can.server.HttpServer
import spray.client.HttpConduit               // setup
import spray.io._                             // setup
import spray.util._                           // setup
import spray.http._                           // setup
import HttpMethods._                          // setup


class HttpConduitExamplesSpec extends Specification {
  //# setup

  implicit val system = ActorSystem()
  val ioBridge = IOExtension(system).ioBridge
  val httpClient = system.actorOf(Props(new HttpClient(ioBridge)))
  val targetHostName = "localhost" // hide
  val port = 8898                  // hide

  val conduit = system.actorOf(
    props = Props(new HttpConduit(httpClient, targetHostName, port)),
    name = "http-conduit"
  )
  //#

  step {
    val handler = system.actorOf(Props(new Actor { def receive = {
      case x@HttpRequest(POST, "/orders", _, _, _) =>
        import spray.httpx.encoding.{Gzip, Deflate}
        sender ! Deflate.encode(HttpResponse(entity = Gzip.decode(x).entity))
      case x: HttpRequest => sender ! HttpResponse(entity = x.uri)
    }}))
    val server = system.actorOf(Props(new HttpServer(ioBridge, SingletonHandler(handler))))
    server.ask(HttpServer.Bind("localhost", port))(Duration(1, SECONDS)).await
  }

  "example-1" in {
    val pipeline = HttpConduit.sendReceive(conduit)   // simple-pipeline
    val response: Future[HttpResponse] = pipeline(HttpRequest(method = GET, uri = "/"))  // response-future
    response.map(_.entity.asString).await === "/"
  }

  "example-2" in {
    import spray.httpx.encoding.{Gzip, Deflate}
    import spray.httpx.SprayJsonSupport._

    case class Order(id: Int)
    case class OrderConfirmation(id: Int)

    object MyJsonProtocol extends DefaultJsonProtocol {
      implicit val orderFormat = jsonFormat1(Order)
      implicit val orderConfirmationFormat = jsonFormat1(OrderConfirmation)
    }

    import HttpConduit._
    import MyJsonProtocol._

    val pipeline: HttpRequest => Future[OrderConfirmation] = (
      addHeader("X-My-Special-Header", "fancy-value")
      ~> addCredentials(BasicHttpCredentials("bob", "secret"))
      ~> encode(Gzip)
      ~> sendReceive(conduit)
      ~> decode(Deflate)
      ~> unmarshal[OrderConfirmation]
    )
    val confirmation: Future[OrderConfirmation] =
      pipeline(Post("/orders", Order(42)))
    confirmation.await === OrderConfirmation(42) // hide
  }

  step { system.shutdown() }
}
