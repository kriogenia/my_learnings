package json

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import common.HttpApp
import json.GameAreaMap.Request

// 1. Add spray imports
import spray.json._

case class Player(username: String, role: String, level: Int)

object GameAreaMap {
  object Request {
    case object GetAllPlayers
    case class GetPlayer(username: String)
    case class GetPlayersByRole(role: String)
    case class AddPlayer(player: Player)
    case class RemovePlayer(username: String)
  }
  object Response {
     case object OperationSuccess
  }
}
class GameAreaMap extends Actor with ActorLogging {
  import GameAreaMap.Request._
  import GameAreaMap.Response._

  override def receive: Receive = onMessage(Map[String, Player]())

  private def onMessage(players: Map[String, Player]): Receive = {
    case GetAllPlayers =>
      log.info("Getting all players")
      sender() ! players.values.toList
    case GetPlayer(username) =>
      log.info(s"Getting player with username $username")
      sender() ! players.get(username)
    case GetPlayersByRole(role) =>
      log.info(s"Getting players with role $role")
      sender() ! players.values.filter(_.role == role).toList
    case AddPlayer(player) =>
      log.info(s"Adding player with username ${player.username}")
      sender() ! OperationSuccess
      context.become(onMessage(players + (player.username -> player)))
    case RemovePlayer(username) =>
      log.info(s"Removing player with username ${username}")
      sender() ! OperationSuccess
      context.become(onMessage(players - username))
  }
}

// 2. Create JSON protocol
trait PlayerJsonProtocol extends DefaultJsonProtocol {
  implicit val playerFormat = jsonFormat3(Player)
}

// 3. Add to the context the protocol and JSON support traits
object MarshallingJson extends HttpApp with PlayerJsonProtocol with SprayJsonSupport {

  private val gameMap = actorSystem.actorOf(Props[GameAreaMap], "game_map")
  List(
    Player("anduin", "priest", 10),
    Player("gromash", "warrior", 12),
    Player("thrall", "shaman", 13),
    Player("voljin", "priest", 9)
  ).foreach(gameMap ! Request.AddPlayer(_))

  /*
    GET   /api/player -> Returns all players
    GET   /api/player/{username} -> Returns the player with this username
    GET   /api/player?role={role} -> Returns all players with the role
    POST  /api/player -> Inserts the provided player
    DELETE /api/player/{username} -> Deletes the specified player
   */

  import actorSystem.dispatcher
  private val gameRouteSkeleton =  pathPrefix("api" / "player") {
    get {
      path(Segment) { username =>
        complete((gameMap ? Request.GetPlayer(username)).mapTo[Option[Player]])
      } ~
      parameter(Symbol("role").as[String]) { role =>
        complete((gameMap ? Request.GetPlayersByRole(role)).mapTo[List[Player]])
      } ~
      pathEndOrSingleSlash {
        complete((gameMap ? Request.GetAllPlayers).mapTo[List[Player]])
      }
    } ~
    post {
      entity(as[Player]) { player =>
        complete((gameMap ? Request.AddPlayer(player)).map(_ => StatusCodes.OK))
      }
    } ~
    (delete & path(Segment)) { username =>
        complete((gameMap ? Request.RemovePlayer(username)).map(_ => StatusCodes.OK))
    }
  }

  Http().newServerAt(host, defaultPort).bind(gameRouteSkeleton)

}
