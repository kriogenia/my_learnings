package example

import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.pattern.ask
import akka.util.Timeout
import common.HttpApp

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object LowLevelServer extends HttpApp {

  /*
    Low-level server API managing a counter with GET, POST and DELETE /count endpoints
   */

  // Actor to hold the counter
  private object CounterActor {
    case object Read
    case object Add
    case object Reset
  }
  private class CounterActor extends Actor {
    import CounterActor._

    override def receive: Receive = init

    private def init = handle(0)

    private def handle(count: Int): Receive = {
      case Read => sender() ! count
      case Add => context.become(handle(count + 1))
      case Reset => context.become(init)
    }

  }
  private val counter = actorSystem.actorOf(Props[CounterActor], "counter")

  private implicit val timeout: Timeout = Timeout(2 seconds)
  import actorSystem.dispatcher

  private val internalServerErrorResponse = HttpResponse(StatusCodes.InternalServerError)

  private val requestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/count"), _, _, _) =>
      (counter ? CounterActor.Read).map { count =>
        HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"$count"))
      }.recover(_ => internalServerErrorResponse)
    case HttpRequest(HttpMethods.POST, Uri.Path("/count"), _, _, _) =>
      counter ! CounterActor.Add
      Future(HttpResponse(StatusCodes.Accepted))
    case HttpRequest(HttpMethods.DELETE, Uri.Path("/count"), _, _, _) =>
      counter ! CounterActor.Reset
      Future(HttpResponse(StatusCodes.Accepted))
    case notFound: HttpRequest =>
      notFound.discardEntityBytes()
      Future(HttpResponse(StatusCodes.NotFound))
  }

  Http().newServerAt(host, defaultPort).bind(requestHandler)

}
