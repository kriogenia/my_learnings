package lifecycle

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props, Terminated}
import common.SimpleLoggingActor

object DeathWatch extends App {

  private val system = ActorSystem("death_watch")

  private object Watcher {
    case class Spawn(name: String)
  }
  private class Watcher extends Actor with ActorLogging {
    import Watcher._

    override def receive: Receive = {
      case Spawn(name) =>
        val child = context.actorOf(Props[SimpleLoggingActor], name)
        context.watch(child)
        log.info(s"Spawned and watching child $child")
      case Terminated(ref) =>
        log.info(s"The reference $ref has been stopped")
    }
  }

  private val watcher = system.actorOf(Props[Watcher], "watcher")
  watcher ! Watcher.Spawn("spawned")

  Thread.sleep(500)

  system.actorSelection("/user/watcher/spawned") ! PoisonPill
}
