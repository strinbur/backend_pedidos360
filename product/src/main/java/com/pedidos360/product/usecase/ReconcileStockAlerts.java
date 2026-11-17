package com.pedidos360.product.usecase;

import com.pedidos360.product.messaging.producer.PublishStockLow;
import com.pedidos360.product.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReconcileStockAlerts implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ReconcileStockAlerts.class);

    private final MongoTemplate mongoTemplate;
    private final PublishStockLow publishStockLow;
    private final int threshold;

    public ReconcileStockAlerts(MongoTemplate mongoTemplate, PublishStockLow publishStockLow,
                                @Value("${app.kafka.low-stock-threshold}") int threshold) {
        this.mongoTemplate = mongoTemplate;
        this.publishStockLow = publishStockLow;
        this.threshold = threshold;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<Product> low = mongoTemplate.find(
                    new Query(Criteria.where("stock").lt(threshold)), Product.class);
            // previousStock = stock actual: como está bajo el umbral, siempre publica LOW u OUT
            low.forEach(p -> publishStockLow.executeIfRelevant(p, p.getStock()));
            log.info("Reconciliación de alertas: {} productos bajo el umbral", low.size());
        } catch (RuntimeException e) {
            log.error("No se pudo reconciliar las alertas de stock", e);
        }
    }
}