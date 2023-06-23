package dev.sotoestevez.jms.demo.listener;

import dev.sotoestevez.jms.demo.config.JmsConfiguration;
import dev.sotoestevez.jms.demo.model.SimpleMessage;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class SimpleMessageListener {

    @JmsListener(destination = JmsConfiguration.QUEUE_NAME)
    public void listen(@Payload SimpleMessage msg) {
        System.out.printf("> Message received: [%s] \"%s\"%n", msg.id(), msg.message());
    }

}
