package examples

import akka.NotUsed
import akka.stream.ClosedShape
import akka.stream.scaladsl.{Broadcast, GraphDSL, RunnableGraph, Sink, Source}
import common.StreamApp

object EventBroadcast extends StreamApp {

  private sealed trait Event
  private case class Request(method: String, url: String) extends Event
  private case class Exception(msg: String) extends Event
  private case class HttpError(code: Int, msg: String) extends Event


  private val source = Source(List(
    Request("GET", "/"),
    Request("POST", "/user"),
    Request("GET", "/user/123"),
    HttpError(404, "User not found"),
    HttpError(500, "FooBarAgent has crashed"),
    Exception("NullPointerException at line 3"),
    HttpError(503, "FooBarAgent"),
  ))

  private val logger = Sink.foreach[Event] {
    case Request(method, url) => println(s"[INFO] $method $url")
    case Exception(msg) => println(s"[ERROR] $msg")
    case HttpError(code, msg) => println(s"[WARN] $code $msg")
  }

  private val ticket = Sink.foreach[Event] {
    case Exception(msg) => println(s"tickets@support.com > [ERROR] $msg")
    case HttpError(code, msg) if code == 503 => println(s"tickets@support.com > [SERVICE_UNAVAILABLE] $msg")
    case _ =>
  }

  private val stats = Sink.foreach[Event] {
    case Request(method, _) => println(s"SUCCESS $method")
    case HttpError(code, _) => println(s"FAILED $code")
    case _ =>
  }

  /*
              +--> logger
    source ---+--> ticket
              +--> stats
   */

  private val graph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val broadcast = builder.add(Broadcast[Event](3))

      source ~> broadcast ~> logger
                broadcast ~> ticket
                broadcast ~> stats

      ClosedShape
    }
  )
  graph.run()

}
