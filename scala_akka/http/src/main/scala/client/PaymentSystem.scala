package client

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import client.PaymentSystemDomain.PaymentRequest
import common.HttpApp
import spray.json._

case class CreditCard(number: String, cvc: String, owner: String)

object PaymentSystemDomain {
  case class PaymentRequest(cc: CreditCard, receiverAccount: String, amount: Double)
  case object PaymentAccepted
  case object PaymentRejected
}

trait PaymentJsonProtocol extends DefaultJsonProtocol {
  implicit val creditCardFormat: RootJsonFormat[CreditCard] = jsonFormat3(CreditCard)
  implicit val paymentRequestFormat: RootJsonFormat[PaymentSystemDomain.PaymentRequest] = jsonFormat3(PaymentSystemDomain.PaymentRequest)
}

class PaymentValidator extends Actor with ActorLogging {
  import PaymentSystemDomain._

  override def receive: Receive = {
    case PaymentRequest(CreditCard(number, _, senderAccount), receiverAccount, amount) =>
      log.info(s"$senderAccount attempting to send $amount € to $receiverAccount")
      if (number.length < 12) {
        sender() ! PaymentRejected
      } else {
        sender() ! PaymentAccepted
      }
  }
}

object PaymentSystemExample extends PaymentJsonProtocol {
  val httpRequests: Seq[HttpRequest] = List(
    CreditCard("1234-1234-1234-1234", "123", "Owner McOwner"),
    CreditCard("1234", "124", "Too short"),
    CreditCard("1234-1234-1234-1236", "125", "Credit Cardson"),
  )
    .map(cc => PaymentRequest(cc, "store", 99))
    .map(pr => HttpRequest(
      HttpMethods.POST,
      uri = Uri("/api/payments"),
      entity = HttpEntity(
        ContentTypes.`application/json`,
        pr.toJson.prettyPrint,
      ),
    ))
}

object PaymentSystem extends HttpApp with PaymentJsonProtocol with SprayJsonSupport {
  import PaymentSystemDomain._

  private val paymentValidator = actorSystem.actorOf(Props[PaymentValidator], "payment-validator")

  import actorSystem.dispatcher
  private val paymentRoute = path("api" / "payments") {
    post {
      entity(as[PaymentRequest]) { request =>
        println(request)
        val validationResponseFuture = (paymentValidator ? request).map {
          case PaymentRejected => StatusCodes.Forbidden
          case PaymentAccepted => StatusCodes.OK
        }
        complete(validationResponseFuture)
      }
    }
  }

  Http().newServerAt(host, defaultPort).bind(paymentRoute)

}
