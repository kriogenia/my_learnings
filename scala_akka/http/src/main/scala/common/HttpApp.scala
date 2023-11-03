package common

import akka.actor.ActorSystem
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

trait HttpApp extends App {
  implicit val actorSystem: ActorSystem = ActorSystem("http-app")

  implicit val defaultTimeout: Timeout = Timeout(2 second)

  val host: String = "localhost"
  val defaultPort: Int = 8080
  val altPort: Int = 8081

}
