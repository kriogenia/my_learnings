package graphs

import akka.stream.{ClosedShape, OverflowStrategy}
import akka.stream.scaladsl.GraphDSL.Implicits.{SourceArrow, SourceShapeArrow}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, MergePreferred, RunnableGraph, Source}
import common.StreamApp

object Cycles extends StreamApp {

  // Cycles risk deadlocking, can be addressed adding bounds to the number of elements in the cycle (like buffering)

  private val source = Source(1 to 100)
  private def incrementer(tag: String) = Flow[Int].map { x =>
    println(s"$tag\tAccelerating $x")
    Thread.sleep(1000)
    x + 1
  }

  /*
    Graph cycle deadlock, source keeps feeding the merger as the incrementer does to, reaches backpressure and stops

    +-----------------------------------------+
    | source -----+--> merger --> incrementer |
    |             |                     |     |
    |             |                     |     |
    |             +---------------------+     |
    +-----------------------------------------+
   */
  private val deadLockAccelerator = GraphDSL.create() { implicit builder =>
    val merger = builder.add(Merge[Int](2))
    val inc = builder.add(incrementer("deadlock"))

    source ~> merger ~> inc ~> merger

    ClosedShape
  }
  RunnableGraph.fromGraph(deadLockAccelerator).run

  /*
    Solution 1: MergePreferred, it will give priority to the preferred inlet

    +-----------------------------------+
    | source --> merger --> incrementer |
    |              /^\            |     |
    |               |             |     |
    |               +-------------+     |
    +-----------------------------------+
   */
  private val mergePreferredAccelerator = GraphDSL.create() { implicit builder =>
    val merger = builder.add(MergePreferred[Int](1))
    val inc = builder.add(incrementer("mergePreferred"))

    source ~> merger ~> inc ~> merger.preferred

    ClosedShape
  }
  RunnableGraph.fromGraph(mergePreferredAccelerator).run

  /*
    Solution 2: Use buffers to evade the backpressure
   */
  private val bufferedAccelerator = GraphDSL.create() { implicit builder =>
    val incrementer = Flow[Int].buffer(10, OverflowStrategy.dropHead).map { x =>
      println(s"buffered\tAccelerating $x")
      Thread.sleep(1000)
      x + 1
    }

    val merger = builder.add(Merge[Int](2))
    val inc = builder.add(incrementer)

    source ~> merger ~> inc ~> merger

    ClosedShape
  }
  RunnableGraph.fromGraph(bufferedAccelerator).run

}
