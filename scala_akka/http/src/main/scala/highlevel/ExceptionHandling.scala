package highlevel

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import common.HttpApp

object ExceptionHandling extends HttpApp {

  private val simpleRoute = path("api") {
    get {
      throw new RuntimeException("runtime exception")
    } ~
    post {
      parameter(Symbol("id")) { id =>
        if (id.length > 1) throw new IllegalArgumentException("wrong id") else complete(StatusCodes.OK)
      }
    }
  }

  private val customExceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: RuntimeException => complete(StatusCodes.InternalServerError, e.getMessage)
    case e: IllegalArgumentException => complete(StatusCodes.BadRequest, e.getMessage)
  }

  Http().newServerAt(host, defaultPort).bind(handleExceptions(customExceptionHandler) {
    simpleRoute
  })

}
