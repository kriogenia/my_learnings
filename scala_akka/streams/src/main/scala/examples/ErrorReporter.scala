package examples

import akka.stream.scaladsl.GraphDSL.Implicits.port2flow
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Sink, Source}
import akka.stream.{FanOutShape2, SinkShape}
import common.StreamApp

import scala.language.postfixOps

object ErrorReporter extends StreamApp {

  /*
    Validator that receives one response and forwards it, sending also an error message if the response has failed
   */
  private case class Response(code: Int, body: String)

  private val responses = Seq(
    Response(200, "\"User\"{\"id\":123}"),
    Response(404, "No user with id 124"),
    Response(201, "\"User\"{\"id\":124}"),
    Response(500, "Service has crashed"),
    Response(403, "You can't access this resource")
  )

  private val connection = Sink.foreach(println)
  private val errorReporter = Sink.foreach[Int](code => println(s"[WARN] Failed request: $code"))

  /*
              +---------------------------------+
              |                                 |
              |   +---------------------------------> Response
              |   |                             |
  Response -------|                             |
              |   |                             |
              |   +--> filter --> codeMapper -------> Int
              |                                 |
              +---------------------------------+
   */

  private val errorRaiserStaticGraph = GraphDSL.create() { implicit builder =>
    val broadcast = builder.add(Broadcast[Response](2))
    val codeMapper = builder.add(Flow[Response].map(_.code))

    broadcast.out(0) ~> Flow[Response].filter(_.code >= 400) ~> codeMapper

    new FanOutShape2(broadcast.in, broadcast.out(1), codeMapper.out)
  }

  /*
            +-------------------------+
            |                         |
            |   +---> connection      |
            |   |                     |
  Response -----|                     |
            |   |                     |
            |   +---> errorReporter   |
            |                         |
            +-------------------------+
   */

  private val responseEvaluatorSink = Sink.fromGraph(
    GraphDSL.create() { implicit builder =>
      val errorRaiser = builder.add(errorRaiserStaticGraph)

      errorRaiser.out0 ~> connection
      errorRaiser.out1 ~> errorReporter

      SinkShape(errorRaiser.in)
    }
  )
  responseEvaluatorSink.runWith(Source(responses))

}