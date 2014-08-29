import Session.{Message, Config}
import akka.actor._
import unfiltered.netty.websockets.WebSocket

object Session {
  final case class Config(socket: WebSocket)
  final case class Message(value: String)
}

final class Session(config: Config) extends Actor{
  import config._
  override def receive = {
    case Message(value) =>
      if(socket.channel.isActive){
        socket.send(value)
      }
  }
}
