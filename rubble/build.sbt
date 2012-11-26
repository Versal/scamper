name := "rubble"

organization := "com.earldouglas"

version := "0.1-SNAPSHOT"

scalaVersion := "2.9.2"

classpathTypes ~= (_ + "orbit")

libraryDependencies += "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "provided" artifacts (Artifact("javax.servlet", "jar", "jar"))

libraryDependencies ++= Seq(
    "org.eclipse.jetty.orbit"   %  "javax.servlet" % "3.0.0.v201112011016" artifacts (Artifact("javax.servlet", "jar", "jar"))
  , "org.eclipse.jetty.orbit"   %  "javax.servlet" % "3.0.0.v201112011016" artifacts (Artifact("javax.servlet", "jar", "jar"))
  , "org.eclipse.jetty"         %  "jetty-webapp"  % "8.1.4.v20120524"     artifacts (Artifact("jetty-webapp", "jar", "jar"))
)
