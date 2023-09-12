package highlevel

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import common.HttpApp

object Routing extends HttpApp {

  import akka.http.scaladsl.server.Directives._

  // basic directives
  private val simpleRoute: Route = path("home") {    // path
    get {                                                 // method
      complete(StatusCodes.OK)                            // handler
    }
  }

  // chaining directives with routing trees
  private val chainedRouter: Route =
    path("chain") {
      get {
        complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "GET /chain"))
      } ~ // <-- Important: if it's missing, only the last element will be used
      post {
        complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "POST /chain"))
      }
    } ~
    path("other") {
      get {
        complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "GET /other"))
      }
    } ~
    simpleRoute

  Http().newServerAt(host, defaultPort).bind(chainedRouter)

}
