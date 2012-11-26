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

package spray.util.pimps

import java.util.concurrent.TimeUnit._
import scala.concurrent.duration.Duration
import scala.concurrent.Future
import akka.pattern.ask
import akka.actor._


class PimpedActorSystem(underlying: ActorSystem) {

  def terminationOf(subject: ActorRef): Future[Terminated] = {
    underlying.actorOf {
      Props {
        new Actor {
          var receiver: Option[ActorRef] = None

          def receive = {
            case subject: ActorRef =>
              context.watch(subject)
              receiver = Some(sender)
            case x: Terminated =>
              receiver.foreach(_ ! x)
              context.stop(self)
          }
        }
      }
    }
  }.ask(subject)(Duration(Long.MaxValue, NANOSECONDS)).mapTo[Terminated]

}
