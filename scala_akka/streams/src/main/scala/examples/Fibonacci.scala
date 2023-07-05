package examples

import akka.stream.scaladsl.GraphDSL.Implicits.{SourceShapeArrow, fanOut2flow, port2flow}
import akka.stream.scaladsl.{Broadcast, Concat, Flow, GraphDSL, Source, Zip}
import akka.stream.{FlowShape, SourceShape}
import common.StreamApp

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object Fibonacci extends StreamApp {

  /*
          +-----------------------------------------------+
          |              start ----+              +-----+ |
          |                        |-> queue ---> 0    | |
          |                   +----+              |     | |
    Int ----> inputBroadcast -|                   |     -----> (Int, Int)
                              +------------------> 1    | |
          |                                       +-----+ |
          +-----------------------------------------------+
   */
  private val zipWithPrev = Flow.fromGraph(GraphDSL.create() { implicit builder =>
    val start = builder.add(Source.single(0))
    val queue = builder.add(Concat[Int](2))
    val inputBroadcast = builder.add(Broadcast[Int](2))
    val join = builder.add(Zip[Int, Int])

    start ~> queue.in(0)
    inputBroadcast ~> queue.in(1)
    inputBroadcast ~> join.in1
    queue.out ~> join.in0

    FlowShape(inputBroadcast.in, join.out)
  })

  zipWithPrev.runWith(Source(0 to 10), printSink("> "))

  /*
    +-------------------------------------------------------------+
    | one ----+                                                   |
    |         |--> queue --> zipWithPrev ---> add ---> result ----->
    |         |                                           |       |
    |         +-------------------------------------------+       |
    +-------------------------------------------------------------+
    */
  private val fibonacciSource = Source.fromGraph(
    GraphDSL.create() { implicit builder =>
      val one = builder.add(Source.single(1))
      val queue = builder.add(Concat[Int](2))
      val join = builder.add(zipWithPrev)
      val add = builder.add(Flow[(Int, Int)].map(x => x._1 + x._2))
      val result = builder.add(Broadcast[Int](2))

      one ~> queue.in(0)
      queue.out ~> join ~> add ~> result
      result.out(0) ~> queue.in(1)

      SourceShape(result.out(1))
    }
  )

  fibonacciSource.throttle(2, 1 second).to(printSink("fibonacci >")).run

}
