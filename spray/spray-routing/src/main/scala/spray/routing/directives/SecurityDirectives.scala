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

import scala.concurrent.{ExecutionContext, Future}
import shapeless._
import spray.routing.authentication._
import BasicDirectives._
import RouteDirectives._


trait SecurityDirectives {

  /**
   * Wraps its inner Route with authentication support.
   */
  def authenticate[T](am: AuthMagnet[T]): Directive[T :: HNil] = {
    implicit def executor = am.executor
    am.value.unwrapFuture.flatMap {
      case Right(user) => provide(user)
      case Left(rejection) => reject(rejection)
    }
  }

  /**
   * Applies the given authorization check to the request.
   * If the check fails the route is rejected with an [[spray.AuthorizationFailedRejection]].
   */
  def authorize(check: => Boolean): Directive0 = authorize(_ => check)

  /**
   * Applies the given authorization check to the request.
   * If the check fails the route is rejected with an [[spray.AuthorizationFailedRejection]].
   */
  def authorize(check: RequestContext => Boolean): Directive0 =
    extract(check).flatMap(if (_) pass else reject(AuthorizationFailedRejection))

}

class AuthMagnet[T](val value: Directive[Future[Authentication[T]] :: HNil], val executor: ExecutionContext)

object AuthMagnet {
  implicit def fromFutureAuth[T](auth: Future[Authentication[T]])(implicit executor: ExecutionContext) =
    new AuthMagnet(provide(auth), executor)

  implicit def fromContextAuthenticator[T](auth: ContextAuthenticator[T])(implicit executor: ExecutionContext) =
    new AuthMagnet(extract(auth), executor)
}