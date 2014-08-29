import akka.actor.{Props, ActorSystem}
import redis.RedisClient

object App {
  import unfiltered.util._
  import unfiltered.response.ResponseString

  def main(args: Array[String]) {
    val system = ActorSystem("websocket-chat")
    val host = "localhost"
    val port = 6379
    val config = RedisActor.Config(host, port, None)
    val publisher = RedisClient(config.host, config.port, config.password)(system)
    val manager = system.actorOf(Props(new SessionManager(config, publisher)), "session-manager")

    try{
      unfiltered.netty.Http(5679).handler(unfiltered.netty.websockets.Planify({
        case _ => {
          case message =>
            manager ! message
        }
      }))
      .handler(unfiltered.netty.cycle.Planify{
        case _ => ResponseString("not a websocket")
      })
      .run { s =>
        (1 to 4).foreach { i =>
          Browser.open("file://%s".format(App.getClass.getResource("client.html").getFile))
        }
      }
    }finally{
      println("actor system shutdown")
      system.shutdown()
    }
  }
}
