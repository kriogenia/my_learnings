package example

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import common._
import spray.json._

object HighLevelGuitarStoreApi extends HttpApp with GuitarStoreJsonProtocol {

  /*
    GET   localhost:8080/api/guitar => RETURN all guitars in store
    GET   localhost:8080/api/guitar?id={id} => Return the guitar with the given id
    GET   localhost:8080/api/guitar/{id} => Return the guitar with the given id
   */

  import actorSystem.dispatcher
  private val guitarDb = GuitarDB.init(actorSystem)

  private def toHttpEntity(content: String) = HttpEntity(ContentTypes.`application/json`, content)

  private val guitarServerRoute =
    (pathPrefix("api" / "guitar") & get) {
      (parameter(Symbol("id").as[Int]) | path(IntNumber)) { id =>
        complete((guitarDb ? GuitarDB.FindGuitar(id))
          .mapTo[Option[Guitar]]
          .map(_.toJson.prettyPrint)
          .map(toHttpEntity))
      } ~
      pathEndOrSingleSlash {
        complete((guitarDb ? GuitarDB.FindAllGuitars)
          .mapTo[List[Guitar]]
          .map(GuitarList)
          .map(_.toJson.prettyPrint)
          .map(toHttpEntity))
      }
    }

  Http().newServerAt(host, defaultPort).bind(guitarServerRoute)

}
