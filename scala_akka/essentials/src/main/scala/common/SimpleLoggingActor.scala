package common

import akka.actor.{Actor, ActorLogging}

case class SimpleLoggingActor() extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
}
