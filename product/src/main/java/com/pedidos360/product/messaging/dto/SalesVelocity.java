package com.pedidos360.product.messaging.dto;

public record SalesVelocity(String productId, long units, String windowStart, String windowEnd) {}