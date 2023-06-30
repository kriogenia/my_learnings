package examples

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.{DurationDouble, DurationInt}
import scala.language.postfixOps

object ExpiringActor extends App {

  private class SelfClosable extends Actor with ActorLogging {
    implicit private val dispatcher: ExecutionContextExecutor = context.system.dispatcher

    override def receive: Receive = {
      case message =>
        log.info(message.toString)
        scheduleExpiration()
    }

    private def expiring(expirationTask: Cancellable): Receive = {
      case message =>
        expirationTask.cancel()
        log.info(message.toString)
        scheduleExpiration()
    }

    private def scheduleExpiration(): Unit = {
      val expirationTask = context.system.scheduler.scheduleOnce(1 second) {
        log.info("reached expiration time, closing actor")
        context.stop(self)
      }
      context.become(expiring(expirationTask))
    }

  }

  private val system = ActorSystem("expiring_actor_demo")
  private val expiringActor = system.actorOf(Props[SelfClosable], "expiring_actor")

  import system.dispatcher
  private val routine: Cancellable = system.scheduler.scheduleWithFixedDelay(
    1 second,
    0.5 seconds,
    expiringActor,
    "keep alive")

  system.scheduler.scheduleOnce(5 second) {
    system.log.info("stopping requester")
    routine.cancel()
  }

}
