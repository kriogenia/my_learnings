package fault_tolerance

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import common.SimpleLoggingActor

object ContextStop extends App {

  private val system = ActorSystem("context_stop")

  object Parent {
    case class StartChild(name: String)
    case class StopChild(name: String)
    case object Stop
  }
  class Parent extends Actor with ActorLogging {
    import Parent._

    override def receive: Receive = withChildren(Map())

    private def withChildren(children: Map[String, ActorRef]): Receive = {
      case StartChild(name) =>
        log.info(s"Starting child $name")
        context.become(withChildren(children + (name -> context.actorOf(Props[SimpleLoggingActor], name))))
      case StopChild(name) =>
        log.info(s"Trying to stop child $name")
        val child = children.get(name)
        child.foreach(childRef => context.stop(childRef))
      case Stop =>
        log.info("Stopping myself")
        context.stop(self)
      case message => log.info(message.toString)
    }
  }

  import Parent._
  val parent = system.actorOf(Props[Parent], "parent")
  parent ! StartChild("child-1")

  private val child1 = system.actorSelection("/user/parent/child-1")
  child1 ! "Hi"

  parent ! StopChild("child-1")

  parent ! StartChild("child-2")
  parent ! Stop // also stops child

  Thread.sleep(500)

  parent ! "Check status"
  child1 ! "Check status"
  system.actorSelection("/user/parent/child-2") ! "Check status"
}
