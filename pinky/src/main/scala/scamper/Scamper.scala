package scamper

import org.pinky.guice.{CakeServletModule, PinkyServletContextListener}

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

