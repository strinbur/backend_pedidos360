package com.pedidos360.rabbitadmin.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.stereotype.Service;

@Service
public class RabbitBrokerAdminService implements BrokerAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitBrokerAdminService.class);

    private final AmqpAdmin amqpAdmin;

    public RabbitBrokerAdminService(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    @Override
    public void createQueue(String name, String deadLetterExchange, String deadLetterRoutingKey) {
        QueueBuilder builder = QueueBuilder.durable(name).quorum();
        if (deadLetterExchange != null && !deadLetterExchange.isBlank()) {
            builder.deadLetterExchange(deadLetterExchange);
        }
        if (deadLetterRoutingKey != null && !deadLetterRoutingKey.isBlank()) {
            builder.deadLetterRoutingKey(deadLetterRoutingKey);
        }
        amqpAdmin.declareQueue(builder.build());
        log.info("Cola creada name={}", name);
    }

    @Override
    public void deleteQueue(String name) {
        amqpAdmin.deleteQueue(name);
        log.info("Cola eliminada name={}", name);
    }

    @Override
    public void createExchange(String name, String type) {
        Exchange exchange = switch (type) {
            case "direct" -> new DirectExchange(name, true, false);
            case "topic" -> new TopicExchange(name, true, false);
            case "fanout" -> new FanoutExchange(name, true, false);
            case "headers" -> new HeadersExchange(name, true, false);
            default -> throw new IllegalArgumentException("Tipo de exchange no soportado: " + type);
        };
        amqpAdmin.declareExchange(exchange);
        log.info("Exchange creado name={} type={}", name, type);
    }

    @Override
    public void deleteExchange(String name) {
        amqpAdmin.deleteExchange(name);
        log.info("Exchange eliminado name={}", name);
    }

    @Override
    public void createBinding(String exchange, String queue, String routingKey) {
        amqpAdmin.declareBinding(buildBinding(exchange, queue, routingKey));
        log.info("Binding creado exchange={} queue={} routingKey={}", exchange, queue, routingKey);
    }

    @Override
    public void deleteBinding(String exchange, String queue, String routingKey) {
        amqpAdmin.removeBinding(buildBinding(exchange, queue, routingKey));
        log.info("Binding eliminado exchange={} queue={} routingKey={}", exchange, queue, routingKey);
    }

    private Binding buildBinding(String exchange, String queue, String routingKey) {
        return new Binding(queue, Binding.DestinationType.QUEUE, exchange, routingKey, null);
    }
}