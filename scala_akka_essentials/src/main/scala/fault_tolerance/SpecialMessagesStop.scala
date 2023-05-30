package fault_tolerance

import akka.actor.{ActorSystem, Kill, PoisonPill, Props}
import common.SimpleLoggingActor

object SpecialMessagesStop extends App {

  private val system = ActorSystem("special_messages_stop")

  private val loose = system.actorOf(Props[SimpleLoggingActor], "loose")
  loose ! "You'll be kindly killed"
  loose ! PoisonPill

  private val terminated = system.actorOf(Props[SimpleLoggingActor], "terminated")
  terminated ! "You'll be abruptly terminated"
  terminated ! Kill

  Thread.sleep(500)
  loose ! "Check status"
  terminated ! "Check status"

}
