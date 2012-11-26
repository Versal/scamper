resolvers ++= Seq(
  // for scala-ssh
  "spray repo" at "http://repo.spray.io",
  // sbt-twirl transitively depends on akka-actor right now...
  "typesafe releases" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "com.github.siasia" %% "xsbt-web-plugin" % "0.12.0-0.2.11.1",
  "com.decodified" % "scala-ssh" % "0.6.2",
  "org.bouncycastle" % "bcprov-jdk16" % "1.46",
  "com.jcraft" % "jzlib" % "1.1.1"
  // "ch.qos.logback" % "logback-classic" % "1.0.7"
)

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.2")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.6.2")

addSbtPlugin("io.spray" % "sbt-twirl" % "0.6.1")

addSbtPlugin("io.spray" % "sbt-boilerplate" % "0.5.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.8.5")