package lifecycle

import akka.actor.{Actor, ActorRef, ActorSystem, AllForOneStrategy, OneForOneStrategy, Props, SupervisorStrategy, Terminated}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class SupervisionSpec extends TestKit(ActorSystem("supervision_spec"))
  with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import SupervisionSpec._

  "a supervisor" should {
    "resume its child in case of minor fault" in {
      val child = init()

      child ! "Basic sentence"
      child ! Report
      expectMsg(2)

      child ! "Sentence too long to be evaluated"
      child ! Report
      expectMsg(2)
    }

    "restart its child in case of empty strings" in {
      val child = init()

      child ! "Basic sentence"
      child ! Report
      expectMsg(2)

      child ! ""
      child ! Report
      expectMsg(0)
    }

    "terminate its child in case of major error" in {
      val child = init()
      watch(child)

      child ! "lowercase"
      val terminatedMsg = expectMsgType[Terminated]
      assert(terminatedMsg.actor == child)
    }

    "escalate an error when it doesn't know how to handle it" in {
      val child = init()
      watch(child)

      child ! 0
      val terminatedMsg = expectMsgType[Terminated]
      assert(terminatedMsg.actor == child)
    }

    def init(): ActorRef = {
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]

      expectMsgType[ActorRef]
    }
  }

  "a kinder supervisor" should {
    "not kill children in case it's restarted or escalates failures" in {
      val supervisor = system.actorOf(Props[NoDeathSupervisor], "no-death_supervisor")
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      child ! "Correct sentence"
      child ! Report
      expectMsg(2)

      child ! 0
      child ! Report
      expectMsg(0)
    }
  }

  "an all for one supervisor" should {
    "apply the strategy to all the children" in {
      val supervisor = system.actorOf(Props[AllForOneSupervisor], "all-for-one_supervisor")

      supervisor ! Props[FussyWordCounter]
      val firstChild = expectMsgType[ActorRef]

      supervisor ! Props[FussyWordCounter]
      val secondChild = expectMsgType[ActorRef]

      secondChild ! "Valid message"
      secondChild ! Report
      expectMsg(2)

      EventFilter[NullPointerException]() intercept {
        firstChild ! ""
      }

      Thread.sleep(500)
      secondChild ! Report
      expectMsg(0)
    }
  }

}
object SupervisionSpec {

  private class Supervisor extends Actor {
    import SupervisorStrategy._

    override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: RuntimeException => Resume
      case _: Exception => Escalate
    }

    override def receive: Receive = {
      case props: Props =>
        val childRef = context.actorOf(props)
        sender() ! childRef
    }
  }

  private class NoDeathSupervisor extends Supervisor {
    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {}
  }

  private class AllForOneSupervisor extends Supervisor {
    import SupervisorStrategy._

    override val supervisorStrategy: SupervisorStrategy = AllForOneStrategy() {
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: RuntimeException => Resume
      case _: Exception => Escalate
    }
  }

  private case object Report
  private class FussyWordCounter extends Actor {
    override def receive: Receive = onMessage(0)

    private def onMessage(count: Int): Receive = {
      case Report => sender() ! count
      case "" => throw new NullPointerException("sentence is empty")
      case sentence: String if sentence.length > 20 => throw new RuntimeException("sentence too big")
      case sentence: String if !Character.isUpperCase(sentence(0)) =>
        throw new IllegalArgumentException("sentence must start with uppercase")
      case sentence: String => context.become(onMessage(count + sentence.split(" ").length))
      case _ => throw new Exception("type not supported")
    }
  }
}
