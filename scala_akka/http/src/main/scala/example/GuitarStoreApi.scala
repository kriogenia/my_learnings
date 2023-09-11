package example

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.pattern.ask
import common.{Guitar, GuitarId, GuitarList, GuitarStoreJsonProtocol, HttpApp}
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object GuitarStoreApi extends HttpApp with GuitarStoreJsonProtocol {

  private object GuitarDB {
    case class CreateGuitar(guitar: Guitar)
    case class GuitarCreated(id: Int)
    case class FindGuitar(id: Int)
    case object FindAllGuitars
  }

  private class GuitarDB extends Actor with ActorLogging {
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

  private val guitarDb = actorSystem.actorOf(Props[GuitarDB], "guitar_db")
  List(
    Guitar("Fendor", "Stratocaster"),
    Guitar("Gibson", "Les Paul"),
    Guitar("Martin", "LX1")
  ).foreach(guitar => guitarDb ! GuitarDB.CreateGuitar(guitar))

  /*
    GET   localhost:8080/api/guitar => ALL guitars in store
    POST  localhost:8080/api/guitar => INSERT guitar in store
   */
  import actorSystem.dispatcher
  private val requestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/api/guitar"), _, _, _) =>
      (guitarDb ? GuitarDB.FindAllGuitars)
        .mapTo[List[Guitar]]
        .map(guitars => GuitarList(guitars))
        .map { guitars =>
          HttpResponse(
            StatusCodes.OK,
            entity = HttpEntity(ContentTypes.`application/json`, guitars.toJson.prettyPrint)
          )
        }
    case HttpRequest(HttpMethods.POST, Uri.Path("/api/guitar"), _, entity, _) =>
      entity.toStrict(3 second)
        .map(entity => entity.data.utf8String)
        .map(b => { println(b); b })
        .map(json => json.parseJson.convertTo[Guitar])
        .flatMap(guitar => guitarDb ? GuitarDB.CreateGuitar(guitar))
        .mapTo[GuitarDB.GuitarCreated]
        .map(msg => GuitarId(msg.id))
        .map { msg =>
          HttpResponse(
            StatusCodes.Created,
            entity = HttpEntity(ContentTypes.`application/json`, msg.toJson.prettyPrint)
          )
        }
    case ignored: HttpRequest =>
      ignored.discardEntityBytes()
      Future(HttpResponse(
        StatusCodes.NotFound,
        entity = HttpEntity(
          ContentTypes.`application/json`,
          "{}"
        )
      ))
  }

  Http().newServerAt(host, defaultPort).bind(requestHandler)

}
