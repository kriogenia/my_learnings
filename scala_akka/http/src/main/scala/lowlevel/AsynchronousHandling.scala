package lowlevel

import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Sink
import common.HttpApp

import scala.concurrent.Future

object AsynchronousHandling extends HttpApp {

  import actorSystem.dispatcher // recommended to use a different dispatcher

  private val requestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/async"), _, _, _) => Future(HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(
          ContentTypes.`application/json`,
          """
            |{
            | "hello": "world",
            | "sender": "Asynchronous Akka HTTP"
            |}
            |""".stripMargin
        )
      ))
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

  // Alternative
  Http().newServerAt(host, altPort)
    .connectionSource()
    .runWith(Sink.foreach[IncomingConnection](_.handleWithAsyncHandler(requestHandler)))

}
