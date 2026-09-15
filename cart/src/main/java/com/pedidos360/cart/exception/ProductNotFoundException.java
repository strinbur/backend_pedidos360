package com.pedidos360.cart.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String productId) {
        super("Producto no encontrado con id: " + productId);
    }
}