package com.pedidos360.product.messaging.producer;

import com.pedidos360.product.messaging.dto.StockLowMessage;
import com.pedidos360.product.messaging.validation.MessageValidator;
import com.pedidos360.product.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
public class PublishStockLow {

    private static final Logger log = LoggerFactory.getLogger(PublishStockLow.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;
    private final MessageValidator validator;
    private final String topic;

    public PublishStockLow(KafkaTemplate<String, String> kafkaTemplate, JsonMapper jsonMapper,
                           MessageValidator validator,
                           @Value("${app.kafka.topics.stock-low}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
        this.validator = validator;
        this.topic = topic;
    }

    public void execute(Product product, int threshold) {
        StockLowMessage message = new StockLowMessage(
                product.getId(), product.getCode(), product.getName(), product.getStock(), threshold);
        try {
            validator.validate(message);
            String json = jsonMapper.writeValueAsString(message);
            // key = productId: los eventos del mismo producto caen en la misma partición
            kafkaTemplate.send(topic, message.productId(), json).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("No se pudo publicar stock.low productId={}", message.productId(), ex);
                } else {
                    log.info("stock.low publicado productId={} stock={} partición={}",
                            message.productId(), message.stock(), result.getRecordMetadata().partition());
                }
            });
        } catch (RuntimeException e) {
            log.error("No se pudo armar stock.low para productId={}", product.getId(), e);
        }
    }
}