package testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random

class TimedAssertionsSpec extends TestKit(ActorSystem("timed_assertions"))
  with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll
{

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import TimedAssertionsSpec._

  "A worker actor" should {
    val worker = system.actorOf(Props[WorkerActor])

    "reply with the meaning of life in a timely manner" in {
      within(500 millis, 1 second) {
        worker ! "work"
        expectMsg(WorkResult(42))
      }
    }

    "reply with valid work at a reasonable cadence" in {
      within(1 second) {
        worker ! "work_sequence"

        val results: Seq[Int] = receiveWhile[Int](max = 2 second, idle = 500 millis, messages = 10) {
          case WorkResult(result) => result
        }

        assert(results.sum == 10)
      }
    }

  }

}

object TimedAssertionsSpec {

  private case class WorkResult(result: Int)
  private class WorkerActor extends Actor {
    override def receive: Receive = {
      case "work" =>
        Thread.sleep(500)
        sender() ! WorkResult(42)
      case "work_sequence" =>
        val r = new Random
        for (_ <- 1 to 10) {
          Thread.sleep(r.nextInt(50))
          sender() ! WorkResult(1)
        }
    }
  }

}