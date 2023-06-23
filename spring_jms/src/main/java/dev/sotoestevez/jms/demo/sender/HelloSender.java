package dev.sotoestevez.jms.demo.sender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sotoestevez.jms.demo.config.JmsConfiguration;
import dev.sotoestevez.jms.demo.model.SimpleMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HelloSender {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 2000)
    public void sendMessage() {
        SimpleMessage msg = new SimpleMessage(UUID.randomUUID(), "Hello World");
        jmsTemplate.convertAndSend(JmsConfiguration.UNIDIRECTIONAL_QUEUE, msg);
    }

    @Scheduled(fixedRate = 2000)
    public void sendAndReceiveMessage() throws JMSException {
        SimpleMessage msg = new SimpleMessage(UUID.randomUUID(), "Hello");
        var received = jmsTemplate.sendAndReceive(JmsConfiguration.BIDIRECTIONAL_QUEUE, session -> {
            var helloMessage = session.createTextMessage(serialize(msg));
            helloMessage.setStringProperty("_type", SimpleMessage.class.getName());
            return helloMessage;
        });

        if (received != null) {
            var msgReceived = readMessage(received);
            System.out.printf("> Reply received: [%s] \"%s\"%n", msgReceived.id(), msgReceived.message());
        }

    }

    private String serialize(SimpleMessage message) throws JMSException {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new JMSException("Unable to serialize message");
        }
    }

    private SimpleMessage readMessage(Message msg) throws JMSException {
        var body = msg.getBody(String.class);
        try {
            return objectMapper.readValue(body, SimpleMessage.class);
        } catch (JsonProcessingException e) {
            throw new JMSException("Unable to deserialize message");
        }
    }

}
