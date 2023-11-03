package examples

import akka.stream.scaladsl.Source
import common.StreamApp

object NameFilter extends StreamApp {

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
