package patterns

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success}

class AskPatternSpec extends TestKit(ActorSystem("AskSpec"))
  with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll
{

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import AskPatternSpec._

  "An authenticator" should {
    authenticatorTestSuite(Props[AuthManager])
  }

  "A piped authenticator" should {
    authenticatorTestSuite(Props[PipedAuthManager])
  }

  private def authenticatorTestSuite(props: Props): Unit = {
    import AuthManager._

    "fail to authenticate a non-registered user" in {
      val authManager = system.actorOf(props)
      authManager ! Authenticate("test", "pass")

      expectMsg(AuthFailure(AUTH_FAILURE_NOT_FOUND))
    }

    "fail to authenticate with invalid password" in {
      val authManager = system.actorOf(props)
      authManager ! RegisterUser("test", "pass")
      authManager ! Authenticate("test", "wrong")

      expectMsg(AuthFailure(AUTH_FAILURE_WRONG_PASSWORD))
    }

    "successfully authenticate with correct credentials" in {
      val authManager = system.actorOf(props)
      authManager ! RegisterUser("test", "pass")
      authManager ! Authenticate("test", "pass")

      expectMsg(AuthSuccess)
    }
  }

}

object AskPatternSpec {

  private object KVActor {
    final case class Read(key: String)
    final case class Write(key: String, value: String)
  }
  private class KVActor extends Actor with ActorLogging {
    import KVActor._

    override def receive: Receive = online(Map())

    private def online(kv: Map[String, String]): Receive = {
      case Read(k) =>
        log.info(s"Trying to read the value stored with key $k")
        sender() ! kv.get(k)
      case Write(k, v) =>
        log.info(s"Writing the value $v for key $k")
        context.become(online(kv + (k -> v)))
    }
  }

  private object AuthManager {
    final case class RegisterUser(username: String, password: String)
    final case class Authenticate(username: String, password: String)

    final case class AuthFailure(message: String)
    final case object AuthSuccess

    val AUTH_FAILURE_NOT_FOUND = "user not found"
    val AUTH_FAILURE_WRONG_PASSWORD = "wrong password"
    val AUTH_FAILURE_INTERNAL = "internal server error"
  }
  private class AuthManager extends Actor with ActorLogging {
    import AuthManager._

    implicit val timeout: Timeout = Timeout(1 second)
    implicit val executionContext: ExecutionContext = context.dispatcher

    protected val authDb: ActorRef = context.actorOf(Props[KVActor])

    override def receive: Receive = {
      case RegisterUser(username, password) => authDb ! KVActor.Write(username, password)
      case Authenticate(username, password) => handleAuthentication(username, password)
    }

    protected def handleAuthentication(username: String, password: String): Unit = {
      // WARNING: never call methods on the actor instance or access mutable state in onComplete
      val originalSender = sender()

      (authDb ? KVActor.Read(username)).onComplete {
        case Success(None) => originalSender ! AuthFailure(AUTH_FAILURE_NOT_FOUND)
        case Success(Some(dbPassword)) if dbPassword != password =>
          originalSender ! AuthFailure(AUTH_FAILURE_WRONG_PASSWORD)
        case Success(_) => originalSender ! AuthSuccess
        case Failure(_) => originalSender ! AuthFailure(AUTH_FAILURE_INTERNAL)
      }
    }

  }

  // Prefer the piped approach
  private class PipedAuthManager extends AuthManager {
    import AuthManager._
    override def handleAuthentication(username: String, password: String): Unit = {
      (authDb ? KVActor.Read(username)).mapTo[Option[String]].map {
        case None => AuthFailure(AUTH_FAILURE_NOT_FOUND)
        case Some(dbPassword) if dbPassword != password => AuthFailure(AUTH_FAILURE_WRONG_PASSWORD)
        case Some(_) => AuthSuccess
      }.pipeTo(sender())
    }
  }

  // TODO attempt behavior approach

}
