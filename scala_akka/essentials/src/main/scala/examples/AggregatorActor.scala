package examples

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object AggregatorActor extends App {

  private object LogLine {
    case class Response(status: Int)
    case object StatusRequest

    case class StatusReply(status: Option[Int])
  }
  private class LogLine extends Actor {
    import LogLine._

    override def receive: Receive = empty()

    private def empty(): Receive = {
      case Response(status) => context.become(populated(status))
      case StatusRequest => sender() ! StatusReply(None)
    }

    private def populated(status: Int): Receive = {
      case Response(_) =>
      case StatusRequest => sender() ! StatusReply(Option(status))
    }
  }


  private object LogAnalyzer {
    private case object Start
    private case object Finish

    private type Stats = Map[Int, Int]
  }
  private class LogAnalyzer extends Actor {
    import LogAnalyzer._

    override def receive: Receive = empty()

    private def empty(): Receive = {
      case logs: Set[ActorRef] =>
        context.become(analyzing(logs, Map()))
        self ! Start
    }

    private def analyzing(logs: Set[ActorRef], stats: Stats): Receive = {
      case Start if logs.isEmpty => println(stats)
      case Start => logs.foreach(log => log ! LogLine.StatusRequest)

      case LogLine.StatusReply(None) => update(logs, stats)
      case LogLine.StatusReply(Some(status)) =>
        val computed = stats + stats.get(status).map(v => status -> (v + 1)).getOrElse(status -> 1)
        update(logs, computed)
    }

    private def finished(stats: Stats): Receive = {
      case Finish => println(stats)
    }

    private def update(logs: Set[ActorRef], stats: Stats): Unit = {
      val remaining = logs - sender()
      if (remaining.isEmpty) {
        context.become(finished(stats))
        self ! Finish
      } else {
        context.become(analyzing(remaining, stats))
      }
    }

  }


  private val system = ActorSystem("log_system")
  private val logs = (0 to 5).map(i => system.actorOf(Props[LogLine], s"log_line:$i")).toList

  logs.head ! LogLine.Response(200)
  logs(1) ! LogLine.Response(404)
  logs(2) ! LogLine.Response(200)
  logs(3) ! LogLine.Response(500)

  private val logAnalyzer = system.actorOf(Props[LogAnalyzer], "analyzer")

  logAnalyzer ! logs.toSet

}
