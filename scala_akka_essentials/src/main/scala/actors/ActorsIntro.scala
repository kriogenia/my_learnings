package actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {

  // 1. Create actor system
  val actorSystem = ActorSystem("actor_system")

  // 2. Define actor
  private class WordCountActor extends Actor {
    private val totalWords = 0
    override def receive: Receive = onMessage(totalWords)

    private def onMessage(totalWords: Int): Receive = {
      case message: String =>
        println(s"[${getClass.getSimpleName}] Received: $message")
        context.become(onMessage(totalWords + message.split(" ").length))
      case message => println(s"[${getClass.getSimpleName}] Unable to understand ${message.toString}")
    }
  }

  // 3. Instantiate actor
  private val wordCountActor = actorSystem.actorOf(Props[WordCountActor], "word_counter_1")
  private val otherWordCountActor = actorSystem.actorOf(Props[WordCountActor], "word_counter_2")

  // 4. Communicate with the actor via reference, this is asynchronous
  wordCountActor ! "This is the first message sent to an Akka actor"  // aka "tell"
  otherWordCountActor ! "Second message, different destination"

  /////////////////

  // To create actors with constructor arguments use a companion object

  private object HumanActor {
    def props(name: String): Props = Props(new HumanActor(name))
  }

  private class HumanActor(name: String) extends Actor {
    override def receive: Receive = {
      case "Hi" => println(s"Hello, my name is $name")
      case _ =>
    }
  }

  private val brad = actorSystem.actorOf(HumanActor.props("BradPitt"))
  brad ! "Hi"

}
