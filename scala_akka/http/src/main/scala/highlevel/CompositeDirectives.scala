package highlevel

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import common.HttpApp

object CompositeDirectives extends HttpApp {

  private val compactSimpledNestedRoute = (path("api" / "compact") & get) {
    complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "/api/compact"))
  }

  private val compactExtractRequestRoute = (path("api" / "extract") & extractRequest & extractLog) { (req, log) =>
    log.info(s"Request: $req")
    complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "/api/extract"))
  }

  private val compositeRoute = (path("index") | path("index.html")) {
    complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><body><h1>Index</h1></body></html>"))
  }

  private val pathAndQueryParameterRoute = //path("api" / "item") {
    (path(IntNumber) | parameter(Symbol("id").as[Int])) { (id: Int) =>
      complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"/api/item/$id"))
    }
  //}

  private val router =
    compactSimpledNestedRoute ~ compactExtractRequestRoute ~ compositeRoute ~ pathAndQueryParameterRoute

  Http().newServerAt(host, defaultPort).bind(router)

}
