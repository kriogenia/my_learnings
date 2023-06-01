package infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Timers}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object Timer extends App {

  private case object Start
  private case object Reminder
  private case object Stop

  private case object TimerKey
  private class TimedActor extends Actor with ActorLogging with Timers {

    timers.startSingleTimer(TimerKey, Start, 500 millis)

    override def receive: Receive = {
      case Start =>
        log.info("starting")
        timers.startTimerAtFixedRate(TimerKey, Reminder, 1 second)
      case Reminder =>
        log.info("i'm alive")
      case Stop =>
        log.warning("stopping")
        timers.cancel(TimerKey)
    }
  }

  import system.dispatcher
  private val system = ActorSystem("timer_demo")
  private val timedActor = system.actorOf(Props[TimedActor], "timed_actor")

  system.scheduler.scheduleOnce(5 seconds) {
    timedActor ! Stop
  }

}
