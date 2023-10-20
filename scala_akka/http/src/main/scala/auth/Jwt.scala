package auth

import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import common.HttpApp
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtSprayJson}
import spray.json._

import java.util.concurrent.TimeUnit
import scala.util.{Failure, Success}

object SecurityDomain extends DefaultJsonProtocol {
  case class LoginRequest(username: String, password: String)
  implicit val loginRequestFormat: RootJsonFormat[LoginRequest] = jsonFormat2(LoginRequest)
}

object Jwt extends HttpApp with SprayJsonSupport {
  import SecurityDomain._

  private val userDb = Map(
    "admin" -> "admin",
    "user" -> "1234"
  )

  private def checkPassword(username: String, password: String): Boolean = userDb.get(username).contains(password)

  private val algorithm = JwtAlgorithm.HS256
  private val secretKey = "secret"
  private def createToken(username: String, expirationInDays: Int): String = {
    val claims = JwtClaim(
      expiration = Some(System.currentTimeMillis() / 1000 + TimeUnit.DAYS.toSeconds(expirationInDays)),
      issuedAt = Some(System.currentTimeMillis() / 1000),
      issuer = Some("sotoestevez.dev"),
      content = username,
    )
    JwtSprayJson.encode(claims, secretKey, algorithm)
  }

  private def isTokenExpired(token: String): Boolean = JwtSprayJson.decode(token, secretKey, Seq(algorithm)) match {
    case Success(claims) => claims.expiration.getOrElse(0L) < System.currentTimeMillis() / 1000
    case Failure(_) => true
  }

  private def isTokenValid(token: String): Boolean = JwtSprayJson.isValid(token, secretKey, Seq(algorithm))

  private val loginRoute = post {
    entity(as[LoginRequest]){
      case LoginRequest(username, password) if checkPassword(username, password) =>
        val token = createToken(username, 1)
        respondWithHeader(RawHeader("Access-Token", token)) {
          complete(StatusCodes.OK)
        }
      case _ =>  complete(StatusCodes.Unauthorized)
    }
  }

  private val authenticatedRoute = (path("secure") & get) {
    optionalHeaderValueByName("Authorization") {
      case Some(token) if isTokenValid(token) =>
        if (isTokenExpired(token)) {
          complete(HttpResponse(
            status = StatusCodes.Unauthorized,
            entity = "Token expired"
          ))
        } else {
          complete("Secure content")
        }
      case Some(_) => {
          complete(HttpResponse(
            status = StatusCodes.Unauthorized,
            entity = "Invalid token"
          ))
        }
      case _ =>
        complete(HttpResponse(
          status = StatusCodes.Unauthorized,
          entity = "Missing token "
        ))
    }
  }

  private val route = loginRoute ~ authenticatedRoute
  Http().newServerAt(host, defaultPort).bind(route)

}
