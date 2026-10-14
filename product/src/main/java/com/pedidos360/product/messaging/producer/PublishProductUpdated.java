package com.pedidos360.product.messaging.producer;

import com.pedidos360.product.messaging.broker.MessageBroker;
import com.pedidos360.product.messaging.config.RabbitProperties;
import com.pedidos360.product.messaging.dto.ProductChangeType;
import com.pedidos360.product.messaging.dto.ProductChangedMessage;
import com.pedidos360.product.messaging.validation.MessageValidator;
import com.pedidos360.product.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.stereotype.Service;

@Service
public class PublishProductUpdated {

    private static final Logger log = LoggerFactory.getLogger(PublishProductUpdated.class);

    private final MessageBroker broker;
    private final RabbitProperties props;
    private final MessageValidator validator;

    public PublishProductUpdated(MessageBroker broker, RabbitProperties props, MessageValidator validator) {
        this.broker = broker;
        this.props = props;
        this.validator = validator;
    }

    public void execute(Product product) {
        ProductChangedMessage message = new ProductChangedMessage(
                product.getId(), ProductChangeType.UPDATED,
                product.getCode(), product.getName(), product.getPrice(), product.getImageUrl());

        validator.validate(message);

        try {
            broker.publish(props.exchanges().topic(), props.routingKeys().productUpdated(), message);
        } catch (AmqpException e) {
            log.error("No se pudo publicar product.updated para productId={}", product.getId(), e);
        }
    }
}