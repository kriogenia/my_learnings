package patterns.fsm

import akka.actor.{ActorRef, FSM}
import patterns.fsm.VendingMachine.Types.{Money, Prices, Stock}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

trait VendingState
private case object Idle extends VendingState
case object Operational extends VendingState
case object WaitingForMoney extends VendingState


trait VendingData
case object Uninitialized extends VendingData
case class Initialized(stock: Stock, prices: Prices) extends VendingData
case class WaitingData(
                    stock: Stock,
                    prices: Prices,
                    product: String,
                    priceLeft: Money,
                    requester: ActorRef
                  ) extends VendingData

class FiniteStateVendingMachine extends FSM[VendingState, VendingData] with VendingMachine {
  import VendingMachine.Input._
  import VendingMachine.Output._

  // no receive, manage EVENT(message, data)

  startWith(Idle, Uninitialized)

  when(Idle) {
    case Event(Initialize(stock, prices), Uninitialized) =>
      goto(Operational) using Initialized(stock, prices)
    case _ =>
      sender() ! VendingError("MachineNotInitialized")
      stay()
  }

  when(Operational) {
    case Event(RequestProduct(product), Initialized(stock, prices)) =>
      stock.get(product) match {
        case None | Some(0) =>
          sender() ! VendingError("ProductNotAvailable")
          stay()
        case Some(_) =>
          val price = prices(product)
          sender() ! Instruction(s"Please, insert ${price}€")
          goto(WaitingForMoney) using WaitingData(
            stock = stock,
            prices = prices,
            product = product,
            priceLeft = price,
            requester = sender()
          )
      }
  }

  when(WaitingForMoney, stateTimeout = 1 second) {
    case Event(StateTimeout, WaitingData(stock, prices, product, priceLeft, requester)) =>
      requester ! VendingError("RequestTimeout")
      val inserted = prices(product) - priceLeft
      if (inserted > 0) requester ! Change(inserted)
      goto(Operational) using Initialized(stock, prices)

    case Event(InsertMoney(amount), WaitingData(stock, prices, product, priceLeft, requester)) if amount >= priceLeft =>
      requester ! Deliver(product)

      val change = amount - priceLeft
      if (change > 0) requester ! Change(change)

      val updatedStock = stock + (product -> (stock(product) - 1))
      goto(Operational) using Initialized(updatedStock, prices)

    case Event(InsertMoney(amount), data: WaitingData) =>
      val newPriceLeft = data.priceLeft - amount
      data.requester ! Instruction(s"Please, insert ${newPriceLeft}€")

      goto(WaitingForMoney) using data.copy(priceLeft = newPriceLeft)
  }

  whenUnhandled {
    case Event(_, _) =>
      sender() ! VendingError("CommandNotFound")
      stay()
  }

  onTransition {
    case from -> to => log.info(s"Transitioning from $from to $to")
  }

  initialize()

}