package com.pedidos360.product.usecase;

import com.pedidos360.product.model.SaleRecord;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class SaleRecordWriter {

    private final MongoTemplate mongo;

    public SaleRecordWriter(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    /** Llega orders.checkout: crea la venta como PENDIENTE solo si no existe (no pisa un resultado ya resuelto). */
    public void recordPending(String cartId, String productId, String name,
                              int quantity, double subtotal, String occurredAt) {
        Update u = new Update()
                .setOnInsert("cartId", cartId)
                .setOnInsert("productId", productId)
                .setOnInsert("productName", name)
                .setOnInsert("quantity", quantity)
                .setOnInsert("subtotal", subtotal)
                .setOnInsert("occurredAt", occurredAt)
                .setOnInsert("status", "PENDIENTE");
        mongo.upsert(byId(cartId, productId), u, SaleRecord.class);
    }

    /** Resultado del descuento de stock: fija el estado final, exista o no ya el registro. */
    public void resolve(String cartId, String productId, int quantity, String name,
                        double subtotal, String status, String occurredAt) {
        Update u = new Update()
                .set("status", status)
                .setOnInsert("cartId", cartId)
                .setOnInsert("productId", productId)
                .setOnInsert("quantity", quantity)
                .setOnInsert("occurredAt", occurredAt);
        if (name != null) {
            u.set("productName", name).set("subtotal", subtotal);
        } else {
            u.setOnInsert("subtotal", 0.0);
        }
        mongo.upsert(byId(cartId, productId), u, SaleRecord.class);
    }

    private Query byId(String cartId, String productId) {
        return new Query(Criteria.where("_id").is(cartId + ":" + productId));
    }
}