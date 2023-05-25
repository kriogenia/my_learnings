package examples

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object DistributedWordCountActor extends App {

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

  private object  WordCounterWorker {
    case class Task(id: Int, text: String)
  }
  private class WordCounterWorker extends Actor {
    import WordCounterWorker._
    override def receive: Receive = {
      case Task(id, text) => sender() ! WordCounterMaster.WordCountReply(id, text.split(" ").length)
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

}
