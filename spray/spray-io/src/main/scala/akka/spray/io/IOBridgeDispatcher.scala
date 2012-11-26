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

package akka.spray.io

import java.util.concurrent.TimeUnit
import com.typesafe.config.Config
import scala.concurrent.duration.{Duration, FiniteDuration}
import akka.dispatch._
import akka.actor.{ActorCell, Cell}
import akka.event.Logging.Warning
import spray.io.IOExtension


class IOBridgeDispatcherConfigurator(config: Config, prerequisites: DispatcherPrerequisites)
  extends MessageDispatcherConfigurator(config, prerequisites) {

  override def dispatcher(): MessageDispatcher =
    new IOBridgeDispatcher(
      _prerequisites = prerequisites,
      _id = config.getString("id"),
      _mailboxType = mailboxType(),
      _shutdownTimeout = Duration(config.getMilliseconds("shutdown-timeout"), TimeUnit.MILLISECONDS),
      _threadPoolConfig = ThreadPoolConfig() // we use the default config
    )
}

// a PinnedDispatcher with a SelectorWakingMailbox
// Limitation: currently doesn't unblock the IOBridge (by waking up the selector) for incoming TaskInvocations
// (executeTask method). Since the IOBridge doesn't schedule Futures to its dispatcher this appears not to be
// a problem. TODO: verify that a broken 'executeTask' is ok or override with selector waking
class IOBridgeDispatcher(
  _prerequisites: DispatcherPrerequisites,
  _id: String,
  _mailboxType: MailboxType,
  _shutdownTimeout: FiniteDuration,
  _threadPoolConfig: ThreadPoolConfig
  ) extends PinnedDispatcher(_prerequisites, null,  _id, _mailboxType, _shutdownTimeout, _threadPoolConfig) {

  override protected[akka] def createMailbox(actor: Cell) = {
    val mb = new SelectorWakingMailbox(actor.system, mailboxType.create(Some(actor.self), Some(actor.system)))
    IOExtension(actor.system).register(actor.self, mb)
    mb
  }

  protected[akka] override def unregister(actor: ActorCell) {
    super.unregister(actor)
    IOExtension(actor.system).unregister(actor.self)
  }
}
