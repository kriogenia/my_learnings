package basic

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import common.StreamApp

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object Backpressure extends StreamApp {

  private val fastSource = Source(1 to 100)
  private def slowSink(tag: String) = Sink.foreach[Int] { x =>
    Thread.sleep(1000)    // simulate long processing
    println(s"$tag: $x")
  }

  fastSource.to(slowSink("fusing > ")).run()        // fusing -> not backpressure

  fastSource.to(slowSink("async > ")).async.run()   // backpressure

  private def simpleFlow(tag: String) = Flow[Int].map { x =>
    println(s"$tag: $x")
    x + 1
  }

  fastSource.via(simpleFlow("flow >> ")).async.to(slowSink("backpressure > ")).async.run()

  /*
    Reactions to the backpressure (in order):
      - Try to slowdown if possible
      - Buffer elements until there's more demand
      - Dropdown elements from the buffer if it overflows
      - Teardown/kill the whole stream (failure)
   */

  private val bufferedFlow = simpleFlow("buffer >> ").buffer(10, overflowStrategy = OverflowStrategy.dropHead)
  fastSource.via(bufferedFlow).async.to(slowSink("buffered > ")).async.run()
  // 1-16 no backpressure, 17-26 backpressure kicks and flow will buffer, 27-90 will be dropped, 91-100 will survive

  /*
    Overflow strategies:
      - drop head => oldest element in the buffer:      1-10 + 11 = 2-11
      - drop tail => newest element in the buffer:      1-10 + 11 = 1-9, 11
      - drop new => (deprecated) element to be added:   1-10 + 11 = 1-10
      - drop buffer => entire buffer                    1-10 + 11 = 11
      - backpressure => sends the signal upstream       1-10 !(+ 11)
      - fail => fails the graph execution
   */

  // Manually proc a backpressure -> throttling
  fastSource.throttle(2, 1 second).runWith(Sink.foreach(x => println(s"throttled > $x")))

}
