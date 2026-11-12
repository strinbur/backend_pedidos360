package com.pedidos360.product.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "sales_history")
public class SaleRecord {
    @Id
    private String id;          // cartId:productId (evita duplicados si Kafka reentrega)
    private String cartId;
    private String productId;
    private String productName;
    private int quantity;
    private double subtotal;
    private String occurredAt;
}