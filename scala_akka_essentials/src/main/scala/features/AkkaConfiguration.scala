package features

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object AkkaConfiguration extends App {

  private case class SimpleLoggingActor() extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  // 1. Inline configuration
  private val configString =
    """
      | akka {
      |   loglevel = "DEBUG"
      | }
      |""".stripMargin
  private val parsedConfig = ConfigFactory.parseString(configString)

  private val inlineConfigSystem = ActorSystem("inline_config_demo", ConfigFactory.load(parsedConfig))
  private val inline = inlineConfigSystem.actorOf(Props[SimpleLoggingActor], "inline")
  inline ! "inline: " + parsedConfig.getString("akka.loglevel")

  // 2. Default configuration file
  private val defaultFileConfigSystem = ActorSystem("default_conf_file__demo")
  private val default = defaultFileConfigSystem.actorOf(Props[SimpleLoggingActor], "default")
  //default ! "default: " + ConfigFactory.load().getString("akka.loglevel")

  // 3. Namespaced configuration in the default configuration file
  private val namespaceConfig = ConfigFactory.load().getConfig("namespace")
  private val namespacedConfigSystem = ActorSystem("namespaced_conf_demo", namespaceConfig)
  private val namespaced = namespacedConfigSystem.actorOf(Props[SimpleLoggingActor], "namespaced")
  namespaced ! "namespace: " + namespaceConfig.getString("akka.loglevel")

  // 4. Different configuration file
  private val customFileConfig = ConfigFactory.load("custom/secret.conf")
  private val customFileConfigSystem = ActorSystem("custom_conf_demo", customFileConfig)
  private val custom = customFileConfigSystem.actorOf(Props[SimpleLoggingActor], "custom")
  custom ! "custom: " + customFileConfig.getString("akka.loglevel")

  // 5. Different file formats
  private val jsonFileConfig = ConfigFactory.load("json/config.json")
  private val jsonFileConfigSystem = ActorSystem("json_conf_demo", jsonFileConfig)
  private val json = jsonFileConfigSystem.actorOf(Props[SimpleLoggingActor], "json")
  json ! "json: " + jsonFileConfig.getString("akka.loglevel")

}
