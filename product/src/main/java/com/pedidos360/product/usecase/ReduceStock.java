package com.pedidos360.product.usecase;

import com.mongodb.client.result.UpdateResult;
import com.pedidos360.product.exception.InsufficientStockException;
import com.pedidos360.product.exception.ProductNotFoundException;
import com.pedidos360.product.messaging.producer.PublishStockLow;
import com.pedidos360.product.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReduceStock {

    private static final Logger log = LoggerFactory.getLogger(ReduceStock.class);

    public record StockReduction(String productId, int quantity) {}

    private final MongoTemplate mongoTemplate;
    private final PublishStockLow publishStockLow;
    private final int lowStockThreshold;

    public ReduceStock(MongoTemplate mongoTemplate, PublishStockLow publishStockLow,
                       @Value("${app.kafka.low-stock-threshold}") int lowStockThreshold) {
        this.mongoTemplate = mongoTemplate;
        this.publishStockLow = publishStockLow;
        this.lowStockThreshold = lowStockThreshold;
    }

    public void execute(List<StockReduction> reductions) {
        List<StockReduction> applied = new ArrayList<>();
        try {
            for (StockReduction r : reductions) {
                reduceOne(r);
                applied.add(r);
            }
        } catch (RuntimeException e) {
            rollback(applied);   // si un ítem falla, devuelve el stock de los anteriores
            throw e;
        }
        notifyLowStock(reductions);
    }

    private void reduceOne(StockReduction r) {
        Query query = new Query(Criteria.where("id").is(r.productId())
                .and("stock").gte(r.quantity()));
        Update update = new Update().inc("stock", -r.quantity());

        UpdateResult result = mongoTemplate.updateFirst(query, update, Product.class);

        if (result.getModifiedCount() == 0) {
            boolean exists = mongoTemplate.exists(
                    new Query(Criteria.where("id").is(r.productId())), Product.class);
            if (!exists) {
                throw new ProductNotFoundException("Producto no encontrado con id: " + r.productId());
            }
            throw new InsufficientStockException(r.productId(), r.quantity());
        }
    }

    private void rollback(List<StockReduction> applied) {
        for (StockReduction r : applied) {
            mongoTemplate.updateFirst(
                    new Query(Criteria.where("id").is(r.productId())),
                    new Update().inc("stock", r.quantity()),
                    Product.class);
        }
    }

    // El stock ya se descontó: un fallo aquí NO debe hacer fallar (ni mandar a la DLQ) el checkout
    private void notifyLowStock(List<StockReduction> reductions) {
        for (StockReduction r : reductions) {
            try {
                Product product = mongoTemplate.findById(r.productId(), Product.class);
                if (product != null && product.getStock() < lowStockThreshold) {
                    publishStockLow.execute(product, lowStockThreshold);
                }
            } catch (RuntimeException e) {
                log.error("No se pudo evaluar stock bajo de productId={}", r.productId(), e);
            }
        }
    }
}