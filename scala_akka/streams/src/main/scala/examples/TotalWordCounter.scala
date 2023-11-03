package examples

import akka.stream.scaladsl.{Sink, Source}
import common.StreamApp

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object TotalWordCounter extends StreamApp {

  private val sentences = List(
    "The quick brown fox jumps over the lazy dog",                        // 9
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit",            // 8
    "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"   // 11
  )

  private val words = countWords(sentences)
  println(s"The total number of words is $words")

  private def countWords(sentences: List[String]): Int = {
    val result = Source(sentences)
      .map(_.split(" ").length)
      .runWith(Sink.fold(0)((length, sum) => sum + length))
    Await.result(result, 100 millis)
  }

}
