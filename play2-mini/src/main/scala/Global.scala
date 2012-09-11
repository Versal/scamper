import play.api.Configuration
import com.typesafe.config.ConfigFactory

object Global extends com.typesafe.play.mini.Setup(scamper.App) {
  override def configuration: Configuration = Configuration(ConfigFactory.load("application.conf"))
}
