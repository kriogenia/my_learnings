package basic

import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import common.StreamApp

import scala.util.{Failure, Success}

object MaterializingStreams extends StreamApp {

  private val simpleGraph = Source(1 to 10).to(Sink.foreach(i => println(s"simpleGraph > $i")))
  private val simpleMaterializedValue = simpleGraph.run()
  println(s"A consumer sink returns a $simpleMaterializedValue")

  private val source = Source(1 to 10)
  private val sink = Sink.reduce[Int]((a, b) => a + b)
  private val sumFuture = source.runWith(sink)
  sumFuture.onComplete {
    case Success(value) => println(s"sumFuture > The sum of all elements is: $value")
    case Failure(exception) => println(s"sumFuture > The sum of the elements could not be computed: $exception")
  }

  // Choosing materialized values
  private val simpleSource = Source(1 to 10)
  private val simpleFlow = Flow[Int].map(_ + 1)
  private val simpleSink = printSink[Int]("matGraph >")
  private val graph = simpleSource.viaMat(simpleFlow)(Keep.left).toMat(simpleSink)(Keep.right)
  graph.run().onComplete {
    case Success(_) => println("matGraph >\tStream processing finished")
    case Failure(exception) => println(s"matGraph >\tStream processing failed with $exception")
  }

  Source(1 to 10).runWith(Sink.reduce[Int](_ + _))  // source.toMat(Sink.reduce)(Keep.right)
  Source(1 to 10).runReduce(_ + _)                  // source.toMat(Sink.reduce)(Keep.right)

  printSink[Int]("backwards >").runWith(Source.single(42))   // backwards

  Flow[Int].map(_ * 2).runWith(simpleSource, printSink[Int](s"bothWays >"))   // both ways

  // Return the last element of a source
  Source(1 to 10).runWith(Sink.last).onComplete {
    case Failure(exception) => println(s"lastElement > Failed to return the last element: $exception")
    case Success(value) => println(s"lastElement > $value")
  }

}
