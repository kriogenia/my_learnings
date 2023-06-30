package infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

object Dispatchers extends App {

  private class Counter extends Actor with ActorLogging {

    override def receive: Receive = onMessage(0)

    private def onMessage(current: Int): Receive = {
      case message =>
        val count = current + 1
        log.info(s"[$count] $message")
        context.become(onMessage(count))
    }

  }

  private val system = ActorSystem("dispatchers_demo", ConfigFactory.load().getConfig("dispatchers"))

  private val counters = for (i <- 1 to 10) yield
    system.actorOf(Props[Counter].withDispatcher("my-dispatcher"), s"counter-$i")
  private val _ = system.actorOf(Props[Counter], "conf_counter")

  private val r = new Random()
  for (i <- 1 to 100) {
    counters(r.nextInt(10)) ! i
  }

  // dispatchers implement the ExecutionContext trait
  private class DBActor extends Actor with ActorLogging {
    // move to a different dedicated dispatcher to reduce blocks
    implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("my-dispatcher")
    override def receive: Receive = {
      case message => Future {
        Thread.sleep(1000) // wait for resource
        log.info(s"Success: $message")
      }
    }
  }

  private val dbActor = system.actorOf(Props[DBActor])
  dbActor ! "test"

  private val nonBlockingActor = system.actorOf(Props[Counter])
  for (i <- 1 to 1000) {
    val msg = s"key message: $i"
    dbActor ! msg
    nonBlockingActor ! msg
  }

}
