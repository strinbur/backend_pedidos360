package com.pedidos360.product.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productId, int requested) {
        super("Stock insuficiente para el producto " + productId + " (solicitado: " + requested + ")");
    }
}