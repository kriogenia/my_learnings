package dev.sotoestevez.jms.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
public class JmsConfiguration {

    public static final String UNIDIRECTIONAL_QUEUE = "unidirectional-queue";
    public static final String BIDIRECTIONAL_QUEUE = "bidirectional-queue";

    /**
    @Bean(initMethod = "init")
    public AMQServer activeMQServer() throws Exception {
        return new AMQServer();
    }
     */

    @Bean
    public MessageConverter messageConverter() {
        var converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

}
