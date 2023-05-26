package features

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

object ActorLog extends App {

  // 1. Explicit logger
  private class Explicit extends Actor {
    private val logger = Logging(context.system, this)

    override def receive: Receive = {
      case message => logger.info(message.toString)
    }
  }

  // 2. Implicit logger from ActorLogging trait
  private class TraitDerived extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }


  private val system = ActorSystem("logging_system")
  private val explicit = system.actorOf(Props[Explicit], "explicit")
  private val traitDerived = system.actorOf(Props[TraitDerived], "trait_derived")

  explicit ! "Message to log"
  traitDerived ! "Message to log"
  explicit ! 12
  traitDerived ! 12

}
