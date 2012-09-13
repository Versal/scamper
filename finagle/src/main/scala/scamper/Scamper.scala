package scamper

import java.net.InetSocketAddress
import java.net.SocketAddress

import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.handler.codec.http.HttpResponseStatus.OK
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1
import org.jboss.netty.handler.codec.http.DefaultHttpResponse
import org.jboss.netty.handler.codec.http.HttpRequest
import org.jboss.netty.handler.codec.http.HttpResponse
import org.jboss.netty.util.CharsetUtil.UTF_8

import com.twitter.finagle.builder.Server
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http.Http
import com.twitter.finagle.Service
import com.twitter.util.Future

object Scamper extends App {

  val service: Service[HttpRequest, HttpResponse] = new Service[HttpRequest, HttpResponse] {

    def apply(request: HttpRequest) = request.getUri() match {
      case "/fast" => sleepilyRespond(request, 0)
      case "/medium" => sleepilyRespond(request, 150)
      case "/slow" => sleepilyRespond(request, 300)
    }
  }

  def sleepilyRespond(request: HttpRequest, delay: Long): Future[HttpResponse] = {
    val response = new DefaultHttpResponse(HTTP_1_1, OK)
    response.setContent(copiedBuffer("<h1>slept for %d ms</h1>".format(sleep(delay)), UTF_8))
    Future(response)
  }

  def sleep(ms: Long): Long = {
    val start = System.currentTimeMillis
    Thread.sleep(ms)
    val stop = System.currentTimeMillis
    stop - start
  }

  val address: SocketAddress = new InetSocketAddress(10000)

  val server: Server = ServerBuilder()
    .codec(Http.get())
    .bindTo(address)
    .name("scamper")
    .build(service)

}