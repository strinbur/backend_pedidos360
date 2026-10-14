package com.pedidos360.product.messaging.producer;

import com.pedidos360.product.messaging.broker.MessageBroker;
import com.pedidos360.product.messaging.config.RabbitProperties;
import com.pedidos360.product.messaging.dto.ProductChangeType;
import com.pedidos360.product.messaging.dto.ProductChangedMessage;
import com.pedidos360.product.messaging.validation.MessageValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.stereotype.Service;

@Service
public class PublishProductDeleted {

    private static final Logger log = LoggerFactory.getLogger(PublishProductDeleted.class);

    private final MessageBroker broker;
    private final RabbitProperties props;
    private final MessageValidator validator;

    public PublishProductDeleted(MessageBroker broker, RabbitProperties props, MessageValidator validator) {
        this.broker = broker;
        this.props = props;
        this.validator = validator;
    }

    public void execute(String productId) {
        ProductChangedMessage message = new ProductChangedMessage(
                productId, ProductChangeType.DELETED, null, null, null, null);

        validator.validate(message);

        try {
            broker.publish(props.exchanges().topic(), props.routingKeys().productDeleted(), message);
        } catch (AmqpException e) {
            log.error("No se pudo publicar product.deleted para productId={}", productId, e);
        }
    }
}