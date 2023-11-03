package patterns

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props}
import akka.pattern.{BackoffOpts, BackoffSupervisor}

import java.io.File
import scala.concurrent.duration.DurationInt
import scala.io.Source
import scala.language.postfixOps

object BackoffSupervisorPattern extends App {


  private case object Read
  private case class ReadFile(filename: String)
  private class FileBasedPersistentActor extends Actor with ActorLogging {
    override def receive: Receive = unplugged

    override def preStart(): Unit = log.info("Persistent actor started")
    override def postStop(): Unit = log.info("Persistent actor stopped")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = log.warning("Persistent actor restarting")

    private def unplugged: Receive = {
      case ReadFile(filename) =>
        val dataSource = Source.fromFile(new File(filename))
        context.become(plugged(dataSource))
        self forward Read
    }

    protected def plugged(source: Source): Receive = {
      case Read =>
        log.info("IMPORTANT data read: " + source.getLines().toList)

    }
  }

  private val system = ActorSystem("backoff_supervisor_demo")
  private val INVALID: String = "src/main/resources/test/invalid.txt"


  private val valid = system.actorOf(Props[FileBasedPersistentActor], "valid")
  valid ! ReadFile("src/main/resources/test/valid.txt")
  private val invalid = system.actorOf(Props[FileBasedPersistentActor], "invalid")
  invalid ! ReadFile(INVALID)


  /**
   * onFailureSupervisor
   * - spawns child on-failure_file_actor of type FileBasedPersistentActor
   * - supervision strategy by default (restart on everything)
   *   - first attempt at 3s mark
   *   - next attempt at 2x the time of the previous attempt: 3s -> 6s -> 12s -> 24s (max 30s)
   */
  private val onFailureSupervisorProps = BackoffSupervisor.props(
    BackoffOpts.onFailure(
      Props[FileBasedPersistentActor], "on-failure_file_actor", 3 seconds, 30 seconds, 0.2
    )
  )
  private val onFailureSupervisor = system.actorOf(onFailureSupervisorProps, "on-failure_supervisor")
  onFailureSupervisor ! ReadFile(INVALID)


  private val onStopSupervisorProps = BackoffSupervisor.props(
    BackoffOpts.onStop(
      Props[FileBasedPersistentActor], "on-stop_file_actor", 3 seconds, 30 seconds, 0.2
    ).withSupervisorStrategy(
      OneForOneStrategy() {
        case _ => Stop
      }
    )
  )
  private val onStopSupervisor = system.actorOf(onStopSupervisorProps, "on-stop_supervisor")
  onStopSupervisor ! ReadFile(INVALID)


  private class EagerFileBasedPersistentActor(filename: String) extends FileBasedPersistentActor {
    override def preStart(): Unit = {
      log.info("Eager actor starting")
      val datasource = Source.fromFile(new File(filename))
      context.become(plugged(datasource))
    }
  }

  /**
   * repeatedSupervisor
   * - child eagerActor of type EagerFileBasedPersistentActor reading invalid file
   *   - will die on start with ActorInitializationException
   *   - trigger the supervision strategy in repeatedSupervisor
   * - backoff will kick in after 1s, 2s, 4s, 8s, 16s
   */
  private val repeatedSupervisorProps = BackoffSupervisor.props(
    BackoffOpts.onStop(
      Props(new EagerFileBasedPersistentActor(INVALID)), "eager_actor", 1 second, 30 seconds, 0.1
    )
  )
  private val _ = system.actorOf(repeatedSupervisorProps, "repeated_supervisor")

  // if valid.txt file is renamed to invalid.txt during the restarting attempts, it will start working

}
