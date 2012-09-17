name := "scamper-blueeyes"

scalaVersion := "2.9.1"

resolvers ++= Seq(
  "Sonatype" at "http://oss.sonatype.org/content/repositories/public",
  "Typesafe" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "com.github.jdegoes" %% "blueeyes-core"  % "0.6.0",
  "com.github.jdegoes" %% "blueeyes-mongo" % "0.6.0",
  "com.github.jdegoes" %% "blueeyes-json"  % "0.6.0",
  "ch.qos.logback" % "logback-classic" % "1.0.0" % "runtime"
)

seq(webSettings :_*)

classpathTypes ~= (_ + "orbit")

libraryDependencies ++= Seq(
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "compile,container" artifacts (Artifact("javax.servlet", "jar", "jar"))
  , "org.eclipse.jetty" % "jetty-webapp" % "8.1.4.v20120524" % "compile,container" artifacts (Artifact("jetty-webapp", "jar", "jar"))
)
