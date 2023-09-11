package common

import spray.json._

case class Guitar(make: String, model: String)
case class GuitarList(guitars: List[Guitar])
case class GuitarId(id: Int)

// 1. Implement a JSON protocol trait
trait GuitarStoreJsonProtocol extends DefaultJsonProtocol {
  // 2. Define implicit JSON format, possible to do with non-case class, but way harder
  implicit val guitarFormat: RootJsonFormat[Guitar] = jsonFormat2(Guitar)
  implicit val guitarListFormat: RootJsonFormat[GuitarList] = jsonFormat1(GuitarList)
  implicit val guitarId: RootJsonFormat[GuitarId] = jsonFormat1(GuitarId)
}

// 3. Implement trait in the scope
object JsonSerDe extends App with GuitarStoreJsonProtocol {

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
