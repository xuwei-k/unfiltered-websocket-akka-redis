package chat

import unfiltered.netty.websockets.WebSocket

object Session {
  final case class Config(socket: WebSocket)
  final case class Message(value: String)

  def apply(config: Config): scalaz.concurrent.Actor[Message] = {
    import config._
    new scalaz.concurrent.Actor({
      case Message(value) =>
        if (socket.channel.isActive) {
          socket.send(value)
        }
    }, _.printStackTrace())
  }
}
