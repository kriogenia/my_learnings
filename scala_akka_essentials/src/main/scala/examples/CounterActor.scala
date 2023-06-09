package examples

import akka.actor.{Actor, ActorSystem, Props}

object CounterActor extends App {

  private class Counter extends Actor {
    import Counter._
    private val count = 0
    override def receive: Receive = onMessage(count)

    private def onMessage(count: Int): Receive = {
      case Increment => context.become(onMessage(count + 1))
      case Decrement => context.become(onMessage(count - 1))
      case Print => println(s"Current count: $count")
    }
  }

  // Domain of the counter
  private object Counter {
    case object Increment
    case object Decrement
    case object Print
  }




  val system = ActorSystem("counter-system")

  private val counter = system.actorOf(Props[Counter], "counter")
  counter ! Counter.Print

  counter ! Counter.Increment
  counter ! Counter.Increment
  counter ! Counter.Print

  counter ! Counter.Decrement
  counter ! Counter.Print

  counter ! Counter.Decrement
  counter ! Counter.Increment
  counter ! Counter.Print

}
