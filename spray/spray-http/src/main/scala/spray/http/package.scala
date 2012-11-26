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

import http.HttpHeaders.RawHeader


package object http {

  type QueryParams = Map[String, String]

  /**
   * Warms up the spray.http module by triggering the loading of most classes in this package,
   * so as to increase the speed of the first usage.
   */
  def warmUp() {
    HttpRequest(
      headers = List(
        RawHeader("Accept", "*/*,text/plain,custom/custom"),
        RawHeader("Accept-Charset", "*,UTF-8"),
        RawHeader("Accept-Encoding", "gzip,custom"),
        RawHeader("Accept-Language", "*,de-de,custom"),
        RawHeader("Authorization", "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ=="),
        RawHeader("Cache-Control", "no-cache"),
        RawHeader("Connection", "close"),
        RawHeader("Content-Disposition", "form-data"),
        RawHeader("Content-Encoding", "deflate"),
        RawHeader("Content-Length", "42"),
        RawHeader("Content-Type", "application/json"),
        RawHeader("Cookie", "spray=cool"),
        RawHeader("Host", "spray.io"),
        RawHeader("X-Forwarded-For", "1.2.3.4"),
        RawHeader("Fancy-Custom-Header", "yeah")
      ),
      entity = "spray rocks!"
    ).parseAll
    HttpResponse(status = 200)
  }
}