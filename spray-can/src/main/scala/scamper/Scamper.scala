package scamper

import org.slf4j.LoggerFactory

import akka.actor.Actor
import akka.actor.Supervisor
import akka.routing.Routing
import akka.routing.CyclicIterator
import akka.config.Supervision.OneForOneStrategy
import akka.config.Supervision.Permanent
import akka.config.Supervision.Supervise
import akka.config.Supervision.SupervisorConfig
import cc.spray.can.HttpServer
import cc.spray.Directives
import cc.spray.HttpService
import cc.spray.SprayCanRootService

object Boot extends App {

  LoggerFactory.getLogger(getClass) // initialize SLF4J early

  val mainModule = new ScamperService {}

  val scamperActors = for {
    _ <- 1 to 24
    val actor = Actor.actorOf[ScamperActor]
    _ = actor.start
  } yield actor

  val httpService = Routing.loadBalancerActor(new CyclicIterator(scamperActors))
  val rootService = Actor.actorOf(new SprayCanRootService(httpService))
  val sprayCanServer = Actor.actorOf(new HttpServer())

  Supervisor(
    SupervisorConfig(
      OneForOneStrategy(List(classOf[Exception]), 3, 100),
      List(
        Supervise(httpService, Permanent),
        Supervise(rootService, Permanent),
        Supervise(sprayCanServer, Permanent))))
}

class ScamperActor extends HttpService((new ScamperService {}).scamperService)

trait ScamperService extends Directives {

  def sleep(ms: Long): Long = {
    val start = System.currentTimeMillis
    Thread.sleep(ms)
    val stop = System.currentTimeMillis
    stop - start
  }

  val scamperService = {
    path("fast") {
      get {
        completeWith {
          <h1>slept for { sleep(0) } ms</h1>
        }
      }
    } ~
      path("medium") {
        get {
          completeWith {
            <h1>slept for { sleep(150) } ms</h1>
          }
        }
      } ~
      path("slow") {
        get {
          completeWith {
            <h1>slept for { sleep(300) } ms</h1>
          }
        }
      }
  }

}