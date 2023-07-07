package common

import akka.actor.ActorSystem

trait HttpApp extends App {
  implicit val actorSystem: ActorSystem = ActorSystem("http-app")

  val host: String = "localhost"
  val defaultPort: Int = 8080
  val altPort: Int = 8081

}
