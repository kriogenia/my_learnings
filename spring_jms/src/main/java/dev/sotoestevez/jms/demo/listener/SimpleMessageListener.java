package dev.sotoestevez.jms.demo.listener;

import dev.sotoestevez.jms.demo.config.JmsConfiguration;
import dev.sotoestevez.jms.demo.model.SimpleMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SimpleMessageListener {

    private final JmsTemplate template;

    @JmsListener(destination = JmsConfiguration.UNIDIRECTIONAL_QUEUE)
    public void listen(@Payload SimpleMessage msg) {
        System.out.printf("> Message received: [%s] \"%s\"%n", msg.id(), msg.message());
    }

    @JmsListener(destination = JmsConfiguration.BIDIRECTIONAL_QUEUE)
    public void listenAndReply(@Payload SimpleMessage msg, Message message) throws JMSException {
        System.out.printf("> Message received: [%s] \"%s\"%n", msg.id(), msg.message());
        var reply = new SimpleMessage(UUID.randomUUID(), "World!");
        template.convertAndSend(message.getJMSReplyTo(), reply);
    }

}
