package scamper

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class BasicServlet extends HttpServlet {
  
  override def doGet(req: HttpServletRequest, res: HttpServletResponse) =
    req.getRequestURI() match {
      case "/simple" => res.getWriter().write("<h1>simple</h1>")
      case "/slow" =>
        val start = System.currentTimeMillis
        Thread.sleep(200)
        val stop = System.currentTimeMillis
        res.getWriter().write("<h1>slept for %d ms</h1>".format(stop - start))
    }
}
