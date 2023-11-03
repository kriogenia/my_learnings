package common

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import example.LowLevelGuitarStoreApi.actorSystem

object GuitarDB {
  case class CreateGuitar(guitar: Guitar)
  case class GuitarCreated(id: Int)
  case class FindGuitar(id: Int)
  case object FindAllGuitars

  def init(system: ActorSystem): ActorRef = {
    val guitarDb = actorSystem.actorOf(Props[GuitarDB], "guitar_db")
    List(
      Guitar("Fendor", "Stratocaster"),
      Guitar("Gibson", "Les Paul"),
      Guitar("Martin", "LX1")
    ).foreach(guitar => guitarDb ! GuitarDB.CreateGuitar(guitar))
    guitarDb
  }
}

class GuitarDB extends Actor with ActorLogging {

  import GuitarDB._

  override def receive: Receive = onMessage(Map(), 0)

  private def onMessage(guitars: Map[Int, Guitar], currentGuitarId: Int): Receive = {
    case FindAllGuitars =>
      log.info("Searching for all guitars...")
      sender() ! guitars.values.toList
    case FindGuitar(id) =>
      log.info(s"Searching guitar by id: $id...")
      sender() ! guitars.get(id)
    case CreateGuitar(guitar) =>
      log.info(s"Adding guitar $guitar with id $currentGuitarId")
      context.become(onMessage(guitars + (currentGuitarId -> guitar), currentGuitarId + 1))
      sender() ! GuitarCreated(currentGuitarId)
  }
}
