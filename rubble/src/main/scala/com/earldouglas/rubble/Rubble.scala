package com.earldouglas.rubble

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import java.io.InputStream
import scala.util.matching.Regex
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServlet
import scala.xml.NodeSeq

object Rubble {

  implicit def richRequest(req: HttpServletRequest) = new RichRequest(req)
  implicit def stringBody(body: String) = StringBody(body)
  implicit def streamBody(body: InputStream) = StreamBody(body)
  implicit def xmlBody(body: NodeSeq) = XmlBody(body)
  implicit def richServlet(servlet: HttpServlet) = new RichServlet(servlet)
  implicit def richResponse(res: HttpServletResponse) = new RichResponse(res)
  implicit def singleHeader(header: Header) = Seq(header)

  class RichRequest(req: HttpServletRequest) {
    val context = req.getContextPath + req.getServletPath
    def uri: String = req.getRequestURI() match {
      case x if x startsWith context =>
        x.substring(context.length())
      case x => x
    }
    def params: Map[String, String] = req.getParameterNames().asScala.map(name => (name -> req.getParameter(name))).toMap
  }

  class RichServlet(servlet: HttpServlet) {
    def initParam(name: String): Option[String] = Option(servlet.getInitParameter(name))
  }

  case class Header(name: String, value: String)

  sealed trait Body
  case object NoBody extends Body
  case class StringBody(body: String) extends Body
  case class StreamBody(body: InputStream) extends Body
  case class XmlBody(body: NodeSeq) extends Body

  class RichResponse(res: HttpServletResponse) {
    def respond(status: Int = 200, headers: Seq[Header] = Nil, body: Body = NoBody) {
      res.setStatus(status)
      headers.foreach(x => res.setHeader(x.name, x.value))
      body match {
        case StringBody(body) => res.getWriter().write(body)
        case StreamBody(body) =>
          val os = res.getOutputStream()
          Iterator.continually(body.read).takeWhile(-1 !=).foreach(os.write)
        case XmlBody(body) =>
          val w = res.getWriter()
          body.foreach(n => w.write(n.toString()))
        case NoBody =>
      }
    }

    def redirect(loc: String) {
      res.setStatus(303)
      res.setHeader("Location", loc)
    }
  }
}

