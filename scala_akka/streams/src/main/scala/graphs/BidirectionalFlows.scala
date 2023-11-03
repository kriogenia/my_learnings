package graphs

import akka.stream.{BidiShape, ClosedShape}
import akka.stream.scaladsl.GraphDSL.Implicits.{ReversePortOps, SinkShapeArrow, SourceShapeArrow, port2flow}
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Source}
import common.StreamApp

object BidirectionalFlows extends StreamApp {

  private implicit val encryptionSalt: Int = 3
  private def encrypt(string: String)(implicit n: Int) = string.map(c => (c + n).toChar)
  private def decrypt(string: String)(implicit n: Int) = encrypt(string)(-n)

  /*
        +-----------------------+
    ---> 1 --> encryptFlow --> 1 --->
    <--- 2 <-- decryptFlow <-- 2 <---
        +-----------------------+
   */
  private val bidiCryptoStaticGraph = GraphDSL.create() { implicit builder =>
    val encryptFlow = builder.add(Flow[String].map(encrypt))
    val decryptFlow = builder.add(Flow[String].map(decrypt))

    BidiShape.fromFlows(encryptFlow, decryptFlow)
  }

  private val rawStrings = "The following strings are not encrypted".split(" ").toSeq
  private val rawSource = Source(rawStrings)
  private val encryptedSource = Source(rawStrings.map(encrypt))

  /*
    +-----------------------------------------------------+
    |                   +-----------+                     |
    |     rawSource ---> 1        1 ---> encryptedSink    |
    |                   |           |                     |
    | encryptedSink <--- 2        2 <--- encryptedSource  |
    |                   +-----------+                     |
    +-----------------------------------------------------+
   */
  private val cryptoBidiGraph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder =>
      val rawSourceShape = builder.add(rawSource)
      val encryptedSourceShape = builder.add(encryptedSource)
      val bidi = builder.add(bidiCryptoStaticGraph)
      val encryptedSink = builder.add(printSink("ENCRYPTED"))
      val decryptedSink = builder.add(printSink("DECRYPTED"))

      rawSourceShape ~> bidi.in1 ; bidi.out1 ~> encryptedSink
      decryptedSink <~ bidi.out2 ; bidi.in2 <~ encryptedSourceShape

      ClosedShape
    }
  )
  cryptoBidiGraph.run

}
