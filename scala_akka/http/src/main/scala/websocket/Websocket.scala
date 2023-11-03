package websocket

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.CompactByteString
import common.HttpApp

import scala.concurrent.duration._
import scala.language.postfixOps

object Websocket extends HttpApp {

  // val textMsg = TextMessage(Source.single("Hello World"))
  // val binMsg = BinaryMessage(Source.single(CompactByteString("Foo bar")))

  private val html =
    """
      |<html>
      |<head>
      |    <script>
      |        const socialSocket = new WebSocket("ws://localhost:8080/social");
      |        const greeterSocket = new WebSocket("ws://localhost:8080/greeter");
      |
      |        console.log("Starting websockets");
      |
      |        greeterSocket.onopen = (event) => {
      |            greeterSocket.send("Data Race!!");
      |        }
      |        greeterSocket.onmessage = (event) => {
      |            const newChild = document.createElement("h4");
      |            newChild.innerText = event.data;
      |            document.getElementById("main").appendChild(newChild);
      |        }
      |
      |        socialSocket.onmessage = (event) => {
      |            const newChild = document.createElement("p");
      |            newChild.innerText = event.data;
      |            document.getElementById("main").appendChild(newChild);
      |        }
      |        socialSocket.onopen = (event) => {
      |            console.log(event);
      |        }
      |
      |        greeterSocket.send("This will fail");
      |    </script>
      |</head>
      |<body>
      |<h3>WebSocket Pront</h3>
      |<div id="main">
      |</div>
      |</body>
      |</html>
      |""".stripMargin

  private case class SocialPost(content: String, owner: String)
  private val socialFeed = Source(
    List(
      SocialPost("CorporationTM", "Haha unfunny joke"),
      SocialPost("EnterpriseSA", "youre so funny m8"),
      SocialPost("CorporationTM", "We are so revolutionary!")
    )
  )

  private val socialMessages = socialFeed
    .throttle(1, 2 second)
    .map(post => TextMessage(s"${post.owner}: ${post.content}"))
  private val socialFlow: Flow[Message, Message, Any] = Flow.fromSinkAndSource(
    Sink.foreach[Message](println),
    socialMessages
  )

  private val websocketFlow: Flow[Message, Message, Any] = Flow[Message].map {
    case tm: TextMessage =>
      TextMessage(Source.single(s"I received your ") ++ tm.textStream ++ Source.single("!"))
    case bm: BinaryMessage =>
      bm.dataStream.runWith(Sink.ignore)
      TextMessage(Source.single("Binary messaged received"))
  }

  private val websocketRoute = (pathEndOrSingleSlash & get) {
    complete(HttpEntity(
    ContentTypes.`text/html(UTF-8)`,
    html
    ))
  } ~
  path("greeter") {
    handleWebSocketMessages(websocketFlow)
  } ~
  path("social") {
    handleWebSocketMessages(socialFlow)
  }


  Http().newServerAt(host, defaultPort).bind(websocketRoute)

}
