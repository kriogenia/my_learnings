package examples

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.{Materializer, SystemMaterializer}

object NameFilter extends App {

  private val actorSystem = ActorSystem("name-filter")
  private implicit val materializer: Materializer = SystemMaterializer(actorSystem).materializer

  private val names = List("Joey", "Chandler", "Monica", "Phoebe", "Rachel", "Ross")
  private val minLength = 5
  private val sizeLimit = 2

  filter(names)

  private def filter(names: List[String]) = {
    Source(names)
      .filter(_.length >= minLength)
      .take(sizeLimit)
      .runForeach(println)
  }

}
