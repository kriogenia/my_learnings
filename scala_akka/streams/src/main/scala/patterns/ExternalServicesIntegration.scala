package patterns

import akka.actor.{Actor, Props}
import akka.dispatch.MessageDispatcher
import akka.pattern.ask
import akka.stream.scaladsl.Source
import akka.util.Timeout
import common.StreamApp

import java.time.LocalDateTime
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object ExternalServicesIntegration extends StreamApp {

  implicit val dispatched: MessageDispatcher = actorSystem.dispatchers.lookup("dedicated-dispatcher")

  /*
    Service mock, alert service like PagerDuty
   */
  private case class PagerEvent(app: String, description: String, date: LocalDateTime)
  private object PagerMock {
    private val engineers = List("Senior McEnginneer", "Q.A. Testerson", "Backend Juniorez Ingeniero")
    private val emails = Map(
      "Senior McEnginneer" -> "seniorm@software.co",
      "Q.A. Testerson" -> "qat@software.co",
      "Backend Juniorez Ingeniero" -> "backendj@software.co",
    )

    def processEvent(event: PagerEvent): String = {
      val engineerOnCall = engineers(event.date.getDayOfYear % engineers.length)
      val email = emails(engineerOnCall)
      println(s"Emailing engineer $engineerOnCall ($email): $event")
      email
    }
  }

  private val eventSource = Source(Seq(
    PagerEvent("AkkaInfra", "Dead letters", LocalDateTime.now()),
    PagerEvent("SearchService", "Solr not available", LocalDateTime.now()),
    PagerEvent("SearchService", "NullPointerException at line 3", LocalDateTime.now()),
    PagerEvent("LanguageService", "Missing configuration for language jp", LocalDateTime.now()),
  ))

  private object PagerService {
    def processEvent(event: PagerEvent): Future[String] = Future {
      Thread.sleep(1000)
      PagerMock.processEvent(event)
    }
  }

  private val searchServiceEvent = eventSource.filter(_.app == "SearchService")
  private val pagedEmails = searchServiceEvent.mapAsync(parallelism = 4)(event => PagerService.processEvent(event))
  pagedEmails.runWith(printSink("Service"))

  /*
    Alternative using Actor
   */

  private class PagerActor extends Actor {
    override def receive: Receive = {
      case event: PagerEvent =>
        Thread.sleep(1000)
        sender() ! PagerMock.processEvent(event)
    }
  }

  implicit val timeout: Timeout = Timeout(2 seconds)
  private val pagerActor = actorSystem.actorOf(Props[PagerActor], "pager-actor")
  private val altPagedEmails = eventSource.filter(_.app == "LanguageService").mapAsync(parallelism = 4)(pagerActor ? _)
  altPagedEmails.runWith(printSink("Actor"))

}
