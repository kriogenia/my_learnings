package highlevel

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import common.HttpApp

object ActionableDirectives extends HttpApp {

  private val completeRoute = complete(StatusCodes.OK)

  private val failRoute = failWith(new RuntimeException("500 Internal Server Error"))

  private val joinRoute = {
    path("ok") { completeRoute } ~
    path("error") { failRoute }
    path("reject") { reject }
  }

  private val router = joinRoute

  Http().newServerAt(host, defaultPort).bind(router)
}
