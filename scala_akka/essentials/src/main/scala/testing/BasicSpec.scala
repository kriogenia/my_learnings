package testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random

class BasicSpec extends TestKit(ActorSystem("basic_spec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll
{

  import BasicSpec._

  override def afterAll(): Unit = {
    super.afterAll()
    TestKit.shutdownActorSystem(system)
  }

  "An echo actor" should {
    "send back the same message" in {
      val actor = system.actorOf(Props[EchoActor])
      val msg = "hello test"
      actor ! msg

      expectMsg(msg)
    }
  }

  "A sink actor" should {
    "not answer back" in {
      val actor = system.actorOf(Props[SinkActor])
      val msg = "hello test"
      actor ! msg

      expectNoMessage(1 second)
    }
  }

  "A lab test actor" should {
    val actor = system.actorOf(Props[LabTestActor])

    "turn a string into uppercase" in {
      actor ! "lowercase"

      val reply = expectMsgType[String]
      assert(reply == "LOWERCASE")
    }

    "reply back to greetings" in {
      actor ! "greetings"

      expectMsgAnyOf("hi", "hello")
    }

    "return its tags when requested" in {
      actor ! "tags"

      expectMsgAllOf("Lab", "Test")
    }

    "reply back as much messages as the number requested" in {
      val n = 3
      actor ! n

      receiveN(3)
    }

  }

}

object BasicSpec {
  private class EchoActor extends Actor {
    override def receive: Receive = {
      case message => sender() ! message
    }
  }

  private class SinkActor extends Actor {
    override def receive: Receive = Actor.emptyBehavior
  }

  private class LabTestActor extends Actor {
    private val random = new Random()

    override def receive: Receive = {
      case "greetings" => sender() ! (if (random.nextBoolean()) "hi" else "hello")
      case "tags" =>
        sender() ! "Lab"
        sender() ! "Test"
      case i: Int => (0 to i).foreach(i => sender() ! i)
      case message: String => sender() ! message.toUpperCase
    }
  }
}