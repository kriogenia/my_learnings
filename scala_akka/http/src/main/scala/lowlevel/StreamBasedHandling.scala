package lowlevel

import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.stream.scaladsl.{Flow, Sink}
import common.HttpApp

object StreamBasedHandling extends HttpApp {

  private val requestHandler: Flow[HttpRequest, HttpResponse, _] = Flow[HttpRequest].map {
    case HttpRequest(HttpMethods.GET, Uri.Path("/stream"), _, _, _) => HttpResponse(
      StatusCodes.OK,
      entity = HttpEntity(
        ContentTypes.`text/plain(UTF-8)`,
        "Hello world! This is stream based Akka HTTP"
      )
    )
    case ignored: HttpRequest =>
      ignored.discardEntityBytes()
      HttpResponse(
        StatusCodes.NotFound,
        entity = HttpEntity(
          ContentTypes.`text/plain(UTF-8)`,
          "Nothing to see here"
        )
      )
  }

  Http().newServerAt(host, defaultPort).bindFlow(requestHandler)

  // Alternative
  Http().newServerAt(host, altPort)
    .connectionSource()
    .runWith(Sink.foreach[IncomingConnection](_.handleWith(requestHandler)))

}
