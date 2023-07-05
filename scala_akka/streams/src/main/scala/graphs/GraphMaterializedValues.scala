package graphs

import akka.stream.SinkShape
import akka.stream.scaladsl.GraphDSL.Implicits.fanOut2flow
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Sink, Source}
import common.StreamApp

import scala.util.{Failure, Success}

object GraphMaterializedValues extends StreamApp {

  /*
    Composite component acting like a sink
      - prints out all string which are lowercase
      - counts the string that are short (< minLength)
   */
  private val minLength = 4

  private val wordSource = Source("A curated list of words to use in this demo".split(" ").toSeq)
  private val printer = Sink.foreach[String](println)
  private val counter = Sink.fold[Int, String](0)((count, _) => count + 1)

  /*
              +-------------------------------------------------------+
              |                 +--> lowercaseFilter --> printer      |
    String ------> broadcast ---|                                     |
              |                 +--> shortStringFilter --> counter ------> Future[Int]
              +-------------------------------------------------------+
   */
  private val complexWordSink = Sink.fromGraph(
    GraphDSL.createGraph(counter) { implicit builder => counterShape =>
      val broadcast = builder.add(Broadcast[String](2))
      val lowercaseFilter = builder.add(Flow[String].filter(s => s == s.toLowerCase))
      val shortStringFilter = builder.add(Flow[String].filter(_.length < minLength))

      broadcast ~> lowercaseFilter ~> printer
      broadcast ~> shortStringFilter ~> counterShape

      SinkShape(broadcast.in)
    }
  )
  wordSource.runWith(complexWordSink).onComplete {
    case Failure(exception) => println(s"Unexpected failure running the complexWorSink: $exception")
    case Success(value) => println(s"The number of short words is $value")
  }

}
