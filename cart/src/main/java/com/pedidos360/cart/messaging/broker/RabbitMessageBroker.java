package com.pedidos360.cart.messaging.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMessageBroker implements MessageBroker {

    private static final Logger log = LoggerFactory.getLogger(RabbitMessageBroker.class);
    private final RabbitTemplate rabbitTemplate;

    public RabbitMessageBroker(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(String exchange, String routingKey, Object message) {
        log.info("Publicando en exchange={} routingKey={}", exchange, routingKey);
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}