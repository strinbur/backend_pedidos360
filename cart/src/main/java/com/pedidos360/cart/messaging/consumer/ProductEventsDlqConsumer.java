package com.pedidos360.cart.messaging.consumer;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

@Component
public class ProductEventsDlqConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProductEventsDlqConsumer.class);

    @RabbitListener(queues = "${app.rabbit.queues.product-events-dlq}", containerFactory = "rawListenerFactory")
    public void onDeadLetter(Message message, Channel channel,
                             @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        log.warn("[DLQ] fecha={} cola-origen={} motivo={} mensaje={}",
                Instant.now(),
                headers.get("x-first-death-queue"),
                headers.get("x-first-death-reason"),
                body);

        channel.basicAck(tag, false);
    }
}