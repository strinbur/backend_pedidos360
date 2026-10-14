package com.pedidos360.cart.messaging.consumer;

import com.pedidos360.cart.cache.CachedProduct;
import com.pedidos360.cart.messaging.dto.ProductChangedMessage;
import com.pedidos360.cart.messaging.validation.MessageValidator;
import com.pedidos360.cart.usecase.RemoveProductFromCache;
import com.pedidos360.cart.usecase.SyncProductInCache;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ProductEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProductEventsConsumer.class);

    private final MessageConverter converter;
    private final MessageValidator validator;
    private final SyncProductInCache syncProductInCache;
    private final RemoveProductFromCache removeProductFromCache;

    public ProductEventsConsumer(MessageConverter converter, MessageValidator validator,
                                 SyncProductInCache syncProductInCache,
                                 RemoveProductFromCache removeProductFromCache) {
        this.converter = converter;
        this.validator = validator;
        this.syncProductInCache = syncProductInCache;
        this.removeProductFromCache = removeProductFromCache;
    }

    @RabbitListener(queues = "${app.rabbit.queues.product-events}", containerFactory = "rawListenerFactory")
    public void onMessage(Message message, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            // 1. Deserializar
            message.getMessageProperties().setInferredArgumentType(ProductChangedMessage.class);
            ProductChangedMessage event = (ProductChangedMessage) converter.fromMessage(message);

            // 2. Validar
            validator.validate(event);

            // 3. Caso de uso según el tipo de evento
            switch (event.type()) {
                case UPDATED -> syncProductInCache.execute(new CachedProduct(
                        event.productId(), event.code(), event.name(),
                        event.price() == null ? -1 : event.price(), event.imageUrl()));
                case DELETED -> removeProductFromCache.execute(event.productId());
            }

            // 4. OK -> ACK
            channel.basicAck(tag, false);

        } catch (Exception e) {
            // 5. ERROR -> log + NACK sin reencolar (va a la DLQ)
            log.error("Evento de producto rechazado, se envía a la DLQ. Motivo: {}", e.getMessage());
            channel.basicNack(tag, false, false);
        }
    }
}