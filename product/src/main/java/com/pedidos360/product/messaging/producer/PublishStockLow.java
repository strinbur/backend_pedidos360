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
    private final int threshold;

    public PublishStockLow(KafkaTemplate<String, String> kafkaTemplate, JsonMapper jsonMapper,
                           MessageValidator validator,
                           @Value("${app.kafka.topics.stock-low}") String topic,
                           @Value("${app.kafka.low-stock-threshold}") int threshold) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
        this.validator = validator;
        this.topic = topic;
        this.threshold = threshold;
    }

    /** Publica si el stock está bajo el umbral, o si acaba de recuperarse por sobre él. */
    public void executeIfRelevant(Product product, int previousStock) {
        boolean below = product.getStock() < threshold;
        boolean recovered = !below && previousStock < threshold;
        if (!below && !recovered) {
            return;
        }
        String level = product.getStock() == 0 ? "OUT" : below ? "LOW" : "OK";

        StockLowMessage message = new StockLowMessage(
                product.getId(), product.getCode(), product.getName(),
                product.getStock(), threshold, level);
        try {
            validator.validate(message);
            String json = jsonMapper.writeValueAsString(message);
            // key = productId: los eventos del mismo producto caen en la misma partición
            kafkaTemplate.send(topic, message.productId(), json).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("No se pudo publicar stock.low productId={}", message.productId(), ex);
                } else {
                    log.info("stock.low publicado productId={} stock={} level={} partición={}",
                            message.productId(), message.stock(), message.level(),
                            result.getRecordMetadata().partition());
                }
            });
        } catch (RuntimeException e) {
            log.error("No se pudo armar stock.low para productId={}", product.getId(), e);
        }
    }
}