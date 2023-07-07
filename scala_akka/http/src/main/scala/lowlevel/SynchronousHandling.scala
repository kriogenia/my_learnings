package lowlevel

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Sink
import common.HttpApp

import scala.language.postfixOps

object SynchronousHandling extends HttpApp {

  private val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/sync"), _, _, _) =>
      HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   Hello world! This is synchronous Akka HTTP
            | </body>
            |</html>
            |""".stripMargin
        )
      )
    case ignored: HttpRequest =>
      ignored.discardEntityBytes()
      HttpResponse(
        StatusCodes.NotFound,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   Nothing to see here
            | </body>
            |</html>
            |""".stripMargin
        )
      )
  }

  Http().newServerAt(host, defaultPort).bindSync(requestHandler)

  // Alternative
  Http().newServerAt(host, altPort)
    .connectionSource()
    .runWith(Sink.foreach[IncomingConnection] (_.handleWithSyncHandler(requestHandler)))

}
