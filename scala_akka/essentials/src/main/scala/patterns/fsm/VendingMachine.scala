package patterns.fsm

import akka.actor.{Actor, ActorLogging}

import scala.language.postfixOps


trait VendingMachine extends Actor with ActorLogging
case object VendingMachine {
  import Types._

  case object Input {
    case class Initialize(stock: Stock, prices: Prices)
    case class RequestProduct(product: String)
    case class InsertMoney(amount: Money)
  }

  case object Output {
    case class Instruction(instruction: String) // message to show in the screen
    case class Deliver(product: String)
    case class Change(amount: Money)
    case class VendingError(reason: String)
  }

  case object Types {
    type Money = Double
    type Stock = Map[String, Int]
    type Prices = Map[String, Money]
  }

}
