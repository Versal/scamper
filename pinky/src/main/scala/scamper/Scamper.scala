package scamper

import org.pinky.guice.{CakeServletModule, PinkyServletContextListener}


import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * this class is referenced in the generated web.xml
 */
class Scamper extends PinkyServletContextListener {
  modules = Array(
    new CakeServletModule with ScamperServlet {
      override def configureServlets {
        val fast   = new FastServletImpl
        val medium = new MediumServletImpl
        val slow   = new SlowServletImpl
        bindServlet(fast) toUrl ("/fast")
        bindServlet(medium) toUrl ("/medium")
        bindServlet(slow) toUrl ("/slow")
      }
   }
  )
}

trait ScamperServlet {
  def sleep(ms: Long): Long = {
    val start = System.currentTimeMillis
    Thread.sleep(ms)
    val stop = System.currentTimeMillis
    stop - start
  }

  import org.pinky.core.PinkySimpleServlet
  import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

  class FastServletImpl extends PinkySimpleServlet {
    GET {
       (request: HttpServletRequest, response: HttpServletResponse) =>
       "text/html" -> "<h1>slept for %d ms</h1>".format(sleep(0))
    }
  }  

  class MediumServletImpl extends PinkySimpleServlet {
    GET {
       (request: HttpServletRequest, response: HttpServletResponse) =>
       "text/html" -> "<h1>slept for %d ms</h1>".format(sleep(150))
    }
  }  

  class SlowServletImpl extends PinkySimpleServlet {
    GET {
       (request: HttpServletRequest, response: HttpServletResponse) =>
       "text/html" -> "<h1>slept for %d ms</h1>".format(sleep(300))
    }
  }  
}


object JettyScamperServer {

  def main(args: Array[String]) {

    val server = new Server()

    val connector = new SelectChannelConnector()
    connector.setPort(9000)
    connector.setThreadPool(new QueuedThreadPool(24))

    server.addConnector(connector)

    val webapp = new WebAppContext()
    webapp.setContextPath("/")
    webapp.setWar("src/main/webapp")
    server.setHandler(webapp)

    server.start()
    server.join()
  }
}
