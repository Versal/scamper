name := "scamper-play2-mini"

version := "0.1-SNAPSHOT"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe" %% "play-mini" % "2.0.3"

mainClass in (Compile, run) := Some("play.core.server.NettyServer")
