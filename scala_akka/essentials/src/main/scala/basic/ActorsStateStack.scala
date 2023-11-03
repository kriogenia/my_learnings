package basic

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorsStateStack extends App {

  private object ByteStack {
    case class Push(byte: Int)
    case object Pop
  }
  private class ByteStack extends Actor {
    import ByteStack._

    private val current = 0

    override def receive: Receive = onMessage(current)

    private def onMessage(current: Int): Receive = {
      case Push(byte) => context.become(onMessage(byte), discardOld = false)
      case Pop =>
        sender() ! current
        context.unbecome()
    }
  }

  private object StringInverter {
    case class Start(string: String)
  }
  private class StringInverter(stack: ActorRef) extends Actor {
    import ByteStack._
    import StringInverter._

    override def receive: Receive = onMessage(stack)

    private def onMessage(stack: ActorRef): Receive = {
      case 0 =>
      case byte: Int =>
        println(s"[${byte.toChar}]")
        stack ! Pop
      case Start(string: String) =>
        string.chars().forEach(c => stack ! Push(c))
        stack ! Pop
    }
  }

  private val system = ActorSystem("stack_system")
  private val stack = system.actorOf(Props[ByteStack], "stack")
  private val inverter = system.actorOf(Props(new StringInverter(stack)), "inverter")

  inverter ! StringInverter.Start("string")

}
