package dev.sotoestevez.jms.demo.config;

import dev.sotoestevez.jms.demo.server.AMQServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JmsConfiguration {

    @Bean(initMethod = "init")
    public AMQServer activeMQServer() throws Exception {
        return new AMQServer();
    }

}
