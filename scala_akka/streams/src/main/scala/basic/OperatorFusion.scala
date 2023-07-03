package basic

import akka.actor.{Actor, Props}
import akka.stream.scaladsl.{Flow, Sink, Source}
import common.StreamApp

object OperatorFusion extends StreamApp {

  private val source = Source(1 to 10)
  private val addFlow = Flow[Int].map(_ + 1)
  private val mulFlow = Flow[Int].map(_ * 10)
  private def sink(tag: String) = Sink.foreach[Int](i => println(s"$tag > $i"))

  // Operator/component fusion
  source.via(addFlow).via(mulFlow).to(sink("fusion")).run() // this runs on the same actor

  // Equivalent (kinda) to the following actor
  private class SimpleActor extends Actor {
    override def receive: Receive = {
      case i: Int =>
        val i2 = i + 1      // addFlow
        val i3 = i2 * 10    // mulFlow
        println(s"actor > $i3")         // sink
    }
  }
  private val simpleActor = actorSystem.actorOf(Props[SimpleActor])
  (1 to 10).foreach(simpleActor ! _)  // source

  // Complex flows
  private val complexAddFlow = Flow[Int].map {
    x =>
      Thread.sleep(500)
      x + 1
  }
  private val complexMulFlow = Flow[Int].map {
    x =>
      Thread.sleep(500)
      x * 10
  }
  source.via(complexAddFlow).via(complexMulFlow).to(sink("complex")).run()   // 2s per action, costly

  // Specifying async boundaries to "detach" costly tasks
  source.via(complexAddFlow).async    // runs on one actor
    .via(complexMulFlow).async
    .to(sink("async_boundary"))
    .run()

  // Ordering guarantees
  source.map(peek("Flow A")).map(peek("Flow B")).map(peek("Flow C")).runWith(Sink.ignore)
  source
    .map(peek("Async A")).async
    .map(peek("Async B")).async
    .map(peek("Async C")).async
    .runWith(Sink.ignore)


  private def peek(tag: String)(i: Int): Int = {
    println(s"$tag > $i")
    i
  }

}
