package com.pedidos360.cart.messaging.consumer;

import com.pedidos360.cart.messaging.dto.OrderResultMessage;
import com.pedidos360.cart.messaging.exception.InvalidMessageException;
import com.pedidos360.cart.messaging.validation.MessageValidator;
import com.pedidos360.cart.usecase.RegisterOrderResult;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class OrderResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderResultConsumer.class);

    private final JsonMapper jsonMapper;
    private final MessageValidator validator;
    private final RegisterOrderResult registerOrderResult;

    public OrderResultConsumer(JsonMapper jsonMapper, MessageValidator validator,
                               RegisterOrderResult registerOrderResult) {
        this.jsonMapper = jsonMapper;
        this.validator = validator;
        this.registerOrderResult = registerOrderResult;
    }

    @KafkaListener(topics = "${app.kafka.topics.orders-result}")
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            OrderResultMessage event = jsonMapper.readValue(record.value(), OrderResultMessage.class);
            validator.validate(event);
            registerOrderResult.execute(event);
        } catch (InvalidMessageException | JacksonException e) {
            log.error("orders.result descartado partición={} offset={} motivo={}",
                    record.partition(), record.offset(), e.getMessage());
        }
        ack.acknowledge();
    }
}