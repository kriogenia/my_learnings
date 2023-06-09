package testing

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class LogInterceptionSpec
  extends TestKit(ActorSystem("test_probe", ConfigFactory.load().getConfig("interceptingLogMessages")))
  with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll
{

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import LogInterceptionSpec._

  val item = "testItem"

  "a checkout flow" should {
    "correctly log the dispatching of an order" in {
      EventFilter.info(pattern = s"Order [0-9]+ for item $item has been dispatched", occurrences = 1) intercept {
        val checkout = system.actorOf(Props[CheckoutActor])
        checkout ! Checkout(item, "1234")
      }
    }

    "freak out if the payment is denied" in {
      EventFilter[RuntimeException](occurrences = 1) intercept {
        val checkout = system.actorOf(Props[CheckoutActor])
        checkout ! Checkout(item, "0")
      }
    }
  }

}
object LogInterceptionSpec {

  private case class Checkout(item: String, creditCard: String)
  private case class AuthorizeCard(creditCard: String)
  private case class DispatchOrder(item: String)
  private case object PaymentAccepted
  private case object PaymentDenied
  private case object OrderConfirmed

  private class CheckoutActor extends Actor {
    private val paymentManager = context.actorOf(Props[PaymentManager])
    private val fulfillmentManager = context.actorOf(Props[FulfillmentManager])
    override def receive: Receive = awaitingCheckout

    private def awaitingCheckout: Receive = {
      case Checkout(item, creditCard) =>
        paymentManager ! AuthorizeCard(creditCard)
        context.become(pendingPayment(item))
    }

    private def pendingPayment(item: String): Receive = {
      case PaymentAccepted =>
        fulfillmentManager ! DispatchOrder(item)
        context.become(pendingDispatching())
      case PaymentDenied =>
        throw new RuntimeException("what")
    }

    private def pendingDispatching(): Receive = {
      case OrderConfirmed =>
        context.become(awaitingCheckout)
    }
  }

  private class PaymentManager extends Actor {
    override def receive: Receive = {
      case AuthorizeCard(creditCard) => sender() ! (if (creditCard.startsWith("0")) PaymentDenied else PaymentAccepted)
    }

  }

  private class FulfillmentManager extends Actor with ActorLogging {
    private val orderId = 43

    override def receive: Receive = onMessage(orderId)

    private def onMessage(orderId: Int): Receive = {
      case DispatchOrder(item) =>
        context.become(onMessage(orderId + 1))
        log.info(s"Order $orderId for item $item has been dispatched")
        sender() ! OrderConfirmed
    }
  }


}
