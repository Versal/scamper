/*
 * Copyright (C) 2011-2012 spray.io
 * Based on code copyright (C) 2010-2011 by the BlueEyes Web Framework Team (http://github.com/jdegoes/blueeyes)
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
package http

sealed abstract class LanguageRange {
  def primaryTag: String
  def subTags: Seq[String]
  val value = (primaryTag +: subTags).mkString("-")
  override def toString = "LanguageRange(" + value + ')'
}

object LanguageRanges {
  
  case object `*` extends LanguageRange {
    def primaryTag = "*"
    def subTags = Seq.empty[String]
  }
  
  case class Language(primaryTag: String, subTags: String*) extends LanguageRange
  
}
