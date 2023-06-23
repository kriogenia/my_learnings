package dev.sotoestevez.jms.demo.server;

import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.ActiveMQServers;
import org.springframework.stereotype.Component;

@Component
public class AMQServer {

    private final ActiveMQServer server;

    public AMQServer() throws Exception {
        var serverConf = new ConfigurationImpl()
                .setPersistenceEnabled(false)
                .setJournalDirectory("target/data/journal")
                .setSecurityEnabled(false)
                .addAcceptorConfiguration("invm", "vm://0");
        this.server = ActiveMQServers.newActiveMQServer(serverConf);
    }

    public void init() throws Exception {
        this.server.start();
    }

}
