package examples

import akka.stream.scaladsl.GraphDSL.Implicits.{SourceArrow, port2flow}
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink, Source, ZipWith}
import akka.stream.{ClosedShape, UniformFanInShape}
import common.StreamApp

object Max3FanIn extends StreamApp {

  /*
    Operator that receives three integers and returns the maximum of them
   */

  private val oneToTen = Source(1 to 10)
  private val tenToOne = Source((1 to 10).reverse)
  private val five     = Source(1 to 10).map(_ => 5)

  private val sink = Sink.foreach(println)

  /*
    +---------------------------+
    |                           |
  ------+                       |
    |   |--> max1 --+           |
  ------+           |           |
    |               |--> max2 ----->
    |               |           |
  ------------------+           |
    |                           |
    +---------------------------+
   */

  private val max3StaticGraph = GraphDSL.create() { implicit builder =>
    val max1 = builder.add(ZipWith[Int, Int, Int]((a, b) => Math.max(a, b)))
    val max2 = builder.add(ZipWith[Int, Int, Int]((a, b) => Math.max(a, b)))

    max1.out ~> max2.in0

    UniformFanInShape(max2.out, max1.in0, max1.in1, max2.in1)
  }

  private val max3RunnableGraph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder =>
      val max3Shape = builder.add(max3StaticGraph)

      oneToTen ~> max3Shape.in(0)
      tenToOne ~> max3Shape.in(1)
      five ~> max3Shape.in(2)
      max3Shape.out ~> sink

      ClosedShape
    }
  )
  max3RunnableGraph.run()

}
