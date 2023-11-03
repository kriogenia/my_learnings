package files

import akka.Done
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, Multipart}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{FileIO, Sink, Source}
import common.HttpApp

import java.io.File
import scala.concurrent.Future
import scala.util.{Failure, Success}

object FileUpload extends HttpApp  {

  import actorSystem.dispatcher
  private val filesRoute = {
    (pathEndOrSingleSlash & get) {
      complete(
        HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   <form action="http://localhost:8080/upload" method="post" enctype="multipart/form-data">
            |     <input type="file" name="myFile"/>
            |     <button type="submit">Upload</button>
            |   </form>
            | </body>
            |</html>
            |""".stripMargin
        )
      )
    } ~
    (path("upload") & extractLog) { log =>
      entity(as[Multipart.FormData]) { formData =>
        val partsSource: Source[Multipart.FormData.BodyPart, Any] = formData.parts
        val filePartsSink: Sink[Multipart.FormData.BodyPart, Future[Done]] = Sink.foreach[Multipart.FormData.BodyPart] {
          bodyPart =>
            if (bodyPart.name == "myFile") {
              val filename = "src/main/resources/download/" + bodyPart.filename.getOrElse(
                s"temp_${System.currentTimeMillis()}")
              val file = new File(filename)
              log.info(s"Writing to $filename")

              val contentsSource = bodyPart.entity.dataBytes
              val contentsSink = FileIO.toPath(file.toPath)

              contentsSource.runWith(contentsSink)
            }
        }

        val operationFuture = partsSource.runWith(filePartsSink)
        onComplete(operationFuture) {
          case Success(_) => complete("File uploaded")
          case Failure(exception) => complete(s"Faile failed to upload: $exception")
        }
    }
  }
  }

  Http().newServerAt(host, defaultPort).bind(filesRoute)
}
