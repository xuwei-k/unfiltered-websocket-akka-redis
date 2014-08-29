package chat

import redis.RedisClient
import unfiltered.netty.websockets.{Message => WebSocketMessage, _}
import scala.collection.mutable
import scalaz.concurrent.Actor

object SessionManager {

  final val channel = "default-chat-channel"

  sealed abstract class Message
  final case class FromRedis(data: String) extends Message
  final case class WebSocketEvent(message: SocketCallback) extends Message

  private implicit class WebSocketOps(private val self: WebSocket) extends AnyVal {
    def getId: Int = self.channel.##
  }

  def apply(config: RedisActor.Config, redisClient: RedisClient): Actor[Message] = {
    val sessions: mutable.Map[Int, Actor[Session.Message]] = mutable.HashMap.empty

    def createChild(socket: WebSocket): Unit = {
      sessions.update(socket.getId, Session(Session.Config(socket)))
    }

    def publish(from: WebSocket, message: String): Unit ={
      redisClient.publish(SessionManager.channel, from.getId + "|" + message)
    }

    new Actor({
      case FromRedis(data) =>
        val message = Session.Message(data)
        sessions.values.foreach(_ ! message)
      case WebSocketEvent(message) => message match {
        case Open(s) =>
          publish(s, "joined")
          createChild(s)
          s.send("sys|hola!")
        case WebSocketMessage(s, Text(msg)) =>
          publish(s, msg)
        case Close(s) =>
          publish(s, "left")
        case Error(s, e) =>
          e.printStackTrace()
      }
    }, _.printStackTrace())
  }

}

