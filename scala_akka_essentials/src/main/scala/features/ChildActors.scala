package features

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActors extends App {

  // Actors can create other actors

  private object Parent {
    case class CreateChild(name: String)
    case class TellChild(msg: String)
  }
  private class Parent extends Actor {
    import Parent._

    override def receive: Receive = childless()

    private def childless(): Receive = {
      case CreateChild(name) =>
        println(s"${self.path} creating child")
        context.become(withChild(context.system.actorOf(Props[Child], name)))
      case TellChild(_) =>
    }

    private def withChild(child: ActorRef): Receive = {
      case CreateChild(_) => println(s"${self.path} already have child")
      case TellChild(msg) => child ! msg
    }
  }

  private class Child extends Actor {
    override def receive: Receive = {
      case msg => println(s"${self.path} I got: $msg")
    }
  }

  private val system = ActorSystem("family_system")
  private val parent = system.actorOf(Props[Parent], "parent")

  parent ! Parent.CreateChild("child")
  parent ! Parent.TellChild("Hello boy")

  // Top-level actors have Guardian actors
  // - /system = system guardian -> like the logger
  // - /user = user-level guardian -> created by the programmer, see "parent" above
  // - / = the root guardian -> parent of /system and /user

  // Actors can be located with its path
  private val childSelection = system.actorSelection("/user/parent/child")
  childSelection ! "I've found you"

}
