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

package spray.http
package parser

import org.parboiled.scala._
import org.parboiled.errors.ParsingException
import BasicRules._

// direct implementation of http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html
private[parser] trait ProtocolParameterRules {
  this: Parser =>

  /* 3.1 HTTP Version */
  
  def HttpVersion = rule { "HTTP" ~ "/" ~ oneOrMore(Digit) ~ "." ~ oneOrMore(Digit) }

  
  /* 3.3 Date/Time Formats */
  
  /* 3.3.1 Full Date */
  
  def HttpDate: Rule1[DateTime] = rule { (RFC1123Date | RFC850Date | ASCTimeDate) ~ OptWS }
  
  def RFC1123Date = rule {
    Wkday ~ str(", ") ~ Date1 ~ ch(' ') ~ Time ~ ch(' ') ~ str("GMT") ~~> {
      (wkday, day, month, year, hour, min, sec) => createDateTime(year, month, day, hour, min, sec, wkday)
    }
  }
  
  def RFC850Date = rule {
    Weekday ~ str(", ") ~ Date2 ~ ch(' ') ~ Time ~ ch(' ') ~ str("GMT") ~~> {
      (wkday, day, month, year, hour, min, sec) => createDateTime(year, month, day, hour, min, sec, wkday)
    }
  }
  
  def ASCTimeDate = rule {
    Wkday ~ ch(' ') ~ Date3 ~ ch(' ') ~ Time ~ ch(' ') ~ Digit4 ~~> {
      (wkday, month, day, hour, min, sec, year) => createDateTime(year, month, day, hour, min, sec, wkday)
    }
  }

  private def createDateTime(year: Int, month: Int, day: Int, hour: Int, min: Int, sec: Int, wkday: Int) = {
    val dt = DateTime(year, month, day, hour, min, sec)
    if (dt.weekday != wkday)
      throw new ParsingException("Illegal weekday in date: is '" + DateTime.WEEKDAYS(wkday) +
        "' but should be '" + DateTime.WEEKDAYS(dt.weekday) + "')" + dt)
    dt
  }

  def Date1 = rule { Digit2 ~ ch(' ') ~ Month ~ ch(' ') ~ Digit4 }
  
  def Date2 = rule { Digit2 ~ ch('-') ~ Month ~ ch('-') ~ Digit4 }
  
  def Date3 = rule { Month ~ ch(' ') ~ (Digit2 | ch(' ') ~ Digit ~> (_.toInt)) }
  
  def Time = rule { Digit2 ~ ch(':') ~ Digit2 ~ ch(':') ~ Digit2 }
  
  def Wkday = rule { stringIndexRule(0, "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat") }
  
  def Weekday = rule { stringIndexRule(0, "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday") }
  
  def Month = rule { stringIndexRule(1, "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec") }

  def Digit2 = rule { group(Digit ~ Digit) ~> (_.toInt) }

  def Digit4 = rule { group(Digit ~ Digit ~ Digit ~ Digit) ~> (_.toInt) }

  private def stringIndexRule(indexDelta: Int, strings: String*) = strings.zipWithIndex.map {
    case (s, ix) => str(s) ~ push(ix + indexDelta)
  } reduce(_ | _)

  /* 3.3.2 Delta Seconds */
  
  def DeltaSeconds = rule { oneOrMore(Digit) ~> (_.toLong) }
  
  
  /* 3.4 Character Sets */
  
  def Charset = rule { Token }
  
  
  /* 3.5 Content Codings */
  
  def ContentCoding = rule { Token }
  
  
  /* 3.6 Transfer Codings */
  
  def TransferCoding = rule { "chunked" | TransferExtension ~ DROP2 }
  
  def TransferExtension = rule { Token ~ zeroOrMore(";" ~ Parameter) }
  
  def Parameter = rule { Attribute ~ "=" ~ Value ~~> ((_, _)) }
  
  def Attribute = rule { Token }
  
  def Value = rule { Token | QuotedString }
  
  /* 3.6.1 Chunked Transfer Codings */
  
  // TODO: implement chunked transfers
  
  
  /* 3.7 Media Types */
  
  def MediaTypeDef: Rule3[String, String, Map[String, String]] = rule {
    Type ~ "/" ~ Subtype ~ zeroOrMore(";" ~ Parameter) ~~> (_.toMap)
  } 
  
  def Type = rule { Token }
  
  def Subtype = rule { Token }
  
  
  /* 3.8 Product Tokens */
  
  def Product = rule { Token ~ optional("/" ~ ProductVersion) }
  
  def ProductVersion = rule { Token }
  
  
  /* 3.9 Quality Values */
  
  def QValue = rule (
      // more loose than the spec which only allows 1 to max. 3 digits/zeros
      ch('0') ~ optional(ch('.') ~ zeroOrMore(Digit)) ~ OptWS
    | ch('1') ~ optional(ch('.') ~ zeroOrMore(ch('0'))) ~ OptWS
  )
  
  
  /* 3.10 Language Tags */
  
  def LanguageTag = rule { PrimaryTag ~ zeroOrMore("-" ~ SubTag) }
  
  // more loose than the spec which only allows 1 to max. 8 alphas
  def PrimaryTag = rule { oneOrMore(Alpha) ~> identityFunc ~ OptWS }
  
  // more loose than the spec which only allows 1 to max. 8 alphas
  def SubTag = rule { oneOrMore(Alpha) ~> identityFunc ~ OptWS }
  
  
  /* 3.11 Entity Tags */
  
  def EntityTag = rule { optional("W/") ~ OpaqueTag }
  
  def OpaqueTag = rule { QuotedString }
  
  
  /* 3.12 Range Units */
  
  def RangeUnit = rule { BytesUnit | OtherRangeUnit }
  
  def BytesUnit = rule { "bytes" ~ push(RangeUnits.bytes) }
  
  def OtherRangeUnit = rule { Token ~~> RangeUnits.CustomRangeUnit }
}