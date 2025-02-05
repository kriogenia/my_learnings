package common

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import akka.stream.{Materializer, SystemMaterializer}

import scala.concurrent.ExecutionContextExecutor

/**
 * Trait to initialize and provide an actor system and materializer required for all stream demos.
 */
trait StreamApp extends App {

  val actorSystem = ActorSystem("stream-app")
  implicit val materializer: Materializer = SystemMaterializer(actorSystem).materializer
  implicit val executionContext: ExecutionContextExecutor = materializer.executionContext

  def printSink[T](tag: String) = Sink.foreach[T](x => println(s"$tag\t$x"))

}
