package infrastructure

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import akka.routing.{ActorRefRoutee, Broadcast, FromConfig, RoundRobinGroup, RoundRobinPool, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory
import common.SimpleLoggingActor

object Routers extends App {

  private val system = ActorSystem("router_demo", ConfigFactory.load().getConfig("routers"))

  private def testRouter(ref: ActorRef, key: String): Unit = {
    for (i <- 1 to 10) {
      ref ! s"\t\t$key [$i]"
    }
  }

  // manual router
  private class Manual extends Actor {
    // 1. create routees
    private val childs = for(i <- 1 to 5) yield {
      val child = context.actorOf(Props[SimpleLoggingActor], s"manual_child-$i")
      context.watch(child)
      ActorRefRoutee(child)
    }

    // 2. define router
    private val router = Router(RoundRobinRoutingLogic(), childs)

    override def receive: Receive = {
      // 4. handle the termination of the routees
      case Terminated(ref) =>
        router.removeRoutee(ref)
        val child = context.actorOf(Props[SimpleLoggingActor])
        context.watch(child)
        router.addRoutee(ActorRefRoutee(child))
      // 3. route the messages
      case message => router.route(message, sender)
    }
  }

  private val manual = system.actorOf(Props[Manual], "manual")
  testRouter(manual, "Manual")

  // pool router: router actor with its own children
  private val progPool = system.actorOf(RoundRobinPool(5).props(Props[SimpleLoggingActor]), "prog_pool")
  testRouter(progPool, "Prog")

  private val confPool = system.actorOf(FromConfig.props(Props[SimpleLoggingActor]), "conf_pool")
  testRouter(confPool, "Conf")

  // group router: router with actors created elsewhere
  private val childList = (1 to 5).map(i => system.actorOf(Props[SimpleLoggingActor], s"group_child-$i")).toList
  private val childPaths = childList.map(ref => ref.path.toString)

  private val group = system.actorOf(RoundRobinGroup(childPaths).props())
  testRouter(group, "Group") //dunno why it doesn't work

  // special messages
  progPool ! Broadcast("msg to all")

  // PoisonPill and Kill are NOT routed
  // AddRoutee, Remove, Get handled only by the routing actor

}
