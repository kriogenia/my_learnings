package infrastructure

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.dispatch.{ControlMessage, PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.{Config, ConfigFactory}
import common.SimpleLoggingActor

object Mailboxes extends App {

  private val system = ActorSystem("mailbox_demo", ConfigFactory.load().getConfig("mailboxes"))

  /**
   * Case 01: custom priority mailbox
   *  P0 -> most important, P1, P2...
   */
  class SupportTicketPriorityMailbox(settings: ActorSystem.Settings, config: Config)
    extends UnboundedPriorityMailbox(
      PriorityGenerator {
        case message: String if message(2).isDigit => message(2).toInt - 48
        case _ => 10
      }
    )

  private val supportTicketLogger = system.actorOf(
    Props[SimpleLoggingActor].withDispatcher("support-ticket-dispatcher"), "support-ticker-logger")
  supportTicketLogger ! "no prio"
  supportTicketLogger ! PoisonPill        // will be low prio
  supportTicketLogger ! "[P3] low prio"
  supportTicketLogger ! "[P2] medium prio"
  supportTicketLogger ! "[P0] urgent"
  supportTicketLogger ! "[P1] top prio"
  supportTicketLogger ! "late"

  /**
   * Case 02: controlled-aware mailbox
   *  UnboundedControlAwareMailbox
   */
  private case object ManagementTicket extends ControlMessage // marked as important

  private val controlAware = system.actorOf(Props[SimpleLoggingActor].withMailbox("control-mailbox"), "control-aware")
  controlAware ! "no important"
  controlAware ! "[P0] lol, no"
  controlAware ! ManagementTicket

  private val confControlAware = system.actorOf(Props[SimpleLoggingActor], "conf-control-aware")
  confControlAware ! "from conf"
  confControlAware ! ManagementTicket

}
