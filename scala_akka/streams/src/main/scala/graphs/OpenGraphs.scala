package graphs

import akka.stream.{FlowShape, SinkShape, SourceShape}
import akka.stream.scaladsl.GraphDSL.Implicits.{SourceArrow, fanOut2flow}
import akka.stream.scaladsl.{Broadcast, Concat, Flow, GraphDSL, Sink, Source, Zip}
import common.StreamApp

object OpenGraphs extends StreamApp {

  /*
    A composite source that concatenates two sources
      - emits all the elements from the first source
      - then all the elements from the second source
   */

  private val firstSource  = Source(1 to 10)
  private val secondSource = Source(20 to 100)

  /*
    +-------------------------------+
    |                               |
    | firstSource ----+             |
    |                 |--> concat ----->
    | secondSource ---+             |
    |                               |
    +-------------------------------+
   */

  // 1. Set up the fundamentals for the source graph
  private val sourceGraph = Source.fromGraph(
    GraphDSL.create() { implicit builder =>
      // 2. Add the necessary components of the graph
      val concat = builder.add(Concat[Int](2))

      // 3. Tie up the components
      firstSource ~> concat
      secondSource ~> concat

      // 4. Return a source shape
      SourceShape(concat.out)
    }
  )

  sourceGraph.runWith(Sink.foreach[Int](println))

  /*
    A complex sink that broadcast the content to two sinks
   */

  private val sinkLeft = Sink.foreach[Int](x => println(s"LEFT\t$x"))
  private val sinkRight = Sink.foreach[Int](x => println(s"RIGHT\t$x"))

  /*
    +-------------------------------+
    |                               |
    |             + --> sinkLeft    |
  ----> concat ---|                 |
    |             + --> sinkRight   |
    |                               |
    +-------------------------------+
   */

  private val sinkGraph = Sink.fromGraph(
    GraphDSL.create() { implicit builder =>
      val broadcast = builder.add(Broadcast[Int](2))
      broadcast ~> sinkLeft
      broadcast ~> sinkRight
      SinkShape(broadcast.in)
    }
  )
  sinkGraph.runWith(Source(1 to 100))

  /*
    A complex flow composed of two flows:
      - the first one adds one to the number
      - the second one multiplies the number by 10
   */

  private val sumFlow = Flow[Int].map(_ + 1)
  private val mulFlow = Flow[Int].map(_ * 10)

  /*
    +-----------------------+
    |                       |
    |  + --> sumFlow ---+   |
  -----|                |===--->
    |  + --> mulFlow ---+   |
    |                       |
    +-----------------------+
   */

  private val flowGraph = Flow.fromGraph(
    GraphDSL.create() { implicit builder =>
      val broadcast = builder.add(Broadcast[Int](2))
      val zip = builder.add(Zip[Int, Int]())

      broadcast ~> sumFlow ~> zip.in0
      broadcast ~> mulFlow ~> zip.in1

      FlowShape(broadcast.in, zip.out)
    }
  )
  flowGraph.runWith(Source(1 to 100), Sink.foreach[(Int, Int)](println))

}