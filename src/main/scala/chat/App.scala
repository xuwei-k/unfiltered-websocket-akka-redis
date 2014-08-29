package chat

import akka.actor.{ActorRef, ActorSystem}
import redis.RedisClient
import redis.api.pubsub.SUBSCRIBE
import unfiltered.netty.websockets.SocketCallback
import unfiltered.request.{Seg, Path, GET}
import unfiltered.response.Html5

object App {
  import unfiltered.util._

  def main(args: Array[String]) {
    val system = ActorSystem("websocket-chat")
    val host = "localhost"
    val port = 6379
    val config = RedisActor.Config(host, port, None)
    val publisher = RedisClient(config.host, config.port, config.password)(system)
    val manager = SessionManager(config, publisher)
    val subscriber = system.actorOf(RedisActor(config, manager), "subscriber")
    subscriber ! SUBSCRIBE(SessionManager.channel)

    try{
      unfiltered.netty.Server.anylocal.handler(
        unfiltered.netty.websockets.Planify({
          case _ => {
            case message: SocketCallback =>
              manager ! SessionManager.WebSocketEvent(message)
          }
        })
      ).run { websocketServer =>

        val websocketPort = websocketServer.ports.head
        val htmlResponse = Html5(View(websocketPort))

        unfiltered.netty.Server.anylocal.handler(
          unfiltered.netty.cycle.Planify{
            case GET(Path(Seg(Nil))) =>
              htmlResponse
          }
        ).run{ htmlServer =>
          (1 to 2).foreach { i =>
            Browser.open("http://localhost:" + htmlServer.ports.head + "/")
          }
        }

      }
    }finally{
      println("actor system shutdown")
      system.shutdown()
    }
  }
}
