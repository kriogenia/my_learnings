package testing

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class TestProbeSpec extends TestKit(ActorSystem("test_probe"))
  with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll
{

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import TestProbeSpec._

  "A master actor" should {
    "register a slave" in {
      val _ = registerMockSlave()
    }

    "send the work to the slave actor" in {
      val (master, slave) = registerMockSlave()

      master ! Master.Work("test")
      slave.expectMsg(Master.SlaveWork("test", testActor))

      slave.reply(Master.WorkCompleted(3, testActor))
      expectMsg(Master.Report(3))
    }

    "aggregate data correctly" in {
      val (master, slave) = registerMockSlave()

      master ! Master.Work("first")
      master ! Master.Work("second")

      slave.receiveWhile() {
        case Master.SlaveWork("first", `testActor`) => slave.reply(Master.WorkCompleted(3, testActor))
        case Master.SlaveWork("second", `testActor`) => slave.reply(Master.WorkCompleted(2, testActor))
      }

      expectMsg(Master.Report(3))
      expectMsg(Master.Report(5))
    }

    def registerMockSlave(): (ActorRef, TestProbe) = {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("register")

      master ! Master.Register(slave.ref)
      expectMsg(Master.RegistrationAck)

      (master, slave)
    }

  }

}

object TestProbeSpec {

  private object Master {
    case class Register(slaveRef: ActorRef)
    case class Work(test: String)

    case object RegistrationAck
    case class Report(count: Int)

    case class WorkCompleted(count: Int, originalRequester: ActorRef)

    case class SlaveWork(text: String, originalRequester: ActorRef)
  }
  private class Master extends Actor {
    import Master._

    override def receive: Receive = {
      case Register(slaveRef) =>
        context.become(online(slaveRef, 0))
        sender() ! RegistrationAck
      case _ => // ignore
    }

    private def online(slaveRef: ActorRef, total: Int): Receive = {
      case Work(text) => slaveRef ! SlaveWork(text, sender())
      case WorkCompleted(count, originalRequester) =>
        val totalCount = count + total
        originalRequester ! Report(totalCount)
        context.become(online(slaveRef, totalCount))
    }
  }

}
