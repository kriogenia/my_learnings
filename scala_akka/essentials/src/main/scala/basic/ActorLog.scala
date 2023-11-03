package basic
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

import akka.event.Logging


object ActorLog extends App {

  private val system = ActorSystem("logging_demo")

  // 1. Explicit logger
  private class Explicit extends Actor {
    private val logger = Logging(context.system, this)

    override def receive: Receive = {
      case message => logger.info(message.toString)
    }
  }

  private val explicit = system.actorOf(Props[Explicit], "explicit")
  explicit ! "Message to log"
  explicit ! 12


  // 2. Implicit logger from ActorLogging trait
  private class TraitDerived extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.warning(message.toString)
    }
  }

  private val traitDerived = system.actorOf(Props[TraitDerived], "trait_derived")
  traitDerived ! "Message to log"
  traitDerived ! 12

}
