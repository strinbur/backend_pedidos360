package com.pedidos360.product.messaging.consumer;

import com.pedidos360.product.exception.InsufficientStockException;
import com.pedidos360.product.exception.ProductNotFoundException;
import com.pedidos360.product.messaging.dto.CheckoutMessage;
import com.pedidos360.product.messaging.validation.MessageValidator;
import com.pedidos360.product.usecase.ReduceStock;
import com.pedidos360.product.usecase.ReduceStock.SoldItem;
import com.pedidos360.product.usecase.ReduceStock.StockReduction;
import com.pedidos360.product.usecase.ResolveOrder;
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
    private final ResolveOrder resolveOrder;

    public StockConsumer(MessageConverter converter, MessageValidator validator,
                         ReduceStock reduceStock, ResolveOrder resolveOrder) {
        this.converter = converter;
        this.validator = validator;
        this.reduceStock = reduceStock;
        this.resolveOrder = resolveOrder;
    }

    @RabbitListener(queues = "${app.rabbit.queues.stock}", containerFactory = "rawListenerFactory")
    public void onMessage(Message message, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        CheckoutMessage checkout = null;
        try {
            message.getMessageProperties().setInferredArgumentType(CheckoutMessage.class);
            checkout = (CheckoutMessage) converter.fromMessage(message);
            validator.validate(checkout);

            List<StockReduction> reductions = checkout.items().stream()
                    .map(i -> new StockReduction(i.productId(), i.quantity()))
                    .toList();
            List<SoldItem> sold = reduceStock.execute(reductions);

            resolveOrder.confirm(checkout, sold);          // CONFIRMADA + orders.result
            channel.basicAck(tag, false);
            log.info("Checkout procesado cartId={}", checkout.cartId());

        } catch (Exception e) {
            log.error("Checkout rechazado, se envía a la DLQ. Motivo: {}", e.getMessage());
            // Solo los rechazos de negocio se avisan; un mensaje ilegible no tiene cartId confiable
            if (checkout != null
                    && (e instanceof InsufficientStockException || e instanceof ProductNotFoundException)) {
                resolveOrder.reject(checkout, e.getMessage());   // RECHAZADA + orders.result
            }
            channel.basicNack(tag, false, false);
        }
    }
}