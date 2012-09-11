import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "scamper-play2"
  val appVersion = "0.1"

  val main = PlayProject(appName, appVersion, Seq.empty, mainLang = SCALA)

}
