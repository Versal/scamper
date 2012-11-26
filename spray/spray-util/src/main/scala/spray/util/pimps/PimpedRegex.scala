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

import java.util.regex.Pattern
import scala.util.matching.Regex


class PimpedRegex(regex: Regex) {

  def groupCount = {
    try {
      val field = classOf[Pattern].getDeclaredField("capturingGroupCount")
      field.setAccessible(true)
      field.getInt(regex.pattern) - 1
    } catch {
      case t: Throwable =>
        throw new RuntimeException("Could not determine regex group count: " + regex.pattern.pattern, t)
    }
  }

}