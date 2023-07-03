package basic

import akka.stream.scaladsl.{Flow, Sink, Source}
import common.StreamApp

import scala.concurrent.Future

//noinspection ScalaUnusedSymbol
object Intro extends StreamApp {

  // Sources emits elements
  private val source = Source(1 to 10)
  // Sinks consumes elements
  private val sink = Sink.foreach[Int](println)

  private val graph = source.to(sink)
  graph.run()

  // Flows transform elements
  private val flow = Flow[Int].map(x => x + 1)  // source.via(flow).to(sink)
  private val sourceWithFlow = source.via(flow) // sourceWithFlow.to(sink)
  private val flowWithSink = flow.to(sink)      // source.to(flowWithSink)

  // Different kind of sources
  private val finiteSource = Source.single(1)
  private val finiteCollectionSource = Source(Seq(1, 2, 3))
  private val emptySource = Source.empty[Int]
  private val infiniteSource = Source(LazyList.from(1))

  import scala.concurrent.ExecutionContext.Implicits.global
  private val futureSource = Source.future(Future(42))

  // And different kind of sinks
  private val ignoreSink = Sink.ignore
  private val foreachSink = Sink.foreach[String](println)
  private val headSink = Sink.head[Int]
  private val foldSink = Sink.fold[Int, Int](0)((a, b) => a + b)

  // Flows are usually mapped to collection operators
  private val mapFlow = Flow[Int].map(x => 2 * x)
  private val takeFlow = Flow[Int].take(5)
  // but not flatMap

  // Akka Streams API converts those collection operators to flows
  private val mapSource = source.map(x => x * 2)    // same as source.via(Flow[Int].map(x => 2 * x))
  // And consumers to sinks
  mapSource.runForeach(println)             // same as mapSource.to(Sink.foreach[String](println))

  // nulls are not allowed
  private val illegalSource = Source.single[String](null)   // the exception will be thrown here
  illegalSource.to(Sink.foreach(println)).run()

}
