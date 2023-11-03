package examples

import akka.stream.FlowShape
import akka.stream.scaladsl.GraphDSL.Implicits.fanOut2flow
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Sink, Source}
import common.StreamApp

import scala.concurrent.Future

object EnhanceComponent extends StreamApp {

  private val counter = Sink.fold[Int, Any](0)((count, _) => count + 1)

  /*
        +-------------------------------------+
        |                 +--> flowShape ---------> B
    A -----> broadcast ---|                   |
        |                 +--> counterShape |=====> Int
        +-------------------------------------+
   */
  private def enhanceFlowWithCount[A, B](flow: Flow[A, B, _]): Flow[A, B, Future[Int]] = Flow.fromGraph(
    GraphDSL.createGraph(flow, counter)((_, counterMatValue) => counterMatValue) {
      implicit builder => (flowShape, counterShape) =>
        val broadcast = builder.add(Broadcast[A](2))

        broadcast ~> flowShape
        broadcast ~> counterShape

        FlowShape(broadcast.in, flowShape.out)
    }
  )

  val sum = Flow[Int].map(_ + 1)
  val mul = Flow[Int].map(_ * 2)

  private def runEnhanced(tag: String, flow: Flow[Int, _, _], limit: Int): Future[Int] = {
    val enhanced = enhanceFlowWithCount(flow)
    Source(1 to limit).viaMat(enhanced)(Keep.right).toMat(printSink(tag))(Keep.left).run
  }

  runEnhanced("S", sum, 100).onComplete(i => println(s"The number of sums is $i"))
  runEnhanced("M", mul, 50).onComplete(i => println(s"The number of muls is $i"))

}
