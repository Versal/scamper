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

package spray.routing

import java.io.ByteArrayInputStream
import scala.xml.{XML, NodeSeq}
import spray.httpx.unmarshalling._
import spray.httpx.marshalling._
import spray.http._
import HttpHeaders._
import MediaTypes._
import HttpCharsets._


class MarshallingDirectivesSpec extends RoutingSpec {

  implicit val IntUnmarshaller =
    Unmarshaller[Int](ContentTypeRange(`text/xml`, `ISO-8859-2`), `text/html`, `application/xhtml+xml`) {
      case HttpBody(_, buffer) => XML.load(new ByteArrayInputStream(buffer)).text.toInt
    }

  implicit val IntMarshaller =
    Marshaller.delegate[Int, NodeSeq](`application/xhtml+xml`, ContentType(`text/xml`, `UTF-8`))(i => <int>{i}</int>)

  "The 'entityAs' directive" should {
    "extract an object from the requests entity using the in-scope Unmarshaller" in {
      Put("/", <p>cool</p>) ~> {
        entity(as[NodeSeq]) { echoComplete }
      } ~> check { entityAs[String] === "<p>cool</p>" }
    }
    "return a RequestEntityExpectedRejection rejection if the request has no entity" in {
      Put() ~> {
        entity(as[Int]) { echoComplete }
      } ~> check { rejection === RequestEntityExpectedRejection }
    }
    "return an UnsupportedRequestContentTypeRejection if no matching unmarshaller is in scope" in {
      Put("/", HttpBody(`text/css`, "<p>cool</p>")) ~> {
        entity(as[NodeSeq]) { echoComplete }
      } ~> check {
        rejection === UnsupportedRequestContentTypeRejection(
          "Expected 'text/xml' or 'text/html' or 'application/xhtml+xml'")
      }
    }
    "cancel UnsupportedRequestContentTypeRejections if a subsequent `contentAs` succeeds" in {
      Put("/", HttpBody(`text/plain`, "yeah")) ~> {
        entity(as[NodeSeq]) { _ => completeOk } ~
        entity(as[String]) { _ => validate(false, "Problem") { completeOk } }
      } ~> check { rejection === ValidationRejection("Problem") }
    }
    "extract an Option[T] from the requests HttpContent using the in-scope Unmarshaller" in {
      Put("/", <p>cool</p>) ~> {
        entity(as[Option[NodeSeq]]) { echoComplete }
      } ~> check { entityAs[String] === "Some(<p>cool</p>)" }
    }
    "extract an Option[T] as None if the request has no entity" in {
      Put() ~> {
        entity(as[Option[Int]]) { echoComplete }
      } ~> check { entityAs[String] === "None" }
    }
    "return an UnsupportedRequestContentTypeRejection if no matching unmarshaller is in scope (for Option[T]s)" in {
      Put("/", HttpBody(`text/css`, "<p>cool</p>")) ~> {
        entity(as[Option[NodeSeq]]) { echoComplete }
      } ~> check {
        rejection === UnsupportedRequestContentTypeRejection(
          "Expected 'text/xml' or 'text/html' or 'application/xhtml+xml'")
      }
    }
  }

  "The 'produce' directive" should {
    "provide a completion function converting custom objects to an HttpEntity using the in-scope marshaller" in {
      Get() ~> {
        produce(instanceOf[Int]) { prod => _ => prod(42) }
      } ~> check { body === HttpBody(ContentType(`application/xhtml+xml`, `ISO-8859-1`), "<int>42</int>") }
    }
    "return a UnacceptedResponseContentTypeRejection rejection if no acceptable marshaller is in scope" in {
      Get() ~> addHeader(Accept(`text/css`)) ~> {
        produce(instanceOf[Int]) { prod => _ => prod(42) }
      } ~> check {
        rejection === UnacceptedResponseContentTypeRejection(
          Seq(ContentType(`application/xhtml+xml`), ContentType(`text/xml`, `UTF-8`))
        )
      }
    }
    "convert the response content to an accepted charset" in {
      Get() ~> addHeader(`Accept-Charset`(`UTF-8`)) ~> {
        produce(instanceOf[String]) { prod => _ => prod("Hällö") }
      } ~> check { body === HttpBody(ContentType(`text/plain`, `UTF-8`), "Hällö") }
    }
  }

  "The 'handleWith' directive" should {
    def times2(x: Int) = x * 2
    "support proper round-trip content unmarshalling/marshalling to and from a function" in (
      Put("/", HttpBody(`text/html`, "<int>42</int>")) ~> addHeader(Accept(`text/xml`)) ~> handleWith(times2)
      ~> check { body === HttpBody(ContentType(`text/xml`, `UTF-8`), "<int>84</int>") }
    )
    "result in UnsupportedRequestContentTypeRejection rejection if there is no unmarshaller supporting the requests charset" in (
      Put("/", HttpBody(`text/xml`, "<int>42</int>")) ~> addHeader(Accept(`text/xml`)) ~> handleWith(times2)
      ~> check { rejection === UnsupportedRequestContentTypeRejection("Expected 'text/xml; charset=ISO-8859-2' or 'text/html' or 'application/xhtml+xml'") }
    )
    "result in an UnacceptedResponseContentTypeRejection rejection if there is no marshaller supporting the requests Accept-Charset header" in (
      Put("/", HttpBody(`text/html`, "<int>42</int>")) ~> addHeaders(Accept(`text/xml`), `Accept-Charset`(`UTF-16`)) ~>
      handleWith(times2) ~> check {
        rejection === UnacceptedResponseContentTypeRejection(
          Seq(ContentType(`application/xhtml+xml`), ContentType(`text/xml`, `UTF-8`))
        )
      }
    )
  }

}