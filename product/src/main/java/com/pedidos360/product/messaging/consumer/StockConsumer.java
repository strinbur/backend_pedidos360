package com.pedidos360.product.messaging.consumer;

import com.pedidos360.product.messaging.dto.CheckoutMessage;
import com.pedidos360.product.messaging.validation.MessageValidator;
import com.pedidos360.product.usecase.ReduceStock;
import com.pedidos360.product.usecase.ReduceStock.StockReduction;
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
import java.util.List;

@Component
public class StockConsumer {

    private static final Logger log = LoggerFactory.getLogger(StockConsumer.class);

    private final MessageConverter converter;
    private final MessageValidator validator;
    private final ReduceStock reduceStock;

    public StockConsumer(MessageConverter converter, MessageValidator validator, ReduceStock reduceStock) {
        this.converter = converter;
        this.validator = validator;
        this.reduceStock = reduceStock;
    }

    @RabbitListener(queues = "${app.rabbit.queues.stock}", containerFactory = "rawListenerFactory")
    public void onMessage(Message message, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            // 1. Deserializar
            message.getMessageProperties().setInferredArgumentType(CheckoutMessage.class);
            CheckoutMessage checkout = (CheckoutMessage) converter.fromMessage(message);

            // 2. Validar
            validator.validate(checkout);

            // 3. Caso de uso
            List<StockReduction> reductions = checkout.items().stream()
                    .map(i -> new StockReduction(i.productId(), i.quantity()))
                    .toList();
            reduceStock.execute(reductions);

            // 4. OK -> ACK
            channel.basicAck(tag, false);
            log.info("Checkout procesado cartId={}", checkout.cartId());

        } catch (Exception e) {
            // 5. ERROR -> log + NACK sin reencolar (va a la DLQ)
            log.error("Checkout rechazado, se envía a la DLQ. Motivo: {}", e.getMessage());
            channel.basicNack(tag, false, false);
        }
    }
}