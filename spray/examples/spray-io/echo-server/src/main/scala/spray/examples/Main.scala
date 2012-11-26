package spray.examples

import java.util.concurrent.TimeUnit._
import scala.concurrent.duration.Duration
import akka.actor.{ActorRef, Props, ActorSystem}
import akka.pattern.ask
import spray.util._
import spray.io._


object Main extends App {
  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("echo-server")

  // create and start an IOBridge
  val ioBridge = IOExtension(system).ioBridge

  // and our actual server "service" actor
  val server = system.actorOf(
    Props(new EchoServer(ioBridge)),
    name = "echo-server"
  )

  // we bind the server to a port on localhost and hook
  // in a continuation that informs us when bound
  server
    .ask(IOServer.Bind("localhost", 23456))(Duration(1, SECONDS))
    .onSuccess { case IOServer.Bound(endpoint, _) =>
    println("\nBound echo-server to " + endpoint)
    println("Run `telnet localhost 23456`, type something and press RETURN. Type `STOP` to exit...\n")
  }
}

class EchoServer(ioBridge: ActorRef) extends IOServer(ioBridge) {

  override def receive = super.receive orElse {
    case IOBridge.Received(handle, buffer) =>
      buffer.array.asString.trim match {
        case "STOP" =>
          ioBridge ! IOBridge.Send(handle, BufferBuilder("Shutting down...").toByteBuffer)
          log.info("Shutting down")
          context.system.shutdown()
        case x =>
          log.debug("Received '{}', echoing ...", x)
          ioBridge ! IOBridge.Send(handle, buffer, Some('SentOk))
      }

    case 'SentOk =>
      log.debug("Send completed")

    case IOBridge.Closed(_, reason) =>
      log.debug("Connection closed: {}", reason)
  }

}