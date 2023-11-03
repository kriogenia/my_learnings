package patterns.fsm

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, OneInstancePerTest}
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.DurationInt
import scala.language.{postfixOps, reflectiveCalls}
import scala.reflect.ClassTag

class VendingMachineSpec extends TestKit(ActorSystem("fms_spec"))
  with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll with OneInstancePerTest
{

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "a become vending machine" should {
    testImplementation[BecomeVendingMachine]()
  }

  "a finite state vending machine" should {
    testImplementation[FiniteStateVendingMachine]()
  }

  private def testImplementation[T <: Actor: ClassTag](): Unit = {
    import VendingMachine.Input._
    import VendingMachine.Output._

    val VALID = "valid"

    "send error when not initialized" in {
      val vm = system.actorOf(Props[T])

      vm ! RequestProduct("invalid")
      expectMsg(VendingError("MachineNotInitialized"))
    }

    "report a product not available" in {
      val vm = createVendingMachine()

      vm ! RequestProduct("invalid")
      expectMsg(VendingError("ProductNotAvailable"))
    }

    "throw a timeout if no money is inserted" in {
      val vm = createVendingMachine()
      requestValidProduct(vm)

      within(2 second) {
        expectMsg(VendingError("RequestTimeout"))
      }
    }

    "handle the reception of partial money" in {
      val vm = createVendingMachine()
      requestValidProduct(vm)

      vm ! InsertMoney(0.5)
      expectMsg(Instruction("Please, insert 0.5€"))

      within(2 second) {
        expectMsg(VendingError("RequestTimeout"))
        expectMsg(Change(0.5))
      }
    }

    "deliver the product if the money inserted matches the price" in {
      val vm = createVendingMachine()
      requestValidProduct(vm)

      vm ! InsertMoney(1)
      expectMsg(Deliver(VALID))

      requestValidProduct(vm)
    }

    "deliver the product and change if the money inserted exceeds the price" in {
      val vm = createVendingMachine()
      requestValidProduct(vm)

      vm ! InsertMoney(1.5)
      expectMsg(Deliver(VALID))
      expectMsg(Change(0.5))

      requestValidProduct(vm)
    }

    def createVendingMachine(): ActorRef = {
      val vm = system.actorOf(Props[T])
      vm ! Initialize(Map(VALID -> 10), Map(VALID -> 1))
      vm
    }

    def requestValidProduct(vm: ActorRef): Unit = {
      vm ! RequestProduct(VALID)
      expectMsg(Instruction("Please, insert 1.0€"))
    }
  }

}
