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
package directives

import scala.util.control.NonFatal
import shapeless._
import spray.http._
import spray.util._


trait HeaderDirectives {
  import BasicDirectives._
  import RouteDirectives._

  /**
   * Extracts an HTTP header value using the given function. If the function result is undefined for all headers the
   * request is rejected with an empty rejection set. If the given function throws an exception the request is rejected
   * with a [[spray.routing.MalformedHeaderRejection]].
   */
  def headerValue[T](f: HttpHeader => Option[T]): Directive[T :: HNil] = {
    def protectedF(header: HttpHeader): Option[Either[Rejection, T]] =
      try f(header).map(Right.apply)
      catch { case NonFatal(e) => Some(Left(MalformedHeaderRejection(header.name, e))) }
    extract(_.request.headers.mapFind(protectedF)).flatMap {
      case Some(Right(a)) => provide(a)
      case Some(Left(rejection)) => reject(rejection)
      case None => reject
    }
  }

  /**
   * Extracts an HTTP header value using the given partial function. If the function is undefined for all headers the
   * request is rejected with an empty rejection set.
   */
  def headerValuePF[T](pf: PartialFunction[HttpHeader, T]): Directive[T :: HNil] = headerValue(pf.lift)

  /**
   * Extracts the value of the HTTP request header with the given name.
   * If no header with a matching name is found the request is rejected with a [[spray.routing.MissingHeaderRejection]].
   */
  def headerValueByName(headerName: String): Directive[String :: HNil] =
    headerValue(optionalValue(headerName.toLowerCase)) | reject(MissingHeaderRejection(headerName))

  /**
   * Extracts an optional HTTP header value using the given function.
   * If the given function throws an exception the request is rejected
   * with a [[spray.routing.MalformedHeaderRejection]].
   */
  def optionalHeaderValue[T](f: HttpHeader => Option[T]): Directive[Option[T] :: HNil] =
    headerValue(f).map(Some(_) :Option[T]).recoverPF {
      case Nil => provide(None)
    }

  /**
   * Extracts the value of the optional HTTP request header with the given name.
   */
  def optionalHeaderValueByName(headerName: String): Directive[Option[String] :: HNil] = {
    val f = optionalValue(headerName.toLowerCase)
    extract(_.request.headers.mapFind(f))
  }

  private def optionalValue(lowerCaseName: String): HttpHeader => Option[String] = {
    case HttpHeader(`lowerCaseName`, value) => Some(value)
    case _ => None
  }
}

object HeaderDirectives extends HeaderDirectives