package highlevel

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import common.HttpApp

object FilteringDirectives extends HttpApp {

  private val simpleHttpMethodRoute = post {
    complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "POST"))
  }

  private val simplePathRoute = path("path") {
    complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "/path"))
  }

  private val complexPathRoute = path("complex" / "path") { // /complex/path
    complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "/complex/path"))
  }

  private val thisWouldBeUrlEncoded = path("url/encoded") {
    complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "/url%2Fpath"))
  }

  private val pathEndRoute = pathEndOrSingleSlash {
    complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "/"))
  }

  private val router =
    simpleHttpMethodRoute ~ simplePathRoute ~ complexPathRoute ~ thisWouldBeUrlEncoded ~ pathEndRoute

  Http().newServerAt(host, defaultPort).bind(router)

}
