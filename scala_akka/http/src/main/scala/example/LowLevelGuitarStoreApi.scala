package example

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.pattern.ask
import common._
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object LowLevelGuitarStoreApi extends HttpApp with GuitarStoreJsonProtocol {

  private val guitarDb = GuitarDB.init(actorSystem)

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
