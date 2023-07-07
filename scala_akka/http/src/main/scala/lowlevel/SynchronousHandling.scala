package lowlevel

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Sink

import scala.language.postfixOps

object SynchronousHandling extends App {

  private implicit val actorSystem: ActorSystem = ActorSystem("low-level")

  /*
    Serving HTTP responses synchronously
   */
  private val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(HttpMethods.GET, _, _, _, _) =>
      HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   Hello world! This is Akka HTTP
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

  Http().newServerAt("localhost", 8080).bindSync(requestHandler)

  // Alternative
  Http().newServerAt("localhost", 8081)
    .connectionSource()
    .runWith(Sink.foreach[IncomingConnection] (_.handleWithSyncHandler(requestHandler)))

}
