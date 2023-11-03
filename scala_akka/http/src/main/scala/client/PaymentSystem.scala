package client

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import common.HttpApp
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

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
