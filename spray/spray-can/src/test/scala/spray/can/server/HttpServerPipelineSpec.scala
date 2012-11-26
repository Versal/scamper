/*
 * Copyright (C) 2011-2012 spray.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spray.can.server

import com.typesafe.config.ConfigFactory
import akka.testkit.TestActorRef
import akka.actor.{ActorSystem, Actor}
import org.specs2.mutable.Specification
import spray.can.{HttpCommand, HttpPipelineStageSpec}
import spray.util.ConnectionCloseReasons._
import spray.io._
import spray.http._
import HttpHeaders.RawHeader


class HttpServerPipelineSpec extends Specification with HttpPipelineStageSpec {
  implicit val system: ActorSystem = ActorSystem()

  "The HttpServer pipeline" should {

    "dispatch a simple HttpRequest to a singleton service actor" in {
      singleHandlerPipeline.test {
        val Commands(Tell(`singletonHandler`, message, _)) = process(Received(simpleRequest))
        message === HttpRequest(headers = List(RawHeader("host", "test.com")))
      }
    }

    "dispatch an aggregated chunked requests" in {
      testPipeline(SingletonHandler(singletonHandler), requestChunkAggregation = true).test {
        val Commands(Tell(`singletonHandler`, message, _)) = process(
          Received(chunkedRequestStart),
          Received(messageChunk),
          Received(messageChunk),
          Received(chunkedMessageEnd)
        )
        message === HttpRequest(
          headers = List(
            RawHeader("transfer-encoding", "chunked"),
            RawHeader("content-type", "text/plain"),
            RawHeader("host", "test.com")
          ),
          entity = HttpBody("body123body123")
        )
      }
    }

    "dispatch SentAck messages" in {
      "to the sender of an HttpResponse" in {
        singleHandlerPipeline.test {
          val Commands(Tell(_, _, peer)) = process(Received(simpleRequest))
          peer.tell(HttpCommand(HttpResponse().withSentAck(1)), sender1)
          val Commands(Tell(receiver, 1, _)) = clearAndProcess(AckEventWithReceiver(1, sender1))
          receiver === sender1
        }
      }
      "to the senders of a ChunkedResponseStart, MessageChunk and ChunkedMessageEnd" in {
        singleHandlerPipeline.test {
          val Commands(Tell(_, _, peer)) = processAndClear(Received(simpleRequest))
          peer.tell(HttpCommand(ChunkedResponseStart(HttpResponse(entity = HttpBody(""))).withSentAck(1)), sender1)
          process(AckEventWithReceiver(1, sender1))
          peer.tell(HttpCommand(MessageChunk("part 1").withSentAck(2)), sender2)
          process(AckEventWithReceiver(2, sender2))
          peer.tell(HttpCommand(MessageChunk("part 2")), sender2)
          peer.tell(HttpCommand(MessageChunk("part 3").withSentAck(3)), sender3)
          peer.tell(HttpCommand(ChunkedMessageEnd().withSentAck(4)), sender4)
          val Commands(commands@ _*) = process(AckEventWithReceiver(3, sender3), AckEventWithReceiver(4, sender4))

          commands(0) === SendString(`chunkedResponseStart`, AckEventWithReceiver(1, sender1))
          val Tell(`sender1`, 1, _) = commands(1)
          commands(2) === SendString(prep("6\npart 1\n"), AckEventWithReceiver(2, sender2))
          val Tell(`sender2`, 2, _) = commands(3)
          commands(4) === SendString(prep("6\npart 2\n"))
          commands(5) === SendString(prep("6\npart 3\n"), AckEventWithReceiver(3, sender3))
          commands(6) === SendString(prep("0\n\n"), AckEventWithReceiver(4, sender4))
          val Tell(`sender3`, 3, _) = commands(7)
          val Tell(`sender4`, 4, _) = commands(8)
          success
        }
      }
    }

    "dispatch Closed messages" in {
      val CLOSED = Closed(`testHandle`, PeerClosed)
      "to the handler if no request is open" in {
        singleHandlerPipeline.test {
          val Commands(Tell(receiver, CLOSED, _)) = process(CLOSED)
          receiver === singletonHandler
        }
      }
      "to the handler if a request is open" in {
        singleHandlerPipeline.test {
          processAndClear(Received(simpleRequest))
          val Commands(Tell(receiver, CLOSED, _)) = process(CLOSED)
          receiver === singletonHandler
        }
      }
      "to the response sender if a response has been sent but not yet confirmed" in {
        singleHandlerPipeline.test {
          val Commands(Tell(_, _, peer)) = process(Received(simpleRequest))
          peer.tell(HttpCommand(HttpResponse().withSentAck(42)), sender1)
          val Commands(Tell(receiver, CLOSED, _)) = clearAndProcess(CLOSED)
          receiver === sender1
        }
      }
      "to the handler if a response has been sent and confirmed" in {
        singleHandlerPipeline.test {
          val Commands(Tell(_, _, peer)) = process(Received(simpleRequest))
          peer.tell(HttpCommand(HttpResponse().withSentAck(42)), sender1)
          process(AckEventWithReceiver(42, sender1))
          val Commands(Tell(receiver, CLOSED, _)) = clearAndProcess(CLOSED)
          receiver === singletonHandler
        }
      }
      "to the handler if a response has been sent without ack" in {
        singleHandlerPipeline.test {
          val Commands(Tell(_, _, peer)) = process(Received(simpleRequest))
          peer.tell(HttpCommand(HttpResponse()), sender1)
          val Commands(Tell(receiver, CLOSED, _)) = clearAndProcess(CLOSED)
          receiver === singletonHandler
        }
      }
      "to the response sender of a chunk stream if a chunk has been sent but not yet confirmed" in {
        singleHandlerPipeline.test {
          val Commands(Tell(_, _, peer)) = process(Received(simpleRequest))
          peer.tell(HttpCommand(ChunkedResponseStart(HttpResponse()).withSentAck(12)), sender1)
          val Commands(Tell(receiver, CLOSED, _)) = clearAndProcess(CLOSED)
          receiver === sender1
        }
      }
      "to the last response sender of a chunk stream if a chunk has been sent and confirmed" in {
        singleHandlerPipeline.test {
          val Commands(Tell(_, _, peer)) = process(Received(simpleRequest))
          peer.tell(HttpCommand(ChunkedResponseStart(HttpResponse())), sender1)
          peer.tell(HttpCommand(MessageChunk("bla").withSentAck(12)), sender2)
          process(AckEventWithReceiver(12, sender2))
          val Commands(Tell(receiver, CLOSED, _)) = clearAndProcess(CLOSED)
          receiver === sender2
        }
      }
      "to the last response sender if a final chunk has been sent but not yet confirmed" in {
        singleHandlerPipeline.test {
          val Commands(Tell(_, _, peer)) = process(Received(simpleRequest))
          peer.tell(HttpCommand(ChunkedResponseStart(HttpResponse()).withSentAck(1)), sender1)
          peer.tell(HttpCommand(MessageChunk("bla")), sender2)
          peer.tell(HttpCommand(ChunkedMessageEnd().withSentAck(2)), sender3)
          process(AckEventWithReceiver(2, sender3))
          val Commands(Tell(receiver, CLOSED, _)) = clearAndProcess(CLOSED)
          receiver === sender3
        }
      }
      "to the handler if a final chunk has been sent and no confirmation is open" in {
        "example 1" in {
          singleHandlerPipeline.test {
            val Commands(Tell(_, _, peer)) = process(Received(simpleRequest))
            peer.tell(HttpCommand(ChunkedResponseStart(HttpResponse())), sender1)
            peer.tell(HttpCommand(MessageChunk("bla")), sender2)
            peer.tell(HttpCommand(ChunkedMessageEnd()), sender3)
            val Commands(Tell(receiver, CLOSED, _)) = clearAndProcess(CLOSED)
            receiver === singletonHandler
          }
        }
        "example 1" in {
          singleHandlerPipeline.test {
            val Commands(Tell(_, _, peer)) = process(Received(simpleRequest))
            peer.tell(HttpCommand(ChunkedResponseStart(HttpResponse()).withSentAck(1)), sender1)
            peer.tell(HttpCommand(MessageChunk("bla")), sender2)
            peer.tell(HttpCommand(ChunkedMessageEnd().withSentAck(2)), sender3)
            process(AckEventWithReceiver(1, sender1), AckEventWithReceiver(2, sender3))
            val Commands(Tell(receiver, CLOSED, _)) = clearAndProcess(CLOSED)
            receiver === singletonHandler
          }
        }
      }
    }

    "handle 'Expected: 100-continue' headers" in {
      def example(expectValue: String) = {
        singleHandlerPipeline.test {
          val Commands(message, Tell(`singletonHandler`, request, peer)) = processAndClear {
            Received {
              prep {
                """|GET / HTTP/1.1
                  |Host: test.com
                  |Content-Type: text/plain
                  |Content-Length: 12
                  |Expect: %s
                  |
                  |bodybodybody""".format(expectValue)
              }
            }
          }
          peer.tell(HttpCommand(HttpResponse()), sender1)

          message === SendString("HTTP/1.1 100 Continue\r\n\r\n")
          request === HttpRequest(
            headers = List(
              RawHeader("expect", expectValue),
              RawHeader("content-length", "12"),
              RawHeader("content-type", "text/plain"),
              RawHeader("host", "test.com")
            )
          ).withEntity("bodybodybody")
          result.commands(0) === SendString(simpleResponse)
        }
      }
      "with a header value fully matching the spec" in example("100-continue")
      "with a header value containing illegal casing" in example("100-Continue")
    }

    "dispatch HEAD requests as GET requests and suppress sending of HttpResponse bodies" in {
      singleHandlerPipeline.test {
        val Commands(Tell(`singletonHandler`, request, peer)) = processAndClear {
          Received {
            prep {
              """|HEAD / HTTP/1.1
                |Host: test.com
                |
                |"""
            }
          }
        }
        request === HttpRequest(headers = List(RawHeader("host", "test.com")))

        peer.tell(HttpCommand(HttpResponse(entity = "1234567")), sender1)
        result.commands === Seq(
          SendString {
            prep {
            """|HTTP/1.1 200 OK
               |Server: spray/1.0
               |Date: XXXX
               |Content-Type: text/plain
               |Content-Length: 7
               |
               |"""
            }
          }
        )
      }
    }

    "dispatch HEAD requests as GET requests and suppress sending of chunked responses" in {
      singleHandlerPipeline.test {
        val Commands(Tell(`singletonHandler`, request, peer)) = processAndClear {
          Received {
            prep {
              """|HEAD / HTTP/1.1
                |Host: test.com
                |
                |"""
            }
          }
        }
        request === HttpRequest(headers = List(RawHeader("host", "test.com")))

        peer.tell(HttpCommand(ChunkedResponseStart(HttpResponse(entity = "1234567"))), sender1)
        val Seq(SendString(raw, Some(AckEventWithReceiver(IOBridge.Closed(_, CleanClose), `sender1`)))) = result.commands
        raw === prep {
        """|HTTP/1.1 200 OK
           |Server: spray/1.0
           |Date: XXXX
           |Content-Type: text/plain
           |Content-Length: 7
           |
           |"""
        }
      }
    }

    "dispatch Timeout messages in case of a request timeout (and dispatch respective response)" in {
      singleHandlerPipeline.test {
        val Commands(Tell(`singletonHandler`, _, peer)) = processAndClear(Received(simpleRequest))
        Thread.sleep(100)
        val Commands(Tell(`singletonHandler`, spray.http.Timeout(_), `peer`)) = processAndClear(TickGenerator.Tick)
        peer.tell(HttpCommand(HttpResponse()), sender1)
        result.commands(0) === SendString(simpleResponse)
      }
    }

    "dispatch the default timeout response if the Timeout timed out" in {
      singleHandlerPipeline.test {
        val Commands(Tell(`singletonHandler`, _, peer)) = processAndClear(Received(simpleRequest))
        Thread.sleep(55)
        val Commands(Tell(`singletonHandler`, spray.http.Timeout(_), `peer`)) = processAndClear(TickGenerator.Tick)
        Thread.sleep(35)
        val Commands(message, HttpServer.Close(CleanClose)) = process(TickGenerator.Tick)
        message === SendString {
          prep {
            """|HTTP/1.1 500 Internal Server Error
               |Connection: close
               |Server: spray/1.0
               |Date: XXXX
               |Content-Type: text/plain
               |Content-Length: 13
               |
               |Timeout for /"""
          }
        }
      }
    }
  }

  step(system.shutdown())

  ///////////////////////// SUPPORT ////////////////////////

  val simpleRequest = prep {
  """|GET / HTTP/1.1
     |Host: test.com
     |
     |"""
  }

  val simpleResponse = prep {
  """|HTTP/1.1 200 OK
     |Server: spray/1.0
     |Date: XXXX
     |Content-Length: 0
     |
     |"""
  }

  val connectionActor = TestActorRef(new DummyActor, "connectionActor")
  val singletonHandler = TestActorRef(new DummyActor, "singletonHandler")

  class DummyActor extends Actor {
    def receive = {
      case x: Command => currentTestPipelines.commandPipeline(x)
      case x: Event => currentTestPipelines.eventPipeline(x)
    }
    def getContext = context
  }

  override def connectionActorContext = connectionActor.underlyingActor.getContext

  val singleHandlerPipeline = testPipeline(SingletonHandler(singletonHandler))

  def testPipeline(messageHandler: MessageHandler, requestChunkAggregation: Boolean = false) = HttpServer.pipeline(
    new ServerSettings(
      ConfigFactory.parseString("""
        spray.can.server.server-header = spray/1.0
        spray.can.server.idle-timeout = 250 ms
        spray.can.server.request-timeout = 50 ms
        spray.can.server.timeout-timeout = 30 ms
        spray.can.server.reaping-cycle = 0  # don't enable the TickGenerator
        spray.can.server.pipelining-limit = 10
      """ + (if (!requestChunkAggregation) "spray.can.server.request-chunk-aggregation-limit = 0" else ""))
    ),
    messageHandler,
    req => HttpResponse(500, "Timeout for " + req.uri),
    Some(new StatsSupport.StatsHolder),
    system.log
  )
}

