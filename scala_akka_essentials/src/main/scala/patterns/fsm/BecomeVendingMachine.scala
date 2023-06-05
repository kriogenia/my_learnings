package patterns.fsm

import akka.actor.{ActorRef, Cancellable}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class BecomeVendingMachine extends VendingMachine {
  import BecomeVendingMachine._
  import VendingMachine.Input._
  import VendingMachine.Output._

  implicit val executionContext: ExecutionContext = context.dispatcher

  override def receive: Receive = idle()

  private def idle(): Receive = {
    case Initialize(inventory, prices) => context.become(operational(Inventory(inventory, prices)))
    case _ => sender() ! VendingError("MachineNotInitialized")
  }

  private def operational(inventory: Inventory): Receive = {
    case RequestProduct(product) =>
      inventory.stock.get(product) match {
        case None | Some(0) => sender() ! VendingError("ProductNotAvailable")
        case Some(_) =>
          val price = inventory.prices(product)
          sender() ! Instruction(s"Please, insert ${price}€")
          val request = ProductRequest(product, price, sender(), startMoneyWaitTimeout)
          context.become(waitingForMoney(inventory, request))
      }
  }

  private def waitingForMoney(inventory: Inventory, request: ProductRequest): Receive = {
    case ReceiveMoneyTimeout =>
      request.requester ! VendingError("RequestTimeout")
      val inserted = inventory.prices(request.product) - request.priceLeft
      if (inserted > 0) request.requester ! Change(inserted)
      context.become(operational(inventory))

    case InsertMoney(amount) if amount >= request.priceLeft =>  // paid
      request.timeout.cancel()

      request.requester ! Deliver(request.product)
      val change = amount - request.priceLeft
      if (change > 0) request.requester ! Change(change)

      val updatedInventory = inventory.copy(
        stock = inventory.stock + (request.product -> (inventory.stock(request.product) - 1)))
      context.become(operational(updatedInventory))

    case InsertMoney(amount) =>                                  // not yet paid
      request.timeout.cancel()

      val priceLeft = request.priceLeft - amount
      request.requester ! Instruction(s"Please, insert ${priceLeft}€")

      val newRequest = request.copy(priceLeft = priceLeft, timeout = startMoneyWaitTimeout)
      context.become(waitingForMoney(inventory, newRequest))
  }

  private def startMoneyWaitTimeout = context.system.scheduler.scheduleOnce(1 second) {
    self ! ReceiveMoneyTimeout
  }

}
private object BecomeVendingMachine {
  import VendingMachine.Types._

  private case object ReceiveMoneyTimeout

  private case class Inventory(stock: Stock, prices: Prices)
  private case class ProductRequest(product: String, priceLeft: Money, requester: ActorRef, timeout: Cancellable)
}