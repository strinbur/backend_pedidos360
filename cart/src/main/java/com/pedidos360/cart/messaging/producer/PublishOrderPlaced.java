package com.pedidos360.cart.messaging.producer;

import com.pedidos360.cart.messaging.dto.OrderPlacedMessage;
import com.pedidos360.cart.messaging.validation.MessageValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
public class PublishOrderPlaced {

    private static final Logger log = LoggerFactory.getLogger(PublishOrderPlaced.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;
    private final MessageValidator validator;
    private final String topic;

    public PublishOrderPlaced(KafkaTemplate<String, String> kafkaTemplate, JsonMapper jsonMapper,
                              MessageValidator validator,
                              @Value("${app.kafka.topics.orders-checkout}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
        this.validator = validator;
        this.topic = topic;
    }

    public void execute(OrderPlacedMessage message) {
        try {
            validator.validate(message);
            String json = jsonMapper.writeValueAsString(message);
            kafkaTemplate.send(topic, message.cartId(), json).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("No se pudo publicar orders.checkout cartId={}", message.cartId(), ex);
                } else {
                    log.info("orders.checkout publicado cartId={} partición={}",
                            message.cartId(), result.getRecordMetadata().partition());
                }
            });
        } catch (RuntimeException e) {
            // El checkout ya se aceptó por RabbitMQ: el historial en Kafka no debe tumbarlo
            log.error("No se pudo armar orders.checkout para cartId={}", message.cartId(), e);
        }
    }
}