package graphs

import akka.NotUsed
import akka.stream.ClosedShape
import akka.stream.scaladsl.{Balance, GraphDSL, Merge, RunnableGraph, Sink, Source}
import common.StreamApp

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object MergeBalance extends StreamApp {

  private val input = Source(1 to 100)
  private val fastSource = input.throttle(5, 10 millis).map(i => s"f$i")
  private val slowSource = input.throttle(2, 10 millis).map(i => s"s$i")

  private val leftSink = printSink[String]("LEFT")
  private val rightSink = printSink[String]("RIGHT")

  /*
      fastSource ---+                         +--> leftSink
                    |--> merge ---> balance --|
      slowSource ---+                         +--> rightSink
   */

  private val graph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val merge = builder.add(Merge[String](2))
      val balance = builder.add(Balance[String](2))

      fastSource ~> merge
      slowSource ~> merge
      merge ~> balance
      balance ~> leftSink
      balance ~> rightSink

      ClosedShape
    }
  )
  graph.run()

}
