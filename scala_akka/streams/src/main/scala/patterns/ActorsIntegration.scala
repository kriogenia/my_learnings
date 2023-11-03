package patterns

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.stream.{CompletionStrategy, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout
import common.StreamApp

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Try

object ActorsIntegration extends StreamApp {

  private class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case s: String =>
        log.info(s"STRING: $s")
        sender() ! Try(s.toInt).getOrElse(0)
      case n: Int =>
        log.info(s"NUMBER: $n")
        sender() ! n.toString * 2
      case _ =>
    }
  }

  private val simpleActor = actorSystem.actorOf(Props[SimpleActor], "simpleActor")
  private val numbersSource = Source(1 to 10)

  /*
    Actor as a flow
   */
  implicit val timeout: Timeout = Timeout(2 seconds)
  private val actorBasedFlow = Flow[Int].ask[String](parallelism = 4)(simpleActor)
  numbersSource.via(actorBasedFlow).to(printSink("F")).run
  // numbersSource.ask[String](parallelism = 4)(simpleActor).to(printSink("~")).run  // equivalent alternative

  /*
    Actor as source
   */
  private val actorPoweredSource: Source[String, ActorRef] = Source.actorRef(
    completionMatcher = {
      case Done =>
        println(s"[DONE] at actorPoweredSource")
        CompletionStrategy.immediately
    },
    failureMatcher = PartialFunction.empty,
    bufferSize = 10,
    overflowStrategy = OverflowStrategy.dropHead
  )
  private val materializedActorRef = actorPoweredSource.to(printSink[String]("S")).run
  for (x <- 1 to 10) materializedActorRef ! x
  materializedActorRef ! Done   // this will notify the source that no more elements will be sent
  materializedActorRef ! 11     // this will be ignored as the source is finished

  /*
    Actor as sink, needs:
    - an init message
    - an ack message to confirm the reception
    - a complete message
    - a function to generate a message in case the stream throws an exception
   */
  private object SinkActor {
    case object Init
    case object Ack
    case object Complete
    case class Fail(ex: Throwable)
  }
  private class SinkActor extends Actor with ActorLogging {
    import SinkActor._
    override def receive: Receive = {
      case Init =>
        log.info("Stream initialized")
        sender() ! Ack
      case Complete =>
        log.info("Stream complete")
        context.stop(self)
      case Fail(ex) =>
        log.warning("Stream failed", ex)
      case message =>
        log.info(s"Consumed $message")
        sender() ! Ack
    }
  }

  private val sinkActor = actorSystem.actorOf(Props[SinkActor], "sinkActor")
  private val actorPoweredSink = Sink.actorRefWithBackpressure[Int](
    ref = sinkActor,
    onInitMessage = SinkActor.Init,
    ackMessage = SinkActor.Ack,
    onCompleteMessage = SinkActor.Complete,
    onFailureMessage = throwable => SinkActor.Fail(throwable)
  )

  Source(1000 to 1010).to(actorPoweredSink).run

}
