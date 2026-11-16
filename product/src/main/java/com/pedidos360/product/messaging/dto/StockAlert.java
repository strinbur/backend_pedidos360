package com.pedidos360.product.messaging.dto;

public record StockAlert(String productId, String code, String name,
                         int stock, int threshold, String level, boolean changed) {}