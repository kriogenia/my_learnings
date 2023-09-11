package lowlevel

import akka.actor.{Actor, ActorLogging}
import common.HttpApp
import spray.json._

case class Guitar(make: String, model: String)

private object GuitarDB {
  private case class CreateGuitar(guitar: Guitar)
  private case class GuitarCreated(id: Int)
  private case class FindGuitar(id: Int)
  private case object FindAllGuitars
}

class GuitarDB extends Actor with ActorLogging {

  import GuitarDB._

  override def receive: Receive = onMessage(Map(), 0)

  private def onMessage(guitars: Map[Int, Guitar], currentGuitarId: Int): Receive = {
    case FindAllGuitars =>
      log.info("Searching for all guitars...")
      sender() ! guitars.values.toList
    case FindGuitar(id) =>
      log.info(s"Searching guitar by id: $id...")
      sender() ! guitars.get(id)
    case CreateGuitar(guitar) =>
      log.info(s"Adding guitar $guitar with id $currentGuitarId")
      context.become(onMessage(guitars + (currentGuitarId -> guitar), currentGuitarId + 1))
      sender() ! GuitarCreated(currentGuitarId)
  }
}

/*
  GET   localhost:8080/api/guitar => ALL guitars in store
  POST  localhost:8080/api/guitar => INSERT guitar in store
 */

// 1. Implement a JSON protocol trait
trait GuitarStoreJsonProtocol extends DefaultJsonProtocol {
  // 2. Define implicit JSON format, possible to do with non-case class, but way harder
  implicit val guitarFormat: RootJsonFormat[Guitar] = jsonFormat2(Guitar)
}

// 3. Implement trait in the scope
object LowLevelRest extends HttpApp with GuitarStoreJsonProtocol {

  private val simpleGuitar = Guitar("Fendor", "Stratocaster")
  println(simpleGuitar.toJson.prettyPrint)

  private val simpleGuitarJsonString =
    """
      |{
      |  "make": "Fendor",
      |  "model": "Stratocaster"
      |}
      |""".stripMargin
  println(simpleGuitarJsonString.parseJson.convertTo[Guitar])

}
