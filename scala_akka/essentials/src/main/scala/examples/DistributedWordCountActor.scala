package examples

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.util.Success

object DistributedWordCountActor extends App {


  private object WordCounterWorker {
    case class Task(id: Int, text: String)
  }

  private class WordCounterWorker extends Actor {

    import WordCounterWorker._

    override def receive: Receive = {
      case Task(id, text) => sender() ! WordCounterMaster.WordCountReply(id, text.split(" ").length)
    }
  }

  private object WordCounterMaster {
    case class Initialize(numChildren: Int)
    case class WordCountReply(id: Int, count: Int)


    private case class State(index: Int, requests: Map[Int, ActorRef], nextId: Int)
  }
  private class WordCounterMaster extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case Initialize(numChildren) =>
        val childs = (0 to numChildren).map(
          n => context.system.actorOf(Props[WordCounterWorker], s"worker-$n")
        ).toList
        context.become(initialized(childs, State(0, Map(), 0)))
    }

    private def initialized(childs: List[ActorRef], state: State): Receive = {
      case text: String =>
        val computed = state.requests + (state.nextId -> sender())
        childs(state.index) ! WordCounterWorker.Task(state.nextId, text)
        context.become(initialized(childs, State((state.index + 1) % childs.length, computed, state.nextId + 1)))
      case WordCountReply(id, value) =>
        val sender = state.requests(id)
        val computed = state.requests - id
        sender ! value
        context.become(initialized(childs, State(state.index, computed, state.nextId)))
    }
  }

  private class WordCounterMasterAskPattern extends Actor {
    import WordCounterMaster._
    import WordCounterWorker._

    implicit val timeout: Timeout = Timeout(1 second)
    implicit val executionContext: ExecutionContext = context.dispatcher

    override def receive: Receive = {
      case Initialize(numChildren) =>
        val childs = (0 to numChildren).map(
          n => context.system.actorOf(Props[WordCounterWorker], s"ask_worker-$n")
        ).toList
        context.become(initialized(childs, 0))
    }

    private def initialized(childs: List[ActorRef], index: Int): Receive = {
      case text: String =>
        (childs(index) ? Task(0, text)).mapTo[WordCountReply].map(r => r.count).pipeTo(sender())
        context.become(initialized(childs, (index + 1) % childs.length))
    }
  }


  private class OrchestratorActor(val ref: ActorRef) extends Actor {
    override def receive: Receive = {
      case text: String => ref ! text
      case i: Int => println(s"${self.path}: $i words")
    }
  }

  private val system = ActorSystem("count_system")
  private val master = system.actorOf(Props[WordCounterMaster], "master")
  master ! WordCounterMaster.Initialize(3)

  private val o1 = system.actorOf(Props(new OrchestratorActor(master)), "o1")
  private val o2 = system.actorOf(Props(new OrchestratorActor(master)), "o2")
  private val o3 = system.actorOf(Props(new OrchestratorActor(master)), "o3")
  private val o4 = system.actorOf(Props(new OrchestratorActor(master)), "o4")

  o1 ! "a brown fox"
  o4 ! "what a weird fox"
  o3 ! "he went into mexico"
  o2 ! "jumped over the wall"

  private val askMaster = system.actorOf(Props[WordCounterMasterAskPattern], "ask")
  askMaster ! WordCounterMaster.Initialize(3)

  private val a1 = system.actorOf(Props(new OrchestratorActor(askMaster)), "a1")
  private val a2 = system.actorOf(Props(new OrchestratorActor(askMaster)), "a2")
  private val a3 = system.actorOf(Props(new OrchestratorActor(askMaster)), "a3")
  private val a4 = system.actorOf(Props(new OrchestratorActor(askMaster)), "a4")

  a1 ! "a brown fox"
  a4 ! "what a weird fox"
  a3 ! "he went into mexico"
  a2 ! "jumped over the wall"

}
