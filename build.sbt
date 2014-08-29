organization := "com.github.xuwei-k"

scalaVersion := "2.11.2"

name := "unfiltered-websocket-akka-redis"

resolvers += "rediscala" at "http://dl.bintray.com/etaty/maven"

licenses += ("MIT" -> url("http://opensource.org/licenses/MIT"))

libraryDependencies ++= (
  ("net.databinder" %% "unfiltered-netty-websockets" % "0.8.1") ::
  ("com.typesafe.akka" %% "akka-actor" % "2.3.5") ::
  ("com.etaty.rediscala" %% "rediscala" % "1.3.1").exclude("com.typesafe.akka", "akka-actor_" + scalaBinaryVersion.value) ::
  Nil
)
