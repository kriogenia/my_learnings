package highlevel

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import common.HttpApp

object ExtractionDirectives extends HttpApp {

  private val pathExtractionRoute = path("api" / "items" / IntNumber) { (id: Int) =>
    complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"/api/items/$id"))
  }

  private val pathMultiExtractionRoute = path("api" / IntNumber / "items" / IntNumber) { (user, item) =>
    complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"/api/$user/items/$item"))
  }

  private val queryParamExtractionRoute = path("api" / "items") {
    // symbols are held in memory and checked by reference instead of using string comparison, improving performance
    parameter(Symbol("id").as[Int]) { id =>
      complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"/api/items?id=$id"))
    }
  }

  private val extractRequestRoute = path("request") {
    extractRequest { request =>
      complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, request.uri.toString()))
    }
  }

  private val router = pathExtractionRoute ~ pathMultiExtractionRoute ~ queryParamExtractionRoute ~ extractRequestRoute

  Http().newServerAt(host, defaultPort).bind(router)

}
