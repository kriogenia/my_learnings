package client

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Sink, Source}
import client.PaymentSystemExample.httpRequests
import common.HttpApp

import scala.util.{Failure, Success}

object ConnectionLevel extends HttpApp {
  import actorSystem.dispatcher

  private val connectionLevel= Http().outgoingConnection("www.google.com")

  private def oneOfRequest(request: HttpRequest) =
    Source.single(request).via(connectionLevel).runWith(Sink.head)

  oneOfRequest(HttpRequest()).onComplete {
    case Success(response) => println(response)
    case Failure(exception) => println(s"Failed: $exception")
  }

  // Payment system
  Source(httpRequests)
    .via(Http().outgoingConnection(host, defaultPort))
    .to(Sink.foreach[HttpResponse](println))
    .run

}
