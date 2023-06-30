package lifecycle

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}

object PreStartAndPostStop extends App {

  private case object StartChild
  private class LifecycleActor extends Actor with ActorLogging {

    override def preStart(): Unit = log.info("I am starting")

    override def postStop(): Unit = log.info("I have stopped")

    override def receive: Receive = {
      case StartChild =>
        context.actorOf(Props[LifecycleActor], "child")
    }

  }

  val system = ActorSystem("lifecycle_demo")
  val parent = system.actorOf(Props[LifecycleActor], "parent")

  parent ! StartChild
  parent ! PoisonPill

}
