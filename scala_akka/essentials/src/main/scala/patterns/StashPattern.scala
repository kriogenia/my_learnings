package patterns

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}

object StashPattern extends App {

  private object ResourceActor {
    case object Open
    case object Close
    case object Read
    case class Write(data: String)
  }
  // 1. Use Stash trait
  private class ResourceActor extends Actor with ActorLogging with Stash {
    import ResourceActor._

    override def receive: Receive = closed("")

    private def closed(content: String): Receive = {
      case Open =>
        log.info("OPEN resource")
        unstashAll()  // 3. Unstash messages before moving to the state able to handle them
        context.become(open(content))
      case message =>
        log.info(s"STASH $message. Can't be handed yet.")
        stash() // 2. Stash message that you can't handle yet
    }

    private def open(content: String): Receive = {
      case Read => log.info(s"READ resource -> $content")
      case Write(data) =>
        log.info(s"WRITE $data -> resource")
        context.become(open(content + data))
      case Close =>
        log.info("CLOSE resource")
        unstashAll()
        context.become(closed(content))
      case message =>
        log.info(s"STASH $message. Can't be handed yet.")
        stash()
    }
  }

  private val system = ActorSystem("stash_demo")
  private val actor = system.actorOf(Props[ResourceActor], "resource_actor")

  import ResourceActor._
  actor ! Read
  actor ! Open
  actor ! Write("1")
  actor ! Write(", 2")
  actor ! Read
  actor ! Open
  actor ! Read
  actor ! Close
  actor ! Write(" and 3")
  actor ! Read

}
