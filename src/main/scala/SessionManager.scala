import akka.actor._
import redis.RedisClient
import redis.api.pubsub.{UNSUBSCRIBE, SUBSCRIBE}
import unfiltered.netty.websockets._

object SessionManager {

  private final val channel = "default-chat-channel"

}

final class SessionManager(config: RedisActor.Config, redisClient: RedisClient) extends Actor{

  private[this] val subscriber: ActorRef = {
    val actor = context.actorOf(RedisActor(config), "subscriber")
    actor ! SUBSCRIBE(SessionManager.channel)
    context.watch(actor)
    actor
  }

  private def createChild(socket: WebSocket): ActorRef = {
    val id = socket.channel.##.toString
    val child = context.actorOf(Props(new Session(Session.Config(socket))), id)
    context.watch(child)
    child
  }

  private def publish(from: WebSocket, message: String): Unit ={
    redisClient.publish(SessionManager.channel, from.channel.## + "|" + message)
  }

  override def receive = {
    case redis.api.pubsub.Message(channel, data) =>
      val message = Session.Message(data)
      context.children.foreach(_ ! message)
    case Open(s) =>
      publish(s, "joined")
      createChild(s)
      s.send("sys|hola!")
    case Message(s, Text(msg)) =>
      publish(s, msg)
    case Close(s) =>
      val id = s.channel.##.toString
      context.child(id).foreach{
        _ ! PoisonPill
      }
      publish(s, "left")
    case Error(s, e) =>
      e.printStackTrace()
    case Terminated(actor) =>
      println("terminated " + actor)
  }

  override def postStop(): Unit ={
    subscriber ! UNSUBSCRIBE(SessionManager.channel)
    subscriber ! PoisonPill
  }
}
