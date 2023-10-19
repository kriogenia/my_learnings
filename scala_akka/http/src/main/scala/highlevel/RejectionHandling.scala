package highlevel

import akka.http.javadsl.server.{MethodRejection, MissingQueryParamRejection}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Rejection, RejectionHandler}
import common.HttpApp

object RejectionHandling extends HttpApp {

  private val simpleRoute = path("api") {
    get {
      complete(StatusCodes.OK)
    } ~
    parameter(Symbol("id") ) { _ =>
      complete(StatusCodes.OK)
    }
  }

  private val badRequestHandler: RejectionHandler = { rejections: Seq[Rejection] =>
    println(s"Encountered rejections: $rejections")
    Some(complete(StatusCodes.BadRequest))
  }

  private val forbiddenHandler: RejectionHandler = { rejections: Seq[Rejection] =>
    println(s"Encountered rejections: $rejections")
    Some(complete(StatusCodes.Forbidden))
  }

  private val customRejectionHandler = RejectionHandler.newBuilder()
    .handle { case r: MissingQueryParamRejection =>
      println(s"Encountered rejections: $r")
      complete("Add all query parameters")
    }
    .handle { case r: MethodRejection =>
      println(s"Encountered rejections: $r")
      complete("Rejected method")
    }
    .result()

  private val simpleRouteWithHandlers = handleRejections(badRequestHandler) {
    simpleRoute ~
    path("custom") {
      handleRejections(customRejectionHandler) {
        head {
          complete("Head is accepted")
        }
      }
    } ~
    path("forbidden") {
      handleRejections(forbiddenHandler) {
        parameter(Symbol("admin")) { _ =>
          complete(StatusCodes.Accepted)
        }
      }
     }
  }

  Http().newServerAt(host, defaultPort).bind(simpleRouteWithHandlers)

}
