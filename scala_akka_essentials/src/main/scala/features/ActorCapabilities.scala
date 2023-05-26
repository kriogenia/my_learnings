package features

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {

  private val system = ActorSystem("system")

  private class SimpleActor extends Actor {
    override def receive: Receive = {
      case "ping" =>
        println(s"[$self] Received message: ping")
        sender() ! "pong"
      case message: String => println(s"[$self] Received message: $message")
      case number: Int => println(s"[$self] Received number: $number")
      case CustomMessage(content) => println(s"[$self] Received custom: $content")
      case MessageYourself(content) => self ! content
      case SayTo(content, destiny) => destiny ! content
      case Forward(content, destiny) => destiny forward s"[Forwarded] $content" // override tell(..., sender())
    }
  }

  // 1. Messages can be of any type as long as they are immutable and serializable
  private case class CustomMessage(content: String) // scala case class are both immutable and serializable

  private val simpleActor = system.actorOf(Props[SimpleActor], "simple_actor")
  simpleActor ! "Hello, actor"
  simpleActor ! 42
  simpleActor ! CustomMessage("custom content")

  // 2. Actors have information about their context and themselves
  private case class MessageYourself(content: String)
  simpleActor ! MessageYourself("I'm an actor") // uses self to message itself

  // 3. Actors can reply to messages
  private val alice = system.actorOf(Props[SimpleActor], "alice")
  private val bob = system.actorOf(Props[SimpleActor], "bob")

  private case class SayTo(msg: String, ref: ActorRef)
  alice ! SayTo("ping", bob)

  // 4. Replying back to the program is a dead letter
  alice ! "ping"

  // 5. Actors can forward messages
  private case class Forward(msg: String, ref: ActorRef)

  alice ! Forward("Please, pass this message", bob)
}
