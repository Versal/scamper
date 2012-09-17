name := "scamper-scalatra"

organization := "com.versal"

version := "0.1-SNAPSHOT"

scalaVersion := "2.9.1"

seq(webSettings :_*)

classpathTypes ~= (_ + "orbit")

libraryDependencies ++= Seq(
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "compile,container" artifacts (Artifact("javax.servlet", "jar", "jar"))
  , "org.eclipse.jetty" % "jetty-webapp" % "8.1.4.v20120524" % "compile,container" artifacts (Artifact("jetty-webapp", "jar", "jar"))
  , "org.scalatra" %% "scalatra" % "2.0.4"
)

port in container.Configuration := 9000
