package com.pedidos360.cart.messaging.producer;

import com.pedidos360.cart.messaging.broker.MessageBroker;
import com.pedidos360.cart.messaging.config.RabbitProperties;
import com.pedidos360.cart.messaging.dto.CheckoutMessage;
import com.pedidos360.cart.messaging.validation.MessageValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PublishCheckout {

    private static final Logger log = LoggerFactory.getLogger(PublishCheckout.class);

    private final MessageBroker broker;
    private final RabbitProperties props;
    private final MessageValidator validator;

    public PublishCheckout(MessageBroker broker, RabbitProperties props, MessageValidator validator) {
        this.broker = broker;
        this.props = props;
        this.validator = validator;
    }

    public void execute(CheckoutMessage message) {
        validator.validate(message);
        // Si RabbitMQ falla, la excepción sube: el checkout NO debe darse por exitoso
        broker.publish(props.exchanges().direct(), props.routingKeys().stockReduce(), message);
        log.info("Checkout publicado cartId={}", message.cartId());
    }
}