package dev.sotoestevez.jms.demo.sender;

import dev.sotoestevez.jms.demo.config.JmsConfiguration;
import dev.sotoestevez.jms.demo.model.SimpleMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HelloSender {

    private final JmsTemplate jmsTemplate;

    @Scheduled(fixedRate = 2000)
    public void sendMessage() {
        System.out.println("< Sending message");
        SimpleMessage msg = new SimpleMessage(UUID.randomUUID(), "Hello World");
        jmsTemplate.convertAndSend(JmsConfiguration.QUEUE_NAME, msg);
        System.out.println("< Message sent");
    }

}
