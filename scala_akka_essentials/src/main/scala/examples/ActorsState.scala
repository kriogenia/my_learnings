package examples

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorsState extends App {

  private object Session {
    case class Accept(data: String)
    case class Reject(reason: String)

    private val STARTED = "started"
    private val CLOSED = "closed"
  }
  private class Session extends Actor {
    import Session._
    import Client._

    private val state = CLOSED

    override def receive: Receive = onMessage(state)

    private def onMessage(state: String): Receive = {
      case SignOut =>
        context.become(onMessage(CLOSED))
        sender() ! Accept("session closed")
      case SignIn =>
        context.become(onMessage(STARTED))
        sender() ! Accept("session started")
      case Request(request) =>
        sender() ! (if (state == CLOSED) Reject("not authorized") else Accept(s"data of $request"))
    }
  }

  private class StatelessSession extends Actor {
    import Session._
    import Client._

    override def receive: Receive = unauthorizedRequest

    private def authorizedRequest: Receive = {
      case SignOut =>
        context.become(unauthorizedRequest)
        sender() ! Accept("session closed")
      case SignIn =>
        sender() ! Reject("session is already started")
      case Request(request) =>
        sender() ! Accept(s"data of $request")

    }
    private def unauthorizedRequest: Receive = {
      case SignOut =>
        sender() ! Reject("session is already closed")
      case SignIn =>
        context.become(authorizedRequest)
        sender() ! Accept("session started")
      case Request(_) =>
        sender() ! Reject("not authorized")
    }
  }

  private object Client {
    case class Start(ref: ActorRef)

    case object SignIn
    case object SignOut
    case class Request(request: String)
  }
  private class Client extends Actor {
    import Client._

    override def receive: Receive = {
      case Start(kid) =>
        kid ! Request("name")
        kid ! SignIn
        kid ! Request("name")
        kid ! SignOut
      case Session.Reject(reason) => println(s"[ERROR] $reason")
      case Session.Accept(msg) => println(s"[SUCCESS] $msg")
    }
  }

  private val system = ActorSystem("behavior_system")
  private val internalState = system.actorOf(Props[Session], "internal")
  private val statePattern = system.actorOf(Props[Session], "state")
  private val client = system.actorOf(Props[Client], "client")

  client ! Client.Start(internalState)

  client ! Client.Start(statePattern)

}
