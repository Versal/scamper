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

package spray

import shapeless.{HNil, HList}


package object routing {

  type Route = RequestContext => Unit
  type RouteGenerator[T] = T => Route
  type Directive0 = Directive[HNil]

  // should actually live in file "Route.scala"
  // but can't due to https://issues.scala-lang.org/browse/SI-5031
  // will move back once the issue is fixed
  object Route {
    def apply(f: Route): Route = f

    /**
     * Converts the route into a directive that never passes the request to its inner route
     * (and always returns its underlying route).
     */
    def toDirective[L <: HList](route: Route): Directive[L] = new Directive[L] {
      def happly(f: L => Route) = route
    }
  }
}