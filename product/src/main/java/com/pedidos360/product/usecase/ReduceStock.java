package com.pedidos360.product.usecase;

import com.mongodb.client.result.UpdateResult;
import com.pedidos360.product.exception.InsufficientStockException;
import com.pedidos360.product.exception.ProductNotFoundException;
import com.pedidos360.product.messaging.producer.PublishStockLow;
import com.pedidos360.product.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public record SoldItem(String productId, String name, int quantity, double subtotal) {}

    private final MongoTemplate mongoTemplate;
    private final PublishStockLow publishStockLow;

    public ReduceStock(MongoTemplate mongoTemplate, PublishStockLow publishStockLow) {
        this.mongoTemplate = mongoTemplate;
        this.publishStockLow = publishStockLow;
    }

    public List<SoldItem> execute(List<StockReduction> reductions) {
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
        return afterReduce(reductions);
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

    // El stock ya se descontó: un fallo aquí NO debe hacer fallar el checkout
    private List<SoldItem> afterReduce(List<StockReduction> reductions) {
        List<SoldItem> sold = new ArrayList<>();
        for (StockReduction r : reductions) {
            Product product = null;
            try {
                product = mongoTemplate.findById(r.productId(), Product.class);
                if (product != null) {
                    publishStockLow.executeIfRelevant(product, product.getStock() + r.quantity());
                }
            } catch (RuntimeException e) {
                log.error("No se pudo evaluar stock bajo de productId={}", r.productId(), e);
            }
            sold.add(new SoldItem(r.productId(),
                    product == null ? null : product.getName(),
                    r.quantity(),
                    product == null ? 0 : product.getPrice() * r.quantity()));
        }
        return sold;
    }
}