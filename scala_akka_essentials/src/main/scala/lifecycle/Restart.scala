package lifecycle

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}

object Restart extends App {

  private case object FailChild
  private case object CheckChild
  private class Parent extends Actor {
    private val child = context.actorOf(Props[Child], "supervisedChild")

    override def receive: Receive = {
      case FailChild => child ! Fail
      case CheckChild => child ! Check
    }
  }

  private case object Fail
  private case object Check
  private class Child extends Actor with ActorLogging {

    override def preStart(): Unit = log.info("Supervised child started")
    override def postStop(): Unit = log.info("Supervised child stopped")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit =
      log.info(s"Supervised child restarting, reason; ${reason.getMessage}")

    override def postRestart(reason: Throwable): Unit = log.info("Supervised child restarted")

    override def receive: Receive = {
      case Fail =>
        log.warning("child will fail now")
        throw new RuntimeException("I failed")
      case Check => log.info("I'm doing ok")
    }
  }

  val system = ActorSystem("restart_demo")
  val parent = system.actorOf(Props[Parent])

  parent ! FailChild
  parent ! CheckChild

}
