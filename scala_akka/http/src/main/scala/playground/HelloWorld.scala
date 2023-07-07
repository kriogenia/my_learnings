package playground

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, pathEndOrSingleSlash}
import akka.stream.{Materializer, SystemMaterializer}

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object HelloWorld extends App {

  private implicit val actorSystem: ActorSystem = ActorSystem("playground")

  import actorSystem.dispatcher

  private val simpleRoute =
    pathEndOrSingleSlash {
      complete(HttpEntity(
        ContentTypes.`text/html(UTF-8)`,
        """
          |<html>
          | <body>
          |   <p>Hello World!</p>
          | </body>
          |</html>
        """.stripMargin
      ))
    }

  private val bindingFuture = Http().newServerAt("localhost", 8080).bindFlow(simpleRoute)

  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => actorSystem.terminate())

}
