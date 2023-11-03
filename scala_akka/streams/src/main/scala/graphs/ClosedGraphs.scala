package graphs

import akka.NotUsed
import akka.stream.ClosedShape
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, Zip}
import common.StreamApp


object ClosedGraphs extends StreamApp {

  private val input = Source(1 to 100)
  private val sumFlow = Flow[Int].map(_ + 1)        // assumed hard computation
  private val mulFlow = Flow[Int].map(_ * 10)       // assumed hard computation
  private val output = Sink.foreach[(Int, Int)](println)

  /*
            +--> sumFlow ---+
    source -|               |--> output
            +--> addFlow ---+
   */

  // 1. Set up the fundamentals for the graph
  private val graph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      // 2. Add the necessary components of the graph
      val broadcast = builder.add(Broadcast[Int](2))   // fan-out operator
      val zip = builder.add(Zip[Int, Int])                        // fan-in operator

      // 3. Tie up the components
      input ~> broadcast
      broadcast.out(0) ~> sumFlow ~> zip.in0
      broadcast.out(1) ~> mulFlow ~> zip.in1
      zip.out ~> output

      // 4. Return a closed shape
      ClosedShape
    } // static graph
  ) // runnable graph
  graph.run() // materialized graph

}
