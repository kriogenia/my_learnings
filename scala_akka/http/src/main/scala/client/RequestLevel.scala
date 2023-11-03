package client

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.scaladsl.Source
import client.PaymentSystemExample.{httpRequests, route}
import common.HttpApp

import scala.util.{Failure, Success}

object RequestLevel extends HttpApp {
  import actorSystem.dispatcher

  Http().singleRequest(HttpRequest(uri = "https://www.google.com")).onComplete {
    case Success(res) =>
      res.discardEntityBytes()
      println(s"The request was successful: $res")
    case Failure(ex) => println(s"The request failed: $ex ")
  }

  // Payment system
  Source(httpRequests(s"http://$host:$defaultPort$route"))
    .mapAsync(5)(Http().singleRequest(_)) // can be mapAsyncUnordered too
    .runForeach(println)

}
