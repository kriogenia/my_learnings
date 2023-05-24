package examples

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object BankAccountActor extends App {

  private class BankAccount(balance: Int) extends Actor {
    import BankAccount._
    override def receive: Receive = onMessage(balance)

    private def onMessage(balance: Int): Receive = {
      case Operations.Deposit(qty) => onDeposit(balance, qty)
      case Operations.Withdraw(qty) => onWithdraw(balance, qty)
      case Operations.Statement => sender() ! Success(s"Current balance of the account is $balance")
    }

    private def onDeposit(balance: Int, toDeposit: Int): Unit = {
      if (toDeposit < 0) {
        sender() ! Failure("You can't deposit negative amounts")
      } else {
        context.become(onMessage(balance + toDeposit))
        sender() ! Success("Deposit complete")
      }
    }

    private def onWithdraw(balance: Int, toWithdraw: Int): Unit = {
      (toWithdraw < 0, toWithdraw > balance) match {
        case (true, _) => sender() ! Failure("You can't withdraw negative amounts")
        case (_, true) => sender() ! Failure("Not enough balance to withdraw the quantity")
        case _ =>
          context.become(onMessage(balance - toWithdraw))
          sender() ! Success("Withdraw complete")
      }
    }
  }

  private object BankAccount {
    case class Success(message: String)
    case class Failure(message: String)
  }

  private class ATMSession(ref: ActorRef) extends Actor {
    override def receive: Receive = {
      case deposit: Operations.Deposit => ref ! deposit
      case withdraw: Operations.Withdraw => ref ! withdraw
      case Operations.Statement => ref ! Operations.Statement

      case BankAccount.Success(msg) => println(s"[Success] $msg")
      case BankAccount.Failure(msg) => println(s"[Failure] ${msg}")
    }
  }

  private case object Operations {
    case class Deposit(qty: Int)
    case class Withdraw(qty: Int)
    case object Statement
  }


  private val system = ActorSystem("bank_system")
  private val account = system.actorOf(Props(new BankAccount(100)), "account")
  private val atm = system.actorOf(Props(new ATMSession(account)))

  atm ! Operations.Deposit(20)
  atm ! Operations.Statement

  atm ! Operations.Withdraw(70)
  atm ! Operations.Statement

  atm ! Operations.Withdraw(70)
  atm ! Operations.Statement

  atm ! Operations.Deposit(-20)
  atm ! Operations.Withdraw(-20)

}
