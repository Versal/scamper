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

package spray.site

import com.typesafe.config.ConfigFactory
import spray.util.ConfigUtils


object SiteSettings {
  private val c = ConfigUtils.prepareSubConfig(ConfigFactory.load(), "spray.site")

  val Interface    = c getString  "interface"
  val Port         = c getInt     "port"
  val DevMode      = c getBoolean "dev-mode"
  val RepoDirs     = (c getString "repo-dirs").split(':').toList
  val NightliesDir = c getString "nightlies-dir"

  require(Interface.nonEmpty, "interface must be non-empty")
  require(0 < Port && Port < 65536, "illegal port")
}