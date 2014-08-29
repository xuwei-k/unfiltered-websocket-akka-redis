package chat

import akka.actor.Props
import java.net.InetSocketAddress
import redis.actors.RedisSubscriberActor
import redis.api.pubsub.{PMessage, Message}
import scalaz.concurrent.Actor

object RedisActor{

  def apply(config: Config, callback: Actor[SessionManager.Message]): Props =
    Props(new RedisActor(config, callback))

  final case class Config(
    host: String,
    port: Int,
    password: Option[String]
  )

}


final class RedisActor(config: RedisActor.Config, callback: Actor[SessionManager.Message]) extends RedisSubscriberActor(
  address = new InetSocketAddress(config.host, config.port),
  channels = Nil,
  patterns = Nil,
  authPassword = config.password
){

  override def onMessage(m: Message): Unit ={
    callback ! SessionManager.FromRedis(m.data)
  }

  override def onPMessage(m: PMessage): Unit ={
  }

}
