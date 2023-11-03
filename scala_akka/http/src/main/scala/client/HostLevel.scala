package client

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.scaladsl.{Sink, Source}
import client.PaymentSystemExample.{httpRequests, route}
import common.HttpApp

import java.util.UUID
import scala.util.{Failure, Success}

object HostLevel extends HttpApp {

  private val poolFlow  = Http().cachedHostConnectionPool[Int]("www.google.com")

  Source(1 to 10)
    .map(i => (HttpRequest(), i))
    .via(poolFlow)
    .map {
      case (Success(res), value) =>
        res.discardEntityBytes()
        s"Request $value received response $res"
      case (Failure(ex), value) =>
        s"Request $value failed with exception $ex"
    }
    .runWith(Sink.foreach[String](println))

  // Payment system
  Source(httpRequests(route).map((_, UUID.randomUUID().toString)))
    .via(Http().cachedHostConnectionPool[String](host, defaultPort))
    .runForeach {
      case (Success(res@HttpResponse(StatusCodes.Forbidden, _, _, _)), orderId) =>
        println(s"Order $orderId was not allowed: $res")
      case (Success(res), orderId) =>
        println(s"Order $orderId was successful: $res")
      case (Failure(ex), orderId) =>
        println(s"Order $orderId failed: $ex")
    }

}
