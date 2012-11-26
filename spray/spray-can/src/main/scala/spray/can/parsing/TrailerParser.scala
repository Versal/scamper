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

package spray.can.parsing

import spray.http.ChunkExtension
import spray.http.HttpHeaders.RawHeader


class TrailerParser(settings: ParserSettings, extensions: List[ChunkExtension] = Nil, headerCount: Int = 0,
                    headers: List[RawHeader] = Nil) extends HeaderNameParser(settings, null, headerCount, headers) {

  override def valueParser = new HeaderValueParser(settings, null, headerCount, headers, headerName.toString) {
    override def nameParser =
      new TrailerParser(settings, extensions, headerCount + 1, RawHeader(headerName, headerValue.toString) :: headers)
  }

  override def headersComplete = ChunkedEndState(extensions, headers)
}