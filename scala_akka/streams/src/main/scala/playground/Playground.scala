package playground

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{Materializer, SystemMaterializer}

object Playground extends App {
  private implicit val actorSystem: ActorSystem = ActorSystem("playground")
  private implicit val materializer: Materializer = SystemMaterializer(actorSystem).materializer

  Source.single("Hello Streams!").to(Sink.foreach(println)).run()
}
