package common

import akka.actor.ActorSystem

trait HttpApp extends App {
  implicit val actorSystem: ActorSystem = ActorSystem("http-app")
}
