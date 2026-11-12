package com.pedidos360.product.messaging.consumer;

import com.pedidos360.product.messaging.dto.OrderPlacedMessage;
import com.pedidos360.product.messaging.exception.InvalidMessageException;
import com.pedidos360.product.messaging.validation.MessageValidator;
import com.pedidos360.product.usecase.RecordSale;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class OrdersCheckoutConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrdersCheckoutConsumer.class);

    private final JsonMapper jsonMapper;
    private final MessageValidator validator;
    private final RecordSale recordSale;

    public OrdersCheckoutConsumer(JsonMapper jsonMapper, MessageValidator validator, RecordSale recordSale) {
        this.jsonMapper = jsonMapper;
        this.validator = validator;
        this.recordSale = recordSale;
    }

    @KafkaListener(topics = "${app.kafka.topics.orders-checkout}")
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            OrderPlacedMessage order = jsonMapper.readValue(record.value(), OrderPlacedMessage.class);
            validator.validate(order);
            recordSale.execute(order);
        } catch (InvalidMessageException | JacksonException e) {
            // mensaje irrecuperable: se descarta con log (reintentar no lo arreglaría)
            log.error("orders.checkout descartado partición={} offset={} motivo={}",
                    record.partition(), record.offset(), e.getMessage());
        }
        // Si recordSale falla (ej. Mongo caído) la excepción sube sin ACK y Kafka reintenta
        ack.acknowledge();
    }
}