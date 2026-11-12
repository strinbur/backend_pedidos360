package com.pedidos360.cart.messaging.consumer;

import com.pedidos360.cart.messaging.dto.StockLowMessage;
import com.pedidos360.cart.messaging.exception.InvalidMessageException;
import com.pedidos360.cart.messaging.validation.MessageValidator;
import com.pedidos360.cart.usecase.RegisterLowStock;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class StockLowConsumer {

    private static final Logger log = LoggerFactory.getLogger(StockLowConsumer.class);

    private final JsonMapper jsonMapper;
    private final MessageValidator validator;
    private final RegisterLowStock registerLowStock;

    public StockLowConsumer(JsonMapper jsonMapper, MessageValidator validator, RegisterLowStock registerLowStock) {
        this.jsonMapper = jsonMapper;
        this.validator = validator;
        this.registerLowStock = registerLowStock;
    }

    @KafkaListener(topics = "${app.kafka.topics.stock-low}")
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            StockLowMessage event = jsonMapper.readValue(record.value(), StockLowMessage.class);
            validator.validate(event);
            registerLowStock.execute(event);
        } catch (InvalidMessageException | JacksonException e) {
            log.error("stock.low descartado partición={} offset={} motivo={}",
                    record.partition(), record.offset(), e.getMessage());
        }
        ack.acknowledge();
    }
}