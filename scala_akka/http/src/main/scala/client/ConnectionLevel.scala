package client

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, Uri}
import akka.stream.scaladsl.{Sink, Source}
import client.PaymentSystemDomain.PaymentRequest
import common.HttpApp
import spray.json._

import scala.util.{Failure, Success}

object ConnectionLevel extends HttpApp with PaymentJsonProtocol {
  import actorSystem.dispatcher

  private val connectionLevel= Http().outgoingConnection("www.google.com")

  private def oneOfRequest(request: HttpRequest) =
    Source.single(request).via(connectionLevel).runWith(Sink.head)

  oneOfRequest(HttpRequest()).onComplete {
    case Success(response) => println(response)
    case Failure(exception) => println(s"Failed: $exception")
  }

  // Payment system
  private val httpRequests = List(
    CreditCard("1234-1234-1234-1234", "123", "Owner McOwner"),
    CreditCard("1234", "124", "Too short"),
    CreditCard("1234-1234-1234-1236", "125", "Credit Cardson"),
  )
    .map(cc => PaymentRequest(cc, "store", 99))
    .map(pr => HttpRequest(
      HttpMethods.POST,
      uri = Uri("/api/payments"),
      entity = HttpEntity(
        ContentTypes.`application/json`,
        pr.toJson.prettyPrint,
      ),
    ))

  Source(httpRequests)
    .via(Http().outgoingConnection(host, defaultPort))
    .to(Sink.foreach[HttpResponse](println))
    .run

}
