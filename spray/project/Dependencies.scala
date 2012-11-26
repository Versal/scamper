import sbt._

object Dependencies {

  val resolutionRepos = Seq(
    "sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases/",
    "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    "spray repo"         at "http://repo.spray.io/"
  )

  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def runtime   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")
  def container (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")

  val akkaActor     = "com.typesafe.akka"                       %   "akka-actor_2.10.0-RC2"       % "2.1.0-RC2"
  val akkaSlf4j     = "com.typesafe.akka"                       %   "akka-slf4j_2.10.0-RC2"       % "2.1.0-RC2"
  val akkaTestKit   = "com.typesafe.akka"                       %   "akka-testkit_2.10.0-RC2"     % "2.1.0-RC2"
  val clHashMap     = "com.googlecode.concurrentlinkedhashmap"  %   "concurrentlinkedhashmap-lru" % "1.3.1"
  val jettyWebApp   = "org.eclipse.jetty"                       %   "jetty-webapp"                % "8.1.7.v20120910"
  val servlet30     = "org.eclipse.jetty.orbit"                 %   "javax.servlet"               % "3.0.0.v201112011016" artifacts Artifact("javax.servlet", "jar", "jar")
  val logback       = "ch.qos.logback"                          %   "logback-classic"             % "1.0.7"
  val mimepull      = "org.jvnet.mimepull"                      %   "mimepull"                    % "1.8"
  val pegdown       = "org.pegdown"                             %   "pegdown"                     % "1.2.0"
  val parboiled     = "org.parboiled"                           %%  "parboiled-scala"             % "1.1.3"             cross CrossVersion.full
  val shapeless     = "com.chuusai"                             %%  "shapeless"                   % "1.2.3-SNAPSHOT"    cross CrossVersion.full
  val scalatest     = "org.scalatest"                           %%  "scalatest"                   % "1.8-B2"            cross CrossVersion.full
  val specs2        = "org.specs2"                              %%  "specs2"                      % "1.12.2"            cross CrossVersion.full
  val sprayJson     = "io.spray"                                %%  "spray-json"                  % "1.2.2"             cross CrossVersion.full
  val twirlApi      = "io.spray"                                %%  "twirl-api"                   % "0.6.1"             cross CrossVersion.full
}
