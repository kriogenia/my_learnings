package testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{CallingThreadDispatcher, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.Duration

class SynchronousSpec extends AnyWordSpecLike with BeforeAndAfterAll {

  private implicit val system: ActorSystem = ActorSystem("synchronous")

  override def afterAll(): Unit = system.terminate()

  import SynchronousSpec._
  "a counter" should {
    "work on the calling thread dispatcher" in {
      val counter = system.actorOf(Props[Counter].withDispatcher(CallingThreadDispatcher.Id))
      val probe = TestProbe()

      probe.send(counter, Read)
      probe.expectMsg(Duration.Zero, 0)
    }
  }

}

object SynchronousSpec {

  private case object Inc
  private case object Read

  private class Counter extends Actor {
    private val count = 0
    override def receive: Receive = onMessage(count)

    private def onMessage(count: Int): Receive = {
      case Inc => context.become(onMessage(count + 1))
      case Read => sender() ! count
    }
  }

}
