organization := "com.github.xuwei-k"

scalaVersion := "2.11.4"

name := "unfiltered-websocket-akka-redis"

resolvers += "rediscala" at "http://dl.bintray.com/etaty/maven"

licenses += ("MIT" -> url("http://opensource.org/licenses/MIT"))

libraryDependencies ++= (
  ("net.databinder" %% "unfiltered-netty-websockets" % "0.8.3") ::
  ("com.typesafe.akka" %% "akka-actor" % "2.3.7") ::
  ("com.etaty.rediscala" %% "rediscala" % "1.4.2").exclude("com.typesafe.akka", "akka-actor_" + scalaBinaryVersion.value) ::
  ("org.scalaz" %% "scalaz-concurrent" % "7.1.0") ::
  Nil
)
