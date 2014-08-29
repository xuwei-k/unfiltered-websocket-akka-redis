package chat

import akka.actor.Props
import java.net.InetSocketAddress
import redis.actors.RedisSubscriberActor
import redis.api.pubsub.{PMessage, Message}

object RedisActor{

  def apply(config: Config): Props =
    Props(new RedisActor(config))

  final case class Config(
    host: String,
    port: Int,
    password: Option[String]
  )

}


final class RedisActor(config: RedisActor.Config) extends RedisSubscriberActor(
  address = new InetSocketAddress(config.host, config.port),
  channels = Nil,
  patterns = Nil,
  authPassword = config.password
){

  override def onMessage(m: Message): Unit ={
    context.parent ! m
  }

  override def onPMessage(m: PMessage): Unit ={
  }

}
