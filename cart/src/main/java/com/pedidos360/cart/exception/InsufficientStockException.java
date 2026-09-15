package com.pedidos360.cart.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productName, int requested, int available) {
        super("Stock insuficiente para \"" + productName + "\". Solicitado: " + requested + ", disponible: " + available);
    }
}