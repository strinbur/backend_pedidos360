package com.pedidos360.product.messaging.producer;

import com.pedidos360.product.messaging.dto.OrderResultMessage;
import com.pedidos360.product.messaging.validation.MessageValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
public class PublishOrderResult {

    private static final Logger log = LoggerFactory.getLogger(PublishOrderResult.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;
    private final MessageValidator validator;
    private final String topic;

    public PublishOrderResult(KafkaTemplate<String, String> kafkaTemplate, JsonMapper jsonMapper,
                              MessageValidator validator,
                              @Value("${app.kafka.topics.orders-result}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
        this.validator = validator;
        this.topic = topic;
    }

    public void execute(OrderResultMessage message) {
        try {
            validator.validate(message);
            String json = jsonMapper.writeValueAsString(message);
            kafkaTemplate.send(topic, message.cartId(), json).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("No se pudo publicar orders.result cartId={}", message.cartId(), ex);
                } else {
                    log.info("orders.result publicado cartId={} status={} partición={}",
                            message.cartId(), message.status(), result.getRecordMetadata().partition());
                }
            });
        } catch (RuntimeException e) {
            // El stock ya se resolvió: un fallo al avisar no debe mandar el checkout a la DLQ
            log.error("No se pudo armar orders.result para cartId={}", message.cartId(), e);
        }
    }
}