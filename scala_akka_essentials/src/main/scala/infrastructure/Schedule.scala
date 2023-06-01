package infrastructure

import akka.actor.{ActorSystem, Cancellable, Props}
import common.SimpleLoggingActor

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object Schedule extends App {

  private val system = ActorSystem("timers_schedulers_demo")
  private val actor = system.actorOf(Props[SimpleLoggingActor], "actor")

  system.log.info("scheduling reminder for actor")

  //implicit val executionContext = system.dispatcher
  import system.dispatcher
  system.scheduler.scheduleOnce(1 second) {
    actor ! "reminder"
  }

  private val routine: Cancellable = system.scheduler.scheduleWithFixedDelay(1 second, 2 second, actor, "heartbeat")

  system.scheduler.scheduleOnce(5 second) {
    routine.cancel()
    system.log.info("heartbeat cancelled")
  }

}
